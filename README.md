# CodeGenTool

> 🛠️ 一个可扩展的 IntelliJ IDEA 代码生成插件，通过持续迭代逐步融入各种代码生成逻辑，提升开发效率。

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/coder-knock/CodeGenTool.svg)](https://github.com/coder-knock/CodeGenTool/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/coder-knock/CodeGenTool.svg)](https://github.com/coder-knock/CodeGenTool/network)

[English](README-EN.md) | 中文

## 📖 简介

CodeGenTool 是一个面向 Java 开发者的 IntelliJ IDEA 插件，致力于自动化重复且模板化的代码生成工作，提升开发效率和代码规范性。

当前已实现功能：

✅ **枚举类 `isXXX()` 方法自动生成** - 为枚举类的每个常量自动生成类型安全的判断方法
✅ **枚举一致性检查** - 对比不同分支枚举，检查枚举常量值冲突和重复值，支持实时检查

## ✨ 功能特性

### 枚举 `isXXX()` 方法生成

自动为枚举类生成 `isXXX()` 判断方法，支持两种模式：

1. **实例判断模式** - 直接判断枚举实例相等
2. **字段比较模式** - 根据指定字段的值进行比较（支持 `@EqualsField` 注解自定义）

**生成示例：**

```java
public enum Gender {
    MALE(1, "男"),
    FEMALE(2, "女");

    private final int code;
    private final String desc;

    Gender(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 👇 以下方法由插件自动生成
    public boolean isMale() {
        return this == MALE;
    }

    public boolean isFemale() {
        return this == FEMALE;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
```

## 🚀 安装

### 从 JetBrains Marketplace 安装（推荐）

1. 打开 IntelliJ IDEA
2. 进入 `Settings/Preferences` → `Plugins` → `Marketplace`
3. 搜索 **CodeGenTool**
4. 点击 `Install` 后重启 IDE

### 手动安装

1. 从 [Releases](https://github.com/coder-knock/CodeGenTool/releases) 下载最新版本的 `.zip` 包
2. 进入 `Settings/Preferences` → `Plugins` → `⚙️` → `Install Plugin from Disk...`
3. 选择下载的 zip 包安装
4. 重启 IDE

## 📖 使用方法

### 生成枚举 `isXXX()` 方法

1. 在编辑器中打开一个 Java 枚举类
2. 右键点击编辑器空白区域
3. 在菜单中选择 `CodeGen` → `Generate isXXX Methods`
4. 插件会自动在枚举类末尾生成 `isXXX()` 方法

### 自定义字段比较

如果你希望基于某个字段的值进行比较而不是实例比较，可以使用 `@EqualsField` 注解指定：

```java
public enum Gender {
    MALE(1, "男"),
    FEMALE(2, "女");

    @EqualsField
    private final int code;
    private final String desc;

    Gender(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 👇 插件会基于 code 字段生成
    public boolean isMale(int code) {
        return this.code == code;
    }

    public boolean isFemale(int code) {
        return this.code == code;
    }
}
```

> **说明：** 如果枚举有多个字段且未指定 `@EqualsField`，插件默认会选择第一个非静态字段进行比较。

### 枚举一致性检查

当多个分支并行开发枚举时，容易出现枚举常量值冲突、重复值等问题。插件提供两种检查方式：

#### 1. 快速对比当前枚举（当前文件对比）
1. 在编辑器中打开要对比的枚举类
2. 右键点击 → `CodeGen` → `Compare Current Enum with Branch`
3. 选择要对比的目标分支，点击 OK
4. 弹出对话框显示对比结果

#### 2. 全项目批量对比
1. 右键点击项目或文件夹 → `CodeGen` → `Compare All Enums with Branch`
2. 选择要对比的目标分支
3. 后台扫描全项目，显示所有不一致的枚举

#### 3. 实时检查（推荐）
在对比对话框中勾选 `Enable real-time consistency check`，之后编辑枚举时 IDE 会：
- 自动对比当前枚举与目标分支
- 实时高亮显示不一致的枚举常量
- 在编辑器中直接提示问题，无需手动对比

**检查内容：**
| 检查项 | 说明 |
|--------|------|
| 值不匹配 | 同一枚举常量两边值不同 |
| 本地缺失 | 远端有该常量，本地不存在 |
| 远端缺失 | 本地有该常量，远端不存在 |
| 重复值 | 本地多个常量使用了相同的值 |

**优势：**
- 🔐 完全兼容 IDEA Git 认证，**原生支持 SSH 密钥**
- 🔄 复用 IDEA 已有的 Git 配置，无需额外配置
- ⚡ 后台异步扫描，不阻塞编辑

## 🏗️ 构建项目

### 环境要求

- JDK 17+
- Gradle 8+ (项目已包含 gradle wrapper)

### 构建步骤

```bash
# 克隆项目
git clone https://github.com/coder-knock/CodeGenTool.git
cd CodeGenTool

# 构建插件包
./gradlew buildPlugin

# 结果会生成在 build/distributions/CodeGenTool-<version>.zip
```

### 开发调试

```bash
# 启动新的 IDE 实例进行调试
./gradlew runIde
```

## 📋 支持的环境

- **IDE 版本:** IntelliJ IDEA 2022.3+
- **Java 版本:** Java 8+ (插件编译目标 JDK 17)
- **操作系统:** Windows, macOS, Linux

## 🛣️ 规划功能

- [x] ✅ 枚举分支一致性对比检查（已完成）
- [x] ✅ 枚举值冲突实时检查（已完成）
- [ ] 支持生成 Builder 模式
- [ ] 支持 Record 类生成
- [ ] 支持生成 toString/equals/hashCode 模板
- [ ] 支持自定义代码模板
- [ ] 添加插件设置页面，支持配置生成规则

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

详细的贡献指南请阅读 [CONTRIBUTING.md](CONTRIBUTING.md)。

本项目采用 [Contributor Covenant](CODE_OF_CONDUCT.md) 行为准则，参与即表示您同意遵守。

## 📄 变更日志

每个版本的详细变更请查看 [CHANGELOG.md](CHANGELOG.md)。

## 🔒 安全政策

报告安全漏洞请阅读 [SECURITY.md](SECURITY.md)。

## 📝 许可证

Apache 2.0 License - 查看 [LICENSE](LICENSE) 文件了解详情。

## 👨‍💻 作者

[coderknock](https://github.com/coder-knock)

- 技术博客：https://coderknock.blog.csdn.net
- GitHub：https://github.com/coder-knock

## ⭐ Star 历史

[![Star History Chart](https://api.star-history.com/svg?repos=coder-knock/CodeGenTool&type=Date)](https://star-history.com/#coder-knock/CodeGenTool&Date)
