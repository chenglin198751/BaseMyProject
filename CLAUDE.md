# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

BaseMyProject 是一个 Android 应用测试项目，用于验证和调试常用功能组件。

## 注意事项

**注意：1.默认使用中文进行交流和代码注释，除非用户明确要求使用英文;2.禁止调用Windows PowerShell的任何命令，因为当前电脑管理员禁止了Windows PowerShell。可以使用cmd或python或git bash**

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

- **BaseApp**: Application 基类，使用单例模式
- **BaseActivity**: Activity 基类，实现 edge-to-edge、系统栏管理、灰色模式、EventBus 集成、标题栏/加载/空状态的统一管理
- **BaseFragment**: Fragment 基类

### 事件总线

- **EventBus**: 自定义实现的事件总线，通过 `register()`/`unregister()` 管理订阅者，使用 `EventAction` 定义事件 key

### 网络模块 (http 包)

- **HttpUtils**: 基于 OkHttp 的网络请求封装

### 其他重要模块

- **storage 包**: 数据存储相关
- **update 包**: 版本更新相关
- **view/widget 包**: 自定义 View

## 通用工具类

### PreferAppSettings

路径：`com.wcl.test.storage.PreferAppSettings`，基于 MMKV 的全局 KV 存储。新增持久化字段时，必须在此类中统一定义 key 和读写方法，禁止在其他地方直接使用 MMKV。


## 通用组件

### GlideImageView（图片加载组件）

路径：`com.wcl.test.view.image.GlideImageView`，基于 Glide 的图片控件，支持圆角/圆形/边框/宽高比。
- `loadImage(Object uri)` — 自动 CenterCrop + 圆角；`loadImage(uri, RequestOptions)` — 自定义加载
- XML：`riv_corner_radius` / `riv_oval` / `riv_aspect_ratio` / `riv_border_width` / `riv_border_color` / `riv_solid_color`

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
7. **打印日志**: 打印日志时，必须使用 `AppLogUtils`（`com.wcl.test.utils.AppLogUtils`）

## AppConstants 公共常量类说明

路径：`com.wcl.test.utils.AppConstants`，存放全局公共常量。

| 字段 | 说明 |
|------|------|
| `gson` | 全局单例 Gson 实例，基于 GsonFactory，统一使用此实例 |
| `Toggle.isGrayscale` | 是否开启全局黑白（灰阶）模式，默认 `false` |

## AppUtils 工具类说明

路径：`com.wcl.test.utils.AppUtils`

| 方法or常量                            | 说明 |
|-----------------------------------|------|
| `getAppStoragePath()`             | 获取应用私有可写目录 |
| `isNetAvailable()`                | 判断当前是否联网 |
| `dp2px(float)`                    | dp 转 px |
| `showKeyboard(Context, EditText)` | 显示软键盘 |
| `hideKeyboard(Context, EditText)` | 隐藏软键盘 |
| `expandTouchArea(View, int)`      | 扩大 View 的点击区域（单位 dp） |
| `setViewCircle(View)`             | 将 View 裁剪为纯圆形 |
| `setViewRounded(View, int)`       | 将 View 裁剪为圆角矩形（单位 dp） |
| `setDialogEdgeToEdge(Dialog)`     | 设置 Dialog 沉浸式全屏（Edge-to-Edge） |
| `getTopActivity()`                | 获取当前栈顶 Activity（可能为 null） |
| `isAppInForeground()`             | 判断 App 是否处于前台 |
| `getActivityFromContext(Context)` | 从任意 Context 中提取 Activity |
| `isActivityDestroyed(Context)`    | 判断 Activity 是否已销毁或正在 finish |
| `getString(int)`                  | 通过资源 ID 获取字符串 |
| `md5(String)`                     | 对字符串进行 MD5 加密 |
| `getUiHandler()`                  | 获取全局主线程 Handler |
| `formatDouble(double, int)`       | 对 double 四舍五入保留指定小数位 |
| `formatFloat(float, int)`         | 对 float 四舍五入保留指定小数位 |
| `getVersionCode()`                | 获取应用 versionCode |
| `getVersionName()`                | 获取应用 versionName |
| `getPackageName()`                | 获取应用包名 |
| `getAndroidId()`                  | 获取设备 Android ID |
| `isUiThread()`                    | 判断当前线程是否为主线程 |
| `isEdgeToEdge()`                  | 判断应用是否启用了 Edge-to-Edge 模式 |
| `readTextFromAssets(String)`      | 读取 assets 目录下的文本文件内容 |
| `screenWidth`                     | 屏幕宽度（px），应用启动时自动初始化 |
| `screenHeight`                    | 屏幕高度（px），应用启动时自动初始化 |
| `statusBarHeight`                 | 状态栏高度（px），需业务层赋值后使用 |
| `navBarHeight`                    | 底部虚拟导航栏高度（px），需业务层赋值后使用 |