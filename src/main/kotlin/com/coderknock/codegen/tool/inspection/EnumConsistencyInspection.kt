package com.coderknock.codegen.tool.inspection

import com.coderknock.codegen.tool.bundle.adaptedMessage
import com.coderknock.codegen.tool.comparator.EnumComparator
import com.coderknock.codegen.tool.git.GitService
import com.coderknock.codegen.tool.model.EnumComparisonResult
import com.coderknock.codegen.tool.model.ParsedEnumInfo
import git4idea.repo.GitRepository
import com.intellij.codeInspection.*
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.annotations.NotNull

/**
 * 实时枚举一致性检查检查器
 *
 * 功能：在编辑枚举代码时，IDE 会实时检查当前枚举与目标分支的一致性
 * - 检查枚举常量的值是否与目标分支冲突
 * - 检查本地是否有重复的值
 * - 在编辑界面直接高亮显示问题，无需手动比较
 *
 * 使用条件：
 * - 用户必须在比较对话框中启用实时检查
 * - 文件必须是 Java 枚举类
 * - 项目必须是 Git 仓库且配置了比较分支
 *
 * 检查内容：
 * 1. 枚举值不匹配（两边同一常量值不同）
 * 2. 本地有常量在远端不存在
 * 3. 远端有常量在本地不存在
 * 4. 本地有重复的值
 */
class EnumConsistencyInspection : LocalInspectionTool() {

    private val logger = Logger.getInstance(EnumConsistencyInspection::class.java)
    private val gitService = GitService()
    private val config = EnumConsistencyConfig.getInstance()

    override fun checkFile(
        @NotNull file: PsiFile,
        @NotNull manager: InspectionManager,
        isOnTheFly: Boolean
    ): List<ProblemDescriptor> {
        val project = file.project
        val problems = mutableListOf<ProblemDescriptor>()

        // 检查是否启用了实时检查
        if (!config.isRealTimeCheckEnabled) {
            return problems
        }

        // 检查是否是 Java 文件
        val name = file.name
        if (!name.endsWith(".java")) {
            return problems
        }

        // 查找枚举类
        val psiElementFactory = JavaPsiFacade.getInstance(project)
        val psiPackage = psiElementFactory.findPackage(file.containingDirectory.virtualFile.path)
        val classes = mutableListOf<PsiClass>()
        psiPackage?.getClasses(project)?.filterTo(classes) { it.isEnum }

        if (classes.isEmpty()) {
            return problems
        }

        // 获取 Git 仓库
        val repository = gitService.getGitRepository(project) ?: return problems
        val compareBranch = config.compareBranch ?: return problems

        for (enumClass in classes) {
            val virtualFile = enumClass.containingFile.virtualFile ?: continue
            val relativePath = gitService.getRelativePath(project, virtualFile)
            val remoteContent = gitService.getFileContentFromBranch(
                repository, compareBranch, relativePath
            ) ?: continue

            val localContent = file.text

            try {
                val localInfo = EnumComparator.parseEnum(localContent)
                val remoteInfo = EnumComparator.parseEnum(remoteContent)
                val currentBranch = gitService.getCurrentBranch(repository) ?: "local"

                val result = EnumComparator.compare(
                    localInfo, remoteInfo, currentBranch, compareBranch
                )

                // 为每个冲突添加问题描述
                for (conflict in result.conflicts) {
                    val psiEnumConstant = findEnumConstant(enumClass, conflict.enumConstant)
                    val problemElement = psiEnumConstant ?: enumClass
                    val description = buildDescription(conflict)

                    problems.add(
                        manager.createProblemDescriptor(
                            problemElement,
                            description,
                            isOnTheFly,
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            false
                        )
                    )
                }

                // 为本地重复值添加问题
                for (duplicate in result.duplicateValues) {
                    for (constantName in duplicate.affectedConstants) {
                        val psiEnumConstant = findEnumConstant(enumClass, constantName)
                        val problemElement = psiEnumConstant ?: enumClass
                        problems.add(
                            manager.createProblemDescriptor(
                                problemElement,
                                duplicate.message,
                                isOnTheFly,
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                false
                            )
                        )
                    }
                }

            } catch (e: Exception) {
                logger.warn("Failed to compare enum ${enumClass.name}: ${e.message}")
            }
        }

        return problems
    }

    private fun findEnumConstant(enumClass: PsiClass, constantName: String?): PsiElement? {
        if (constantName.isNullOrBlank()) return null
        for (field in enumClass.fields) {
            if (field.name == constantName && field.isEnumConstant) {
                return field
            }
        }
        return null
    }

    private fun buildDescription(conflict: EnumComparisonResult.EnumConflict): String {
        return when (conflict.type) {
            EnumComparisonResult.ConflictType.VALUE_MISMATCH ->
                adaptedMessage("inspection.enum.value.mismatch",
                    conflict.enumConstant, conflict.fieldName, conflict.localValue, conflict.remoteValue)
            EnumComparisonResult.ConflictType.MISSING_IN_REMOTE ->
                adaptedMessage("inspection.enum.missing.remote", conflict.enumConstant)
            EnumComparisonResult.ConflictType.MISSING_IN_LOCAL ->
                adaptedMessage("inspection.enum.missing.local", conflict.enumConstant)
            EnumComparisonResult.ConflictType.LOCAL_DUPLICATE ->
                conflict.message ?: adaptedMessage("inspection.enum.local.duplicate")
            EnumComparisonResult.ConflictType.REMOTE_DUPLICATE ->
                conflict.message ?: adaptedMessage("inspection.enum.remote.duplicate")
        }
    }

    override fun getShortName(): String {
        return "EnumConsistency"
    }

    override fun isEnabledByDefault(): Boolean {
        return true
    }
}
