# BaseMyProject 当前工程功能索引

> 本文档仅记录当前 `app` 模块中可定位到源码、Manifest 或 Gradle 配置的能力。
> 已删除实现、未接入的第三方库、通用 Android/Java 技巧不再在本文档维护。

## 工程基线

| 项目                        | 当前配置           |
|---------------------------|----------------|
| 模块                        | `app`          |
| namespace / applicationId | `com.wcl.test` |
| Java                      | 17             |
| minSdk                    | 28             |
| compileSdk / targetSdk    | 37 / 37        |
| Gradle                    | 9.6.1          |
| Android Gradle Plugin     | 9.3.1          |
| ViewBinding / BuildConfig | 已启用            |

当前源码、依赖与注册组件以 `app/src/main`、`app/build.gradle`、`common.gradle`、`AndroidManifest.xml` 为准。

---

## 1. 应用与基础架构

### 1.1 `MainApp` / `BaseApp`

- Manifest 中注册的 Application 是 `com.wcl.test.base.MainApp`。
- `MainApp` 继承 `BaseApp`；全局 Application 实例由 `BaseApp.getApp()` 提供。
- `BaseApp.onCreate()` 负责：
  - 初始化 MMKV；
  - 禁用默认夜间模式；
  - 初始化应用前后台观察；
  - 跟踪顶部 Activity；
  - 初始化 GsonFactory 容错配置；
  - 注册 SmartRefreshLayout 的默认 Header/Footer。

路径：`app/src/main/java/com/wcl/test/base/BaseApp.java`

`BaseApp.getApp()` 返回 Application Context。需要主题、Window、Dialog 或页面生命周期时，应继续使用 Activity / Fragment 的 Context，不能一律替换为 Application Context。

### 1.2 `AppUtils`

`AppUtils` 是当前公共工具入口，包含：

- 屏幕宽高、状态栏高度、导航栏高度；
- 网络状态：`isNetAvailable()`；
- dp 转 px：`dp2px(float)`；
- 主线程 Handler：`getUiHandler()`；
- 版本、包名、Android ID；
- 顶部 Activity：`getTopActivity()`；
- 前后台状态：`isAppInForeground()`；
- Context 中提取 Activity：`getActivityFromContext(Context)`；
- Activity / Fragment 销毁判断；
- 输入法显示、隐藏；
- View 点击区域扩展；
- 资源、assets 文本读取；
- MD5 与数字格式化。

路径：`app/src/main/java/com/wcl/test/utils/AppUtils.java`

#### `AppUtils.FileUtils`

`AppUtils.FileUtils.getAppStoragePath()` 优先返回应用外部私有目录：

```
/storage/emulated/0/Android/data/{packageName}/files
```

外部目录不可用时回退到内部私有目录：

```
/data/data/{packageName}/files
```

这两个目录均不需要传统共享存储权限。

### 1.3 `AppConstants`

`AppConstants` 当前提供：

- 全局 Gson：`AppConstants.gson`；
- 灰度开关：`AppConstants.Toggle.isGrayscale`。

路径：`app/src/main/java/com/wcl/test/utils/AppConstants.java`

屏幕尺寸不在 `AppConstants` 中，位于 `AppUtils`。

### 1.4 `EnvToggle`、`ToggleSettings` 与日志

- `EnvToggle` 管理 Debug / Log 开关；
- `ToggleSettings` 使用 MMKV 保存运行配置；
- `AppLogUtils` 实际根据 `EnvToggle.isLog()` 决定是否输出日志，不等同于 `isDebug()`。

路径：

```
app/src/main/java/com/wcl/test/EnvToggle.java
app/src/main/java/com/wcl/test/storage/ToggleSettings.java
app/src/main/java/com/wcl/test/utils/AppLogUtils.java
```

#### 拨号调试入口

Manifest 保留了秘密代码 Receiver：

```
*#*#2022360#*#*
```

对应 `DialPhoneBroadcastReceiver`。当前源码可确认其会发送 EventBus 事件；尚未确认完整的事件消费与调试界面展示链路，因此不能视为已验证的“拨号即可打开调试面板”功能。

路径：

```
app/src/main/java/com/wcl/test/helper/DialPhoneBroadcastReceiver.java
app/src/main/AndroidManifest.xml
```

---

## 2. Base UI 与事件体系

### 2.1 `BaseActivity`

`BaseActivity` 是页面统一基类，继承 `AppCompatActivity`，提供：

- Edge-to-Edge 与 WindowInsets 处理；
- 状态栏、导航栏高度记录；
- 灰度模式；
- 统一标题栏：`MainTitleHelper`；
- `WaitDialog`；
- Loading、空数据、无网络状态页；
- 指定状态页挂载容器和显示位置；
- 单实例 Activity 保留策略；
- EventBus 自动注册、注销及向已附加 `BaseFragment` 的事件分发。

业务 Activity 应在 `onCreate()` 中先调用 `super.onCreate(savedInstanceState)`，再使用：

```
setContentLayout(R.layout.your_layout);
```

不要以系统 `setContentView()` 代替 `setContentLayout()`，否则会绕开基类的内容容器与状态页能力。

`setContentLayout(int)` 要求 layout 根节点是 `ViewGroup`。

路径：`app/src/main/java/com/wcl/test/base/BaseActivity.java`

### 2.2 `BaseFragment`

`BaseFragment` 提供与 `BaseActivity` 对应的状态页、等待框和事件分发能力。

子类需要实现：

```
protected int getContentLayout();
protected void onViewCreated(Bundle savedInstanceState, View view);
```

基类已经接管并 final 化标准 `onCreateView()` 与标准签名的 `onViewCreated(View, Bundle)`；业务逻辑应写在上述自定义回调中。

`BaseFragment` 需要宿主为 `BaseActivity` 才能使用等待框等基类能力。

路径：`app/src/main/java/com/wcl/test/base/BaseFragment.java`

### 2.3 `EventBus`

当前 `EventBus` 是**应用进程内**的监听者事件总线：

- 事件通过 `EventBus.post(String, Object)` 发送；
- 始终在主线程分发；
- 注册和注销仅由 `BaseActivity` 在生命周期内管理；
- `BaseActivity` 会将事件递归分发给已附加的 `BaseFragment`。

它不是 Android 系统广播，不跨进程，也不适合作为所有异步任务的通用替代方案。

路径：

```
app/src/main/java/com/wcl/test/base/EventBus.java
app/src/main/java/com/wcl/test/base/EventAction.java
```

### 2.4 `FragmentSwitcher`

`FragmentSwitcher` 用于 Tab 场景的 Fragment 切换：

- 支持 Fragment 懒创建；
- 使用 class name 作为 Fragment tag 恢复已存在实例；
- 自动 hide / show 已管理的 Fragment；
- `BaseFragment.onSelected(int)` 在选中时回调。

在 Activity 中传入 `getSupportFragmentManager()`；在 Fragment 中传入 `getChildFragmentManager()`。

路径：`app/src/main/java/com/wcl/test/helper/FragmentSwitcher.java`

---

## 3. 网络、上传与下载

### 3.1 `OkHttpExecutor`

`OkHttpExecutor` 是当前 OkHttp 请求入口，采用 Builder 链式调用。

#### 异步 GET / POST

以下代码必须写在 `Activity` 或 `Fragment` 的方法体中，不能直接放在类的大括号内。

```
// Activity 中调用
private void requestFromActivity() {
    Map<String, Object> params = new HashMap<>();
    params.put("page", 1);

    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", "Bearer token");

    OkHttpExecutor.get("https://api.example.com/list")
            .params(params)
            .headers(headers)
            .execute(this, (success, result) -> {
                // 主线程回调；Activity 销毁时回调会被丢弃
            });
}
```

```
// Fragment 中调用
private void requestFromFragment() {
    Map<String, Object> params = new HashMap<>();
    params.put("account", "demo");

    OkHttpExecutor.post("https://api.example.com/login")
            .params(params)
            .execute(this, (success, result) -> {
                // 主线程回调；Fragment 已销毁时回调会被丢弃
            });
}
```

支持 Activity 和 Fragment 场景的生命周期检查。

#### 同步请求

```
AppThreadPoolExecutor.getExecutor().execute(() -> {
    String result = OkHttpExecutor.get(url)
            .params(params)
            .executeSync();
});
```

`executeSync()` 会阻塞当前线程，必须在子线程调用。

#### 上传和单次下载

该类还提供：

- 多图上传；
- 普通下载；
- 快速下载。

路径：`app/src/main/java/com/wcl/test/http/OkHttpExecutor.java`

### 3.2 `AppThreadPoolExecutor`

工程共享线程池：

- 核心线程数：2；
- 最大线程数：8；
- 队列容量：100；
- 拒绝策略：`AbortPolicy`。

路径：`app/src/main/java/com/wcl/test/utils/AppThreadPoolExecutor.java`

### 3.3 持久化多任务下载

`download` 包提供独立于 `OkHttpExecutor` 的下载任务系统：

- `DownloadManager`：任务创建、开始、暂停、删除、等待队列；
- `DownloadTask`：任务模型与状态；
- `DownloadWorker`：单任务执行单元；
- `DownloadDBHelper`：SQLite 任务记录；
- `DownloadListener`：状态监听；
- `DownloadButton`：下载 UI 控件；
- `ProgressColorTextView`：进度文本控件。

`DownloadManager` 最多同时执行 4 个下载任务，超过数量的任务进入等待队列；启动时会恢复数据库中的任务记录。

路径：

```
app/src/main/java/com/wcl/test/download/
app/src/main/java/com/wcl/test/download/ui/
```

演示页面：`TestDownloadActivity`。

> `download.DownloadWorker` 是普通任务执行类，不是 AndroidX WorkManager 的 `Worker`。

---

## 4. 数据存储与用户状态

### 4.1 MMKV

MMKV 在 `BaseApp.onCreate()` 初始化。当前使用场景包括：

- `PreferApp`：应用级小型键值；
- `ToggleSettings`：Debug / Log 等开关；
- `UserManager`：当前仅保存、读取和清空 UID。

路径：

```
app/src/main/java/com/wcl/test/storage/PreferApp.java
app/src/main/java/com/wcl/test/storage/ToggleSettings.java
app/src/main/java/com/wcl/test/storage/UserManager.java
```

### 4.2 大文本 KV

- `BigStringDb`：基于 SQLite 的大文本 key-value 存储；
- `BigStringFile`：基于文件系统的大文本 key-value 存储；
- `IBigString`：共同接口。

路径：`app/src/main/java/com/wcl/test/storage/`

使用前应自行约束 key 的来源和格式；文件型存储不应用于未经校验的外部 key。

### 4.3 `AccountContentProvider`

`AccountContentProvider` 是使用 MediaStore / 公共 Alarms 目录读写数据的工具类，不是 Android `ContentProvider`，也没有作为 Provider 注册到 Manifest。

路径：`app/src/main/java/com/wcl/test/storage/alarms/AccountContentProvider.java`

---

## 5. 刷新、列表与 ViewPager2

### 5.1 SmartRefreshLayout

当前工程直接使用 `SmartRefreshLayout`，默认的自定义 Header / Footer 在 Application 初始化时注册。

相关代码：

```
app/src/main/java/com/wcl/test/view/pullrefresh/CustomRefreshHeader.java
app/src/main/java/com/wcl/test/view/pullrefresh/CustomRefreshFooter.java
app/src/main/java/com/wcl/test/base/AppHelper.java
```

相关示例：

```
TestRecyclerViewRefreshActivity
TestRefreshWithBannerActivity
TestSnapNestFragment
```

### 5.2 基础 Adapter

- `BaseListViewAdapter`：ListView Adapter 基类；
- `BaseRecyclerViewAdapter`：RecyclerView Adapter 基类；
- `BaseRecyclerViewHolder`：RecyclerView ViewHolder 基类。

路径：`app/src/main/java/com/wcl/test/base/`

### 5.3 ViewPager2

当前推荐使用 ViewPager2：

- `CommonFragmentViewPager2Adapter`：基于 `FragmentStateAdapter`，接收 `List<BaseFragment>`；
- `MagicIndicatorViewPager2Binder`：MagicIndicator 与 ViewPager2 绑定工具；
- `NestedScrollableHost`：嵌套滚动处理组件。

路径：

```
app/src/main/java/com/wcl/test/common/
app/src/main/java/com/wcl/test/view/NestedScrollableHost.kt
```

相关示例：

```
TestViewPager2Activity
TestSnapNestViewPager2Activity
TestTabLayoutActivity
```

旧 ViewPager 相关控件如 `MyTabLayout`、`NoScrollViewPager`、`CustomViewPagerIndicator` 仍保留在源码中，但不作为新页面首选方案。

---

## 6. 图片、文件共享与 APK 安装

### 6.1 `GlideImageView`

`GlideImageView` 是基于 Glide 的图片加载控件，支持：

- 圆角；
- 圆形；
- 宽高比；
- 边框；
- 占位图和错误图。

常用 XML 属性：

```
app:riv_corner_radius="8dp"
app:riv_oval="true"
app:riv_aspect_ratio="1.778"
app:riv_border_width="2dp"
app:riv_border_color="#FF4081"
app:riv_solid_color="#FFFFFFFF"
```

调用 `loadImage(Object)` 时会通过 Glide 应用圆形或圆角变换；只设置 `android:src` 或 `setImageDrawable()` 不会自动执行 Glide 变换。

路径：`app/src/main/java/com/wcl/test/view/image/GlideImageView.java`

### 6.2 其他图片与图片工具

- `BitmapUtils`：压缩、缩放、截图、拼接和 URI 图片处理；
- `RatioImageView`：按 Drawable 比例测量；
- `ZoomImageView`：支持缩放图片；
- `FastBlurUtil` / `FastBlurUtil2`：Bitmap 模糊；
- `LongImageView`：基于 WebView 的长图展示控件；
- `RecyclerDivider`：RecyclerView 分割线。

路径：

```
app/src/main/java/com/wcl/test/utils/BitmapUtils.java
app/src/main/java/com/wcl/test/view/image/RatioImageView.java
app/src/main/java/com/wcl/test/view/zoomphoto/ZoomImageView.java
app/src/main/java/com/wcl/test/utils/FastBlurUtil.java
app/src/main/java/com/wcl/test/utils/FastBlurUtil2.java
app/src/main/java/com/wcl/test/widget/LongImageView.java
app/src/main/java/com/wcl/test/view/RecyclerDivider.java
```

### 6.3 图片选择器

#### `PhotosPicker`：单图选择

```
private PhotosPicker photosPicker;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    photosPicker = PhotosPicker.from(this, path -> {
        // path 为可读取的本地路径；失败时可能为 null
    });
}

private void choosePhoto() {
    photosPicker.start();
}
```

支持 Activity 和 Fragment 工厂方法。

#### `PhotoPicker2`：单选、多选、拍照、裁剪

```
PhotoPicker2 picker = PhotoPicker2.from(this, paths -> {
    // paths 为可读取的本地路径列表
});

picker.pickSingle();
picker.pickMultiple();
picker.capture(this);
// crop() 的 srcUri 必须为 content:// URI。
picker.crop(contentUri, 1, 1);
```

注意：

- 两个 Picker 均使用 Activity Result API，应在 Activity / Fragment 合适的初始化阶段创建；
- 返回类型是本地路径；`content://` 图片会根据系统版本和 Provider 情况转换为应用可读取文件；
- 相机和裁剪使用 `CustomTorchFileProvider`；
- 裁剪依赖系统或第三方处理 `com.android.camera.action.CROP`，设备没有对应组件时会抛 `ActivityNotFoundException`；
- 取消操作目前不会通过 `OnFinishedListener2` 传递单独的取消状态。

路径：

```
app/src/main/java/com/wcl/test/utils/PhotosPicker.java
app/src/main/java/com/wcl/test/utils/PhotoPicker2.java
```

### 6.4 `CustomTorchFileProvider` 与 APK 安装

Manifest 注册：

```
${applicationId}.custom.file_provider
```

其路径配置位于：

```
app/src/main/res/xml/app_torch_file_paths.xml
```

当前覆盖应用私有 Download、Pictures 与 cache 目录，用于 APK 安装及图片拍照/裁剪等文件共享场景。

相关类：

```
app/src/main/java/com/wcl/test/CustomTorchFileProvider.java
app/src/main/java/com/wcl/test/utils/ApkInstaller.java
```

---

## 7. WebView 与页面导航

### 7.1 系统 WebView

- `BaseWebViewActivity.start(context, url, title)` 启动 WebView 页面；
- `BaseWebViewFragment.loadUrl(url)` 支持命令式加载，并会在视图创建后补加载；
- 当前实现使用系统 `android.webkit.WebView`；
- 支持 URL 保存恢复、页面 Loading 状态、Cookie 白名单、http(s) 内部加载、其他 scheme 交给系统处理；
- 下载链接由外部 Intent 处理；
- 已关闭 WebView 的 file / content access，并在 `onDestroyView()` 中销毁 WebView。

路径：

```
app/src/main/java/com/wcl/test/base/BaseWebViewActivity.java
app/src/main/java/com/wcl/test/base/BaseWebViewFragment.java
```

### 7.2 其他页面/容器辅助组件

- `ReplaceViewUtils`：替换 View；
- `DragRelativeLayout`：可拖拽布局；
- `BottomDialogFragment`：BottomSheetDialogFragment 示例；
- `PullToZoomRecyclerView`：下拉放大头部效果；
- `PullScrollView`：下拉交互效果；
- `AutoScrollRecyclerView`：自动滚动 RecyclerView；
- `AutoGalleryBannerView`、`AutoGalleryBannerView2`：自动轮播组件；
- `NoScrollGridView`、`NoScrollListView`：嵌套场景下的不可滚动列表控件；
- `FlowLayout`、`VerticalTextView`、`MarqueeTextView`、`HollowTextView`、`ShimmerTextView`：自定义文本和布局控件。

路径：`app/src/main/java/com/wcl/test/helper/`、`view/`、`widget/`

---

## 8. 弹窗、点击与常用工具

### 8.1 弹窗

- `CommonDialog`：标题、消息、自定义 View、单/双按钮；
- `WaitDialog`：加载等待弹窗；
- `PopupWindowUtils`：在目标 View 周围展示 PopupWindow；
- `ToastUtils`：统一 Toast 工具。

路径：`app/src/main/java/com/wcl/test/widget/`、`app/src/main/java/com/wcl/test/utils/PopupWindowUtils.java`

### 8.2 点击和回调

- `OnSingleClickListener`：防重复点击监听；
- `OnMultipleClickListener`：多击监听；
- `SingleClickUtils.isSingle(key)`：简单单击节流；
- `OnFinishedListener<B, T>`、`OnFinishedListener2<T>`：通用完成回调。

路径：

```
app/src/main/java/com/wcl/test/listener/
app/src/main/java/com/wcl/test/utils/SingleClickUtils.java
```

### 8.3 其他工具

- `TextViewLinesUtils`：计算 TextView 文本行数，传入的宽度单位为 px；
- `EnglishCharFilter`：按字符编码范围限制输入长度；
- `ReflectUtils`、`SystemProperties`：反射和系统属性工具；
- `ZipByAnt`、`ZipByJava`：压缩工具；
- `DESUtils`、`DesUtils2`：DES 相关工具。

路径：`app/src/main/java/com/wcl/test/utils/`

---

## 9. 定时器

### 9.1 `HandlerTimer`

适用于进程存活期间的短间隔定时任务，基于主线程 Handler。调用方应在适当的生命周期节点 `stop()`，避免延迟任务继续持有页面对象。

### 9.2 `AlarmTimer`

适用于间隔较长的调度，源码建议间隔不小于 1 分钟。

实际触发会受系统省电策略、精确闹钟权限、动态 Receiver 生命周期与进程状态影响，不应视为“杀进程后仍保证执行”的可靠后台任务方案。

### 9.3 `CountDownManager`

全局倒计时管理器，支持多个监听器，适合列表倒计时。

`stop()` 会停止全局计时任务，多个页面共同使用时需要明确停止所有权。

路径：`app/src/main/java/com/wcl/test/utils/timer/`

---

## 10. Manifest 组件与权限

### 10.1 工程直接声明的权限

```
android.permission.ACCESS_WIFI_STATE
android.permission.ACCESS_NETWORK_STATE
android.permission.INTERNET
android.permission.REQUEST_INSTALL_PACKAGES
```

### 10.2 工程直接注册的组件

- `MainApp`；
- Launcher：`MainActivity`；
- `DialPhoneBroadcastReceiver`；
- `BaseWebViewActivity`；
- 多个测试 Activity；
- `SdkProvider`；
- `CustomTorchFileProvider`。

路径：`app/src/main/AndroidManifest.xml`

`SdkProvider` 是当前实际注册的 ContentProvider，用于 Application `onCreate()` 前的初始化时机验证。

---

## 11. 当前测试与演示页面

| 页面/组件 | 用途 |
|---|---|
| `TestRecyclerViewRefreshActivity` | RecyclerView 与刷新、倒计时等示例 |
| `TestRefreshWithBannerActivity` | 刷新与 Banner 组合示例 |
| `TestFlexBoxActivity` | Flexbox 流式布局示例 |
| `TestGridViewWithHeaderActivity` | NestedScrollView + Header 列表示例 |
| `TestConsecutiveNestScrollActivity` | ConsecutiveScroller 嵌套滚动示例 |
| `TestViewPager2Activity` | ViewPager2 示例 |
| `TestSnapNestViewPager2Activity` | 嵌套滚动 ViewPager2 示例 |
| `TestTabLayoutActivity` | Tab / Fragment 示例 |
| `TestDownloadActivity` | 持久化下载系统示例 |
| `TestWorkManager` | WorkManager Worker 演示类 |

`TestWorkManager` 用于演示 Worker 行为；是否真正入队、约束配置和观察结果应由具体业务代码明确完成，不能仅凭类存在视为已接入后台任务流程。

---

## 12. 当前直接依赖

| 依赖类别 | 主要用途 |
|---|---|
| Material、AppCompat、ConstraintLayout、RecyclerView | Android UI 基础能力 |
| OkHttp、Okio | 网络请求与下载 |
| Glide、Glide OkHttp Integration、Glide Transformations | 图片加载和变换 |
| Gson、GsonFactory | JSON 解析与容错 |
| MMKV | 键值存储 |
| SmartRefreshLayout | 刷新与加载更多 |
| Flexbox | 流式布局 |
| ConsecutiveScroller | 连续嵌套滚动 |
| MagicIndicator | ViewPager2 指示器 |
| WorkManager | 后台任务框架 |
| lifecycle-runtime-ktx、lifecycle-process | 生命周期能力 |
| PercentLayout | 百分比布局兼容能力 |

依赖版本以 `app/build.gradle` 为准。
