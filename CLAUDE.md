# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

BaseMyProject 是一个 Android 应用测试项目，用于验证和调试常用功能组件。

## 注意事项

**注意：1.默认使用中文进行交流和代码注释，除非用户明确要求使用英文;2.禁止调用Windows PowerShell的任何命令，因为当前电脑管理员禁止了Windows PowerShell。可以使用cmd或者python或者git bash**

## 构建命令

```bash
# 在 Windows 上使用 gradlew.bat
gradlew.bat clean              # 清理构建
gradlew.bat assembleDebug      # 构建 Debug APK
gradlew.bat assembleRelease    # 构建 Release APK

# 运行测试
gradlew.bat test
```

## 架构概览

### 核心基类 (base 包)

- **BaseApp**: Application 基类，使用单例模式（volatile + double-check）。初始化 MMKV、AppHelper（前后台监听、Activity 栈）、GsonFactory
- **BaseActivity**: Activity 基类，实现 edge-to-edge、系统栏管理、灰色模式、EventBus 集成、标题栏/加载/空状态的统一管理
- **BaseFragment**: Fragment 基类

### 事件总线

- **EventBus**: 自定义实现的事件总线，通过 `register()`/`unregister()` 管理订阅者，使用 `EventAction` 定义事件 key
- **EventBus2 / AppEventProvider**: 基于 ContentProvider 的进程间 IPC 事件总线（用于多进程场景）

### 下载模块 (download 包)

核心组件：
- **DownloadManager** (单例): 管理所有下载任务、线程池（最大4线程）、等待队列
- **DownloadTask**: 下载任务数据模型，状态包括 IDLE/WAITING/DOWNLOADING/PAUSED/FINISHED/ERROR
- **DownloadWorker**: 执行实际下载的 Worker
- **DownloadDBHelper**: 任务持久化

**设计特点**：
- `setDownloadListener(url, LifecycleOwner, DownloadListener)` 订阅下载状态，自动管理生命周期
- 线程池满时任务进入 WAITING 状态并排队
- 支持断点续传（.temp 文件）

### 网络模块 (http 包)

- **HttpUtils**: 基于 OkHttp 的网络请求封装
- **WebSocketExecutor**: WebSocket 长连接实现
- **Downloader**: 另一套下载实现（旧版？）

### 其他重要模块

- **storage 包**: 数据存储相关
- **update 包**: 版本更新相关
- **view/widget 包**: 自定义 View
- **MainFirstFragment**: 首页 Fragment，用于测试各类组件

## 技术栈

- **Language**: Java 17, Kotlin
- **Build**: Gradle 8.13.2, AGP 8.13.2
- **Android SDK**: min 26, target 36 (Android 15)
- **UI**: ViewBinding, Material Design
- **Network**: OkHttp 5.3.2, Okio 3.16.4
- **Image**: Glide 5.0.5
- **Storage**: MMKV 2.3.0
- **Async**: Kotlin Coroutines 1.10.2
- **Architecture**: Lifecycle Components 2.10.0

## 注意事项

1. **语言偏好**: 代码使用 Java 语言编写（除非显式要求 Kotlin）
2. **命名规范**: 包名 `com.wcl.test`
3. **签名配置**: debug 和 release 使用相同的签名配置（keystore_debug.jks）
4. **Apk 输出路径**: `app/debug/base_project_v_{versionName}.apk`, `app/release/base_project_v_{versionName}.apk`
5. **日志**: 通过 `BuildConfig.LOG_ENABLED` 控制日志开关
6. **代码偏好**: 优化代码时可以添加注释，但不要删除原有注释