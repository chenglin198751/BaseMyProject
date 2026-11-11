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

# 2. 初始化 local maven 和 打包Parking3D
- gurusdk get --local /Users/bjit204/code/gurusdk-unity
- gurusdk get --local /Users/bjit204/code/gurusdk-unity --retag
- 打包Parking3D：需要改 /Users/bjit204/unity_code/Parking3D/Assets/Guru/guru_sdk.yaml 中的版本号：guru_sdk: v26.11.0-rc.1
- 打包Parking3D：需要改 /Users/bjit204/code/gurusdk-unity/gurusdk-unikit/com.guru.unikit.offlinelog/Editor/Dependencies.xml 中的版本号：com.guru.sdk:offlinelog:26.11.0-rc.1
- 打包Parking3D：/Users/bjit204/code/gurusdk-unity 分支改为：sdk/v26.11.0-rc.1

# 3. 新建feedback 和 Requirements
1. 在codex 桌面版 安装此插件：https://github.com/castbox/guru-jarvis
2，在codex桌面版切到 knowledge(git仓库)， 执行 $feedback 后面跟要添加的feedback内容

# 4. 先新建PR分支，再开发新功能
1. 在ChatGPT桌面版，添加项目后，执行：[@Guru Jarvis Nexus](plugin://guru-jarvis-nexus@personal)  https://github.com/castbox/gurusdk-framework/issues/857

# 5. 发布到本地maven
cd /Users/bjit204/code/GuruSdk-Android
./gradlew :gurusdk-offlinelog:publishToMavenLocal
./gradlew publishToMavenLocal

## 6. Parking3D 与 Guru SDK 的调用关系
1. **加载 Unity SDK**：Parking3D 通过本地 Package 链接加载 `gurusdk-unity/gurusdk-unikit` 中的 Unity C# 包。
2. **声明原生依赖**：Unity SDK 包通过 `Editor/Dependencies.xml` 声明所需的 Android Maven 依赖，供 Parking3D 构建 APK 时引入AAR。
3. **调用 SDK 接口**：Parking3D 的业务代码调用 Unity C# SDK 提供的接口；SDK 再通过对应的桥接层与 Android 原生依赖通信。

具体用 OfflineLog 示例Parking3D 如何调用 `GuruSdk-Android` 和 `gurusdk-unity/gurusdk-unikit` ：
1. 先通过 /Users/bjit204/unity_code/Parking3D/Packages/com.guru.unikit.offlinelog/Editor/Dependencies.xml 中定义 androidPackage 引入 原生android offlinelog.aar
2. 再通过 Runtime 目录下的/Users/bjit204/unity_code/Parking3D/Packages/com.guru.unikit.offlinelog/Runtime/AndroidChannelOfflineLogger.cs 调用原生android offlinelog.aar


