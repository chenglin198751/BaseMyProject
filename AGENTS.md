# AGENTS.md

> **注意**: 所有的沟通交流默认使用中文。

## 构建命令

### 基础构建命令
```bash
# 使用 gradlew.bat (Windows) 或 ./gradlew (Linux/macOS)
gradlew.bat clean                    # 清理构建产物
gradlew.bat assembleDebug            # 构建 Debug APK
gradlew.bat assembleRelease          # 构建 Release APK
gradlew.bat build                    # 执行完整构建
```

### 测试命令
```bash
gradlew.bat test                     # 运行所有测试
gradlew.bat connectedAndroidTest     # 运行设备测试
gradlew.bat testDebugUnitTest        # 运行 Debug 单元测试
gradlew.bat testReleaseUnitTest      # 运行 Release 单元测试

# 运行单个测试类
gradlew.bat testDebugUnitTest --tests "com.wcl.test.SomeTestClass"

# 运行单个测试方法
gradlew.bat testDebugUnitTest --tests "com.wcl.test.SomeTestClass.testSpecificMethod"
```

### 代码检查命令
```bash
gradlew.bat lint                     # 运行 Android Lint 检查
gradlew.bat lintDebug                # 仅检查 Debug 变体
gradlew.bat lintRelease              # 仅检查 Release 变体
gradlew.bat check                    # 运行所有检查任务（test + lint）
```

## 项目配置

### 技术栈
- **语言**: Java 17 (主要), Kotlin 2.3.0 (支持)
- **构建工具**: Gradle 8.13.2, Android Gradle Plugin 8.13.2
- **目标SDK**: 36 (Android 15), 最低SDK: 26
- **架构**: 单模块应用，包名 `com.wcl.test`
- **视图绑定**: ViewBinding 启用

### 关键依赖
- OkHttp 5.3.2 + Okio 3.16.4 (网络)
- Gson 2.13.2 + GsonFactory 10.3 (JSON)
- Glide 5.0.5 (图片加载)
- MMKV 2.3.0 (键值存储)
- Lifecycle Components 2.10.0 (生命周期管理)
- Coroutines 1.10.2 (异步处理)

## 代码风格指南

### 命名约定
- **类名**: PascalCase (BaseActivity, DownloadManager)
- **方法/变量**: camelCase (mTitleHelper, taskMap, executor)
- **常量**: UPPER_SNAKE_CASE (MAX_THREAD, TAB_FIRST)
- **包名**: 全小写，点分隔 (com.wcl.test.download)
- **资源ID**: snake_case (main_first_icon_selector)

### 导入规范
- 使用具体导入，避免通配符 `import .*`
- 标准库导入在前，第三方库导入在后，最后是项目内导入
- 按字母顺序排列导入语句
- Android framework 导入使用 `android.*` 或 `androidx.*`

### 成员变量前缀
- `m` 前缀: 实例成员变量 (mTitleHelper, mBaseViewHelper)
- `s` 前缀: 静态成员变量 (sInstance)
- 无前缀: 局部变量和参数

### 注释规范
- 类和公共方法必须有 Javadoc 注释
- 使用中文注释，保持与现有代码一致
- 不要删除原有注释，优化时可适当添加注释

### 代码组织
- 遵循 Android 架构包结构: base/, main/, download/, http/, utils/
- 基类放在 base/ 包中 (BaseActivity, BaseFragment, BaseApp)
- 业务模块按功能分包，每个模块包含完整的 M/V/C 层

### 错误处理
- 使用 BuildConfig.LOG_ENABLED 控制日志输出
- Debug 模式启用详细日志，Release 模式关闭
- 网络请求和文件操作必须有适当的异常处理

### 资源管理
- 使用 WeakReference 管理 Activity 引用 (AppHelper.topActivity)
- 实现 LifecycleOwner 模式管理监听器生命周期
- 及时注册/注销 EventBus 和其他监听器

### 设计模式
- **单例模式**: volatile + double-check (DownloadManager, BaseApp)
- **观察者模式**: EventBus, Lifecycle 监听
- **工厂模式**: GsonFactory
- **策略模式**: 下载任务状态管理

### 性能优化
- 线程池限制最大并发数 (下载模块 MAX_THREAD = 4)
- 使用 ViewBinding 替代 findViewById
- 图片加载使用 Glide 优化内存使用
- 长连接使用 WebSocket 管理网络资源

### 版本控制
- 输出 APK 命名: base_project_v_{versionName}.apk
- 签名配置: debug 和 release 使用相同 keystore (keystore_debug.jks)
- ProGuard 配置: release 启用代码混淆和优化

### 测试策略
- 单元测试: 针对工具类和业务逻辑
- 集成测试: 针对数据库和网络模块
- UI 测试: 针对关键用户流程
- 使用 connectedAndroidTest 进行设备端测试