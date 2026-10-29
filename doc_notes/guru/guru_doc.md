# 1. guru / gurusdk 使用笔记

- `guru`：面向 Git / GitHub 的协作编排（gcm）
- `gurusdk`：面向 GuruSDK 依赖的集成交付

## 怎么选

| 你在做什么                                    | 用哪个    |
|-----------------------------------------------|-----------|
| 在业务仓库里 commit、开 PR                    | `guru`    |
| 查 issue、看 project 任务、建任务             | `guru`    |
| 生成 Release Notes、准备发版标签              | `guru`    |
| SdkVersion 的创建 / 归档 / 迁移               | `guru`    |
| 项目接入或升级 GuruSDK（需要 `gurusdk.yaml`） | `gurusdk` |
| 下载指定 SDK 版本、发布模块                   | `gurusdk` |
| SDK 项目校验、混淆、生成 BI Events 制品       | `gurusdk` |

# 2. 初始化 local maven

- gurusdk get --local /Users/bjit204/code/gurusdk-unity

# 3. 新建feedback 和 Requirements

1. 在codex 桌面版 安装此插件：https://github.com/castbox/guru-jarvis
2，在codex桌面版切到 knowledge(git仓库)， 执行 $feedback 后面跟要添加的feedback内容
