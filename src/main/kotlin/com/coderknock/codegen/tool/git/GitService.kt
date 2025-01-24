package com.coderknock.codegen.tool.git

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.TreeWalk
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

/**
 * Git 服务类，用于从指定分支获取文件内容
 *
 * 设计思路：使用 IDEA 内置的 Git4Idea 集成来获取 Git 仓库信息
 * 这样可以自动继承 IDEA 已有的认证配置（包括 SSH 密钥、代理、凭据等）
 * 避免了插件自己处理认证的各种问题，提高兼容性
 *
 * 底层依然使用 JGit 来读取 commit 中的文件内容，这部分不需要认证
 */
class GitService {

    private val logger = Logger.getInstance(GitService::class.java)

    /**
     * 获取项目的 Git 仓库（使用 IDEA 内置管理）
     */
    fun getGitRepository(project: Project): GitRepository? {
        val manager = GitRepositoryManager.getInstance(project)
        val repositories = manager.repositories
        return if (repositories.isNotEmpty()) repositories.first() else null
    }

    /**
     * 获取所有本地分支名称
     */
    fun getLocalBranches(repository: GitRepository): List<String> {
        return try {
            repository.branches.localBranches
                .map { it.name }
                .sorted()
        } catch (e: Exception) {
            logger.warn("Cannot list local branches: ${e.message}")
            emptyList()
        }
    }

    /**
     * 获取所有远程分支名称
     */
    fun getRemoteBranches(repository: GitRepository): List<String> {
        return try {
            repository.branches.remoteBranches
                .map { it.name }
                .filter { !it.contains("HEAD") }
                .sorted()
        } catch (e: Exception) {
            logger.warn("Cannot list remote branches: ${e.message}")
            emptyList()
        }
    }

    /**
     * 从指定分支获取文件内容
     */
    fun getFileContentFromBranch(
        repository: GitRepository,
        branchName: String,
        filePath: String
    ): String? {
        return try {
            // 查找分支对应的 ref
            val objectId = repository.findRefByBranchName(branchName) ?: return null
            val commit = repository.parseCommit(objectId) ?: return null

            return getFileContentFromCommit(repository, commit, filePath)
        } catch (e: Exception) {
            logger.error("Cannot get file content from branch $branchName: ${e.message}")
            null
        }
    }

    /**
     * 从指定 commit 获取文件内容
     */
    private fun getFileContentFromCommit(
        repository: GitRepository,
        commit: RevCommit,
        filePath: String
    ): String? {
        return try {
            val treeWalk = TreeWalk.forPath(
                repository.repository,
                filePath,
                commit.tree
            ) ?: return null

            if (treeWalk.next()) {
                val objectId = treeWalk.getObjectId(0)
                val blob = repository.repository.open(objectId)

                ByteArrayOutputStream().use { output ->
                    blob.copyTo(output)
                    return output.toString(StandardCharsets.UTF_8.name())
                }
            }
            null
        } catch (e: Exception) {
            logger.error("Cannot get file content from commit: ${e.message}")
            null
        }
    }

    /**
     * 获取当前分支名称
     */
    fun getCurrentBranch(repository: GitRepository): String? {
        return try {
            repository.currentBranchName
        } catch (e: Exception) {
            logger.warn("Cannot get current branch: ${e.message}")
            null
        }
    }

    /**
     * 获取文件相对于项目根目录的相对路径
     */
    fun getRelativePath(project: Project, file: VirtualFile): String {
        val basePath = project.basePath ?: return file.path
        return file.path.removePrefix(basePath).removePrefix("/")
    }

    /**
     * 检查项目是否是 Git 仓库
     */
    fun isGitProject(project: Project): Boolean {
        return getGitRepository(project) != null
    }

    /**
     * 通过分支名称查找 ref（支持本地分支和远程分支）
     */
    private fun GitRepository.findRefByBranchName(branchName: String): ObjectId? {
        val jgitRepo = this.repository
        // 先尝试本地分支
        var ref = jgitRepo.findRef("refs/heads/$branchName")
        if (ref != null) {
            return ref.objectId
        }
        // 再尝试远程分支
        ref = jgitRepo.findRef("refs/remotes/$branchName")
        if (ref != null) {
            return ref.objectId
        }
        // 最后尝试匹配后半部分（比如 origin/main 匹配 main）
        if (branchName.contains("/")) {
            val shortName = branchName.substringAfter("/")
            ref = jgitRepo.findRef("refs/heads/$shortName")
            if (ref != null) return ref.objectId
            ref = jgitRepo.findRef("refs/remotes/$branchName")
            if (ref != null) return ref.objectId
        }
        return null
    }

    /**
     * 解析 commit
     */
    private fun GitRepository.parseCommit(objectId: ObjectId): RevCommit? {
        return try {
            repository.parseCommit(objectId)
        } catch (e: Exception) {
            logger.warn("Cannot parse commit $objectId: ${e.message}")
            null
        }
    }
}
