# 变更日志

所有项目的重要变更都将记录在此文件中。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，
本项目遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [Unreleased] - 未发布

### 新增
- **枚举一致性检查功能** - 支持枚举在不同分支之间的一致性对比
- **当前枚举快速对比** - 对当前编辑的枚举快速与目标分支对比
- **全项目批量扫描对比** - 批量检查项目中所有枚举与目标分支差异
- **实时一致性检查** - 编辑枚举时 IDE 实时高亮显示不一致问题
- 检查内容包括：值不匹配、常量缺失、重复值

### 改进
- 重构 Git 服务，**使用 IDEA 内置 Git4Idea 集成**，原生支持 SSH 认证
- 现在完全复用 IDEA 已有的 Git 配置，不再需要插件自己处理认证
- 添加英文 README 文档
- 添加开源项目标准文档：CODE_OF_CONDUCT.md, CONTRIBUTING.md, SECURITY.md
- 添加 GitHub Issue 和 Pull Request 模板
- 完善中文 README 文档，添加使用示例和构建说明
- 修复 .gitignore，恢复 gradle wrapper 文件追踪

## [0.0.3] - 2025-01-23

### 新增
- 支持 `@EqualsField` 注解自定义用于比较的字段
- 支持基于字段值比较生成 `isXXX(int code)` 方法

### 改进
- 优化代码解析逻辑
- 改进菜单显示位置

## [0.0.2] - 2024-12-26

### 新增
- 支持为枚举常量生成 `isXXX()` 实例比较方法

### 改进
- 优化代码结构，分离核心逻辑和 IDE 操作

## [0.0.1] - 2024-12-20

### 新增
- 初始版本发布
- 基本的枚举 `isXXX()` 方法生成功能

[Unreleased]: https://github.com/coder-knock/CodeGenTool/compare/v0.0.4...HEAD
[0.0.4]: https://github.com/coder-knock/CodeGenTool/compare/v0.0.3...v0.0.4
[0.0.3]: https://github.com/coder-knock/CodeGenTool/compare/v0.0.2...v0.0.3
[0.0.2]: https://github.com/coder-knock/CodeGenTool/compare/v0.0.1...v0.0.2
[0.0.1]: https://github.com/coder-knock/CodeGenTool/releases/tag/v0.0.1
