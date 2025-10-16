# Tools For Project Developing written in Clojure

这是一个使用 Clojure 编写的项目开发工具集合。

## 特性

- **命令行工具**：提供了一系列命令行工具，如 `ls`、`rm`、`kill` 等，用于简化日常开发任务。
- **实用工具**：包括 Git 操作、日志记录、图像处理、OCR 支持等功能。
- **跨平台支持**：支持多种操作系统，并包含特定于 Windows 和 Linux 的资源文件。
- **插件系统**：支持插件扩展，例如 JetBrains 路径修改插件。

## 目录结构

- `src/main/clojure/` - Clojure 源代码目录，包含多个模块化组件。
- `src/main/java/` - 包含 Java 接口定义，用于与本地库交互。
- `src/main/resources/` - 资源文件目录，包含模板文件和平台相关的二进制文件。
- `scripts/` - 包含构建和清理脚本。
- `extra/libclipbrd/` - 包含额外的库文件和项目配置。

## 安装

确保你已经安装了 [Clojure CLI](https://clojure.org/guides/getting_started)

1. 克隆仓库：
   ```bash
   git clone https://gitee.com/monkeyNaive/tools-clj.git
   cd tools-clj
   ```

2. 安装依赖：
   ```bash
   clojure -X:deps
   ```

## 使用

你可以通过 Clojure 命令运行各个工具模块。例如：

```bash
clojure -M -m cmd.core
```

具体命令和功能请参考源代码中的各个模块。

## 构建

使用提供的脚本进行构建：

```bash
./scripts/compile
```

对于 Windows 用户，可以使用 `scripts/compile.bat`。

### 构建libclipbrd

这个库是使用Pascal开发的，可以安装lazarus进行编译

## upx compress

[upx compress fix](https://github.com/upx/upx/issues/670#issuecomment-2869306660)

## 清理

清理构建文件：

```bash
./scripts/clean
```

## 贡献

欢迎贡献代码和改进！请提交 Pull Request 或创建 Issue 来报告问题。

## 许可证

本项目采用 MIT 许可证。详情请查看 [LICENSE](LICENSE) 文件。
