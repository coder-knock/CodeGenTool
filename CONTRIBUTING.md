# 贡献指南

感谢你对 CodeGenTool 项目感兴趣！我们欢迎各种形式的贡献。

## 📋 行为准则

本项目采用 [Contributor Covenant](CODE_OF_CONDUCT.md) 行为准则，参与本项目即表示你同意遵守此准则。

## 🐛 报告 Bug

如果你发现了 Bug，请通过 [GitHub Issues](https://github.com/coder-knock/CodeGenTool/issues) 报告，并包含以下信息：

1. **问题描述** - 清晰简洁地描述问题是什么
2. **复现步骤** - 列出复现问题的具体步骤
3. **预期行为** - 描述你期望发生的行为
4. **实际行为** - 描述实际发生的行为
5. **截图** - 如果适用，添加截图帮助说明问题
6. **环境信息** - 你的 IntelliJ IDEA 版本、JDK 版本、操作系统等

我们提供了 [Bug 报告模板](.github/ISSUE_TEMPLATE/bug_report.md)，按照模板填写即可。

## ✨ 功能请求

如果你有新功能想法或改进建议，也欢迎通过 [GitHub Issues](https://github.com/coder-knock/CodeGenTool/issues) 提出：

1. **清晰描述功能** - 说明你想要什么功能
2. **说明价值** - 解释为什么这个功能对大多数用户有价值
3. **讨论可能性** - 我们会一起讨论实现方案

我们也提供了 [功能请求模板](.github/ISSUE_TEMPLATE/feature_request.md)。

## 🔧 开发环境搭建

### 前置要求

- JDK 17 或更高版本
- IntelliJ IDEA 2022.3 或更高版本（推荐）
- Git

### 克隆项目

```bash
git clone https://github.com/coder-knock/CodeGenTool.git
cd CodeGenTool
```

### 使用 Gradle 构建

项目已包含 Gradle wrapper，不需要提前安装 Gradle。

```bash
# 编译并运行测试
./gradlew build

# 构建插件包（输出到 build/distributions/）
./gradlew buildPlugin

# 启动新的 IDE 实例进行调试
./gradlew runIde
```

### 在 IntelliJ IDEA 中导入

1. 打开 IntelliJ IDEA
2. 选择 `File` → `Open`
3. 选择项目根目录的 `build.gradle.kts`
4. 等待 Gradle 同步完成即可开始开发

## 🧪 测试

添加新功能时，请确保：

1. 添加相应的单元测试
2. 所有现有测试都能通过
3. 手动测试你的功能在 IDE 中正常工作

运行测试：

```bash
./gradlew test
```

## 📝 代码风格

- 遵循 [Java 代码规范](https://google.github.io/styleguide/javaguide.html) 和 [Kotlin 编码规范](https://kotlinlang.org/docs/coding-conventions.html)
- 保持代码简洁清晰，添加必要注释
- 提交前确保代码格式正确（IntelliJ 中使用 `Code` → `Reformat Code`）

## 🔀 提交 Pull Request

1. [Fork](https://github.com/coder-knock/CodeGenTool/fork) 本仓库到你的 GitHub 账号
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交你的更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到你的分支 (`git push origin feature/amazing-feature`)
5. 在 GitHub 上开启一个 [Pull Request](https://github.com/coder-knock/CodeGenTool/pulls)
6. 等待代码审查和合并

我们提供了 [Pull Request 模板](.github/PULL_REQUEST_TEMPLATE.md)，请按照模板填写信息。

### Pull Request 指南

- 保持 PR 聚焦：一个 PR 只解决一个问题
- 如果 PR 较大，拆分成多个小 PR
- 确保 CI 检查通过
- 更新相关文档（如 README）
- 所有讨论的反馈都需要处理

## 🔖 版本发布

项目使用 [Semantic Versioning](https://semver.org/) 语义化版本：

- **主版本号**：不兼容的 API 修改
- **次版本号**：向下兼容的功能性新增
- **修订号**：向下兼容的问题修正

## 📄 许可证

你提交的贡献将自动按照项目 [Apache 2.0](LICENSE) 许可证授权。

## 🙏 鸣谢

感谢所有做出贡献的开发者！
