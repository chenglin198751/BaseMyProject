# Parking3D 与 Guru SDK 联动测试

> 适用场景：在 Parking3D 中联动验证本地修改的 Unity SDK（`gurusdk-unikit`）和 Android SDK（`GuruSdk-Android`）。

## 一、给人看的快速流程

当前 OfflineLog Channel 联动的核心验证是：本地 Unity 包正确加载、Android AAR 解析到目标版本、APK 中存在两侧代码、真机运行日志出现 Channel 调用。压力测试属于可选项，只有工程里当前存在 Runner 时才执行。

### 1. 准备两个 SDK 仓库

- Unity SDK：[/Users/bjit204/code/gurusdk-unity/gurusdk-unikit](/Users/bjit204/code/gurusdk-unity/gurusdk-unikit)
- Android SDK：[/Users/bjit204/code/GuruSdk-Android](/Users/bjit204/code/GuruSdk-Android)
- Unity 测试工程：[/Users/bjit204/unity_code/Parking3D](/Users/bjit204/unity_code/Parking3D)

先确认 Unity SDK 的本地包：

```text
/Users/bjit204/code/gurusdk-unity/gurusdk-unikit/com.guru.unikit.offlinelog
```

Parking3D 的包链接应指向这个目录：

```text
Packages/com.guru.unikit.offlinelog
 -> /Users/bjit204/code/gurusdk-unity/gurusdk-unikit/com.guru.unikit.offlinelog
```

Unity 刷新后，在 Package Manager 中确认加载的是本地包。

### 2. 发布 Android SDK 到本地 Maven

```bash
cd /Users/bjit204/code/GuruSdk-Android

./gradlew \
  :gurusdk-offlinelog:publishReleasePublicationToMavenLocal \
  -PguruSdkVersion=版本号
```

当前 Android SDK 已将 OfflineLog 与新版 Channel 放在 `gurusdk-offlinelog` 模块中，因此发布该模块即可。Parking3D 的 `settingsTemplate.gradle` 已有 `mavenLocal()`，通常不需要再改仓库配置。

### 3. 刷新 Unity 并构建 APK

1. 等待 Unity 刷新 Package。
2. 确认 Unity Console 没有 C# 编译错误。
3. 确认 Android Resolver 使用了刚发布的本地 Maven 版本。
4. 构建 Android APK。
5. 如果签名失败，检查项目内 `Assets/Plugins/Android/guru_key.jks` 和 Unity Android 签名配置。

### 4. 安装并自动启动真机测试

```bash
adb devices
adb install -r /path/to/Parking3D.apk
adb shell am force-stop parking.jam3d.traffic.jam.games.car.puzzle
adb logcat -c
```

当前项目入口通常是：

```text
com.google.firebase.MessagingUnityPlayerActivity
```

如果已确认当前工程存在对应压力测试 Runner，再使用其实际支持的参数，例如：

```bash
adb shell am start \
  -n parking.jam3d.traffic.jam.games.car.puzzle/com.google.firebase.MessagingUnityPlayerActivity \
  --ez offlineStressTest true \
  --ez offlineStress400k true \
  --es offlineLogMode channel
```

如果确认 Runner 支持旧版/新版模式，旧版通道测试才可改为：

```text
--es offlineLogMode legacy
```

测试由 Runner 自动执行，不需要手动点击。若搜索不到 Runner 或这些参数，跳过本段，先完成普通启动和运行时 Channel 验证。

### 5. 抓取结果并判断是否通过

```bash
adb logcat -d -v threadtime > device-logcat.txt
adb logcat -d | rg \
  'OFFLINE_AB_400K|OfflineLogChannelBridge|GuruOfflineLog|FATAL EXCEPTION|Fatal signal|ANR'
```

重点看：

- `writeLoopMs`：40 万条写入循环耗时
- `sumCallMs`：`LogMessage` 调用累计耗时
- `meanCallUs`：平均单次调用耗时
- C# `csharpAccepted / csharpRejected`
- Android bridge `accepted / dropped`
- 最终日志文件行数和序号连续性
- 是否出现崩溃、Fatal signal 或 ANR

应用日志通常位于：

```text
/data/user/0/parking.jam3d.traffic.jam.games.car.puzzle/files/guru/logs
```

### 6. 性能对比注意事项

旧版和新版必须使用同一台真机、同一个 APK 版本、同一个 Runner、同样的日志数量、同样的让帧设置和同样的诊断日志开关。最好交替运行多轮；不同条件下的结果只能称为参考数据，不能作为严格 A/B 结论。

---

## 二、给 AI 看的详细执行规范

> 当前状态：Parking3D 的 OfflineLog 包链接指向 `gurusdk-unity/gurusdk-unikit`，Android SDK 实际仓库是 `/Users/bjit204/code/GuruSdk-Android`。不要使用已废弃的 `guru_android2` 或独立的旧 `gurusdk-unikit` 路径。


### 目标与仓库角色

- `gurusdk-unikit`：Unity/C# 包，负责 Unity API、Channel 调用、组批和依赖声明。
- `GuruSdk-Android`：Android Kotlin 模块，负责 Channel bridge、Android 日志模块和文件落盘。
- `Parking3D`：集成宿主，负责 Unity Package 链接、Android Resolver、APK 构建、真机启动和结果核验。

除非用户明确要求，不要把 Parking3D 的测试 Runner 代码误当作 SDK 业务代码；Runner 路径必须以当前工程搜索结果为准；先执行：

```bash
rg -n "OFFLINE_AB|offlineStress|400000|offlineLogMode|OfflineLog.*Runner" /Users/bjit204/unity_code/Parking3D/Assets
```

找不到对应脚本时，不得直接使用压力测试启动参数。

### Unity SDK 接入规则

1. 先检查本地包版本、工作区改动和 `Editor/Dependencies.xml`。
2. OfflineLog 包路径必须是：
   `/Users/bjit204/code/gurusdk-unity/gurusdk-unikit/com.guru.unikit.offlinelog`
3. 检查软链接：
   `Parking3D/Packages/com.guru.unikit.offlinelog`
4. `Dependencies.xml` 中声明的 Android Maven 坐标必须与本次发布的 `guruSdkVersion` 完全一致。
5. 不要重复修改 `Assets/Plugins/Android/settingsTemplate.gradle`；当前工程已有 `mavenLocal()`。
6. Unity 刷新后，必须确认 Package Manager 实际加载本地包，而不是旧目录或缓存包。

### Android SDK 发布规则

当前需要同步发布的模块：

```text
gurusdk-offlinelog
```

当前 checkout 的 `settings.gradle.kts` 只包含 `:gurusdk-offlinelog`；该模块同时承载 OfflineLog 与新版 Channel 实现。不要根据旧 checkout 的模块拆分方式执行发布。

优先使用发布任务：

```bash
cd /Users/bjit204/code/GuruSdk-Android
./gradlew \
  :gurusdk-offlinelog:publishReleasePublicationToMavenLocal \
  -PguruSdkVersion=<version>
```

如果需要先验证编译产物，可执行：

```bash
./gradlew \
  :gurusdk-offlinelog:assembleRelease \
  -PguruSdkVersion=<version>
```

`-PguruSdkVersion` 是项目 Gradle 脚本读取的版本属性，不是 Gradle 内置固定参数；实际值必须与 Unity 包依赖声明一致。发布后检查 `~/.m2/repository` 下的 group、artifact、version、AAR 和 POM。

### Unity 构建与签名规则

- 构建前确认 Unity C# 编译完成且 Console 没有错误。
- 若使用自动构建入口，先确认 Editor 方法仍存在且当前工程已加载：
  `CodexOfflineAbBuild.Build`。
- APK 构建失败时，先区分脚本编译错误、Gradle 依赖错误和签名错误。
- 项目签名文件：
  `Parking3D/Assets/Plugins/Android/guru_key.jks`
- 不要把密码写入报告、提交记录或共享文档；只使用项目已有的本地配置。
- 签名错误 `Can not sign the application / please provide passwords` 通常表示 Unity Editor 的 PlayerSettings 未加载密码，需要通过项目已有的 Editor 配置重新应用后再构建。

### 真机启动参数

先获取包名和 Activity，不要假设入口：

```bash
adb shell dumpsys package parking.jam3d.traffic.jam.games.car.puzzle
```

本项目曾使用过以下入口，执行前仍应以 `dumpsys package` 查询结果为准：

```text
parking.jam3d.traffic.jam.games.car.puzzle/com.google.firebase.MessagingUnityPlayerActivity
```

启动参数、数据量、完成标记、帧让出策略和诊断日志行为都必须以当前 Runner 源码为准。只有搜索确认参数和标记存在，才能启动压力测试；不能凭旧文档推断它们仍然有效。

### 结果采集与核验

抓取完整 logcat：

```bash
adb logcat -d -v threadtime > BuildArtifacts/OfflineLogAB/<run>/device-logcat.txt
```

导出应用私有日志：

```bash
adb exec-out run-as \
  parking.jam3d.traffic.jam.games.car.puzzle \
  cat files/guru/logs/<logger-file> > BuildArtifacts/OfflineLogAB/<run>/offline.log
```

解析时至少核对：

1. `attempted == 400000`。
2. `csharpAccepted == 400000` 且 `csharpRejected == 0`。
3. Channel bridge 汇总的 `accepted` 与 `dropped`。
4. 离线文件记录数等于 400,000。
5. 测试序号从 `0` 到 `399999`，无缺失、重复或越界。
6. logcat 中无 `FATAL EXCEPTION`、`Fatal signal`、`ANR in`、`Force finishing`。
7. 记录设备型号、APK 版本、Runner 条件、Run ID 和测试时间。

### 性能解释边界

新版通道的潜在额外开销包括：

- C# 侧按批次取出消息、创建 `GuruValue`、参数对象和数组。
- Channel/protobuf 的编码、传输和 Android bridge 解码。
- Android 侧虽已改为每个 batch 一个写入任务，但任务内部仍逐条写入文件。
- C# 每批 `Debug.Log` 和 Android 每批 `Log.i` 诊断日志；40 万条约 6,250 批，可能增加额外负载。

这些性能开销是可能因素，不等于已被单独证明。性能结论必须来自条件一致的实测；不要把历史实现细节当成当前代码事实。

### APK 进入证据（Android 与 Unity C#）

构建后必须同时检查依赖、Android DEX、Unity IL2CPP 和运行时日志。Android DEX 应包含 `com/guru/sdk/offlinelog/OfflineLog`、`OfflineLogPlugin`、`ChannelRouter` 等类。当前 Parking3D 使用 IL2CPP，Unity C# 应从 APK 的 `lib/arm64-v8a/libil2cpp.so` 和 `assets/bin/Data/Managed/Metadata/global-metadata.dat` 检查 `Guru.UniKit.OfflineLog`、`AndroidChannelOfflineLogger`、`offlinelog.invoke`。静态字符串只能证明代码进入产物；运行时看到 `invoke method=log, class=com.guru.sdk.offlinelog.OfflineLogPlugin` 才能证明实际走通。

```bash
APK=/Users/bjit204/unity_code/Parking3D/BuildOutput/Parking3D-v26.11.0-rc.1.apk
shasum -a 256 "$APK"
rg -n 'com\.guru\.sdk:(offlinelog|unity-channel|connectivity):26\.11\.0-rc\.1' /Users/bjit204/unity_code/Parking3D/Library/Bee/Android/Prj
adb install -r "$APK"
adb logcat -c
adb shell monkey -p parking.jam3d.traffic.jam.games.car.puzzle 1
sleep 8
adb logcat -d -v threadtime | rg -i 'OfflineLog|OfflineLogPlugin|OfflineLogChannel|offlinelog\.invoke|GuruPlugin|ChannelRouter|FATAL EXCEPTION|Fatal signal|ANR'
```

### 跨会话执行清单

AI 接手任务时按此顺序执行：

1. 读取本文件，确认三个绝对路径和当前 Runner 条件。
2. 检查两个 SDK 仓库 `git status`、版本和目标 diff；不要覆盖用户未提交改动。
3. 检查 Unity 包软链接和 `Dependencies.xml` 版本。
4. 在 GuruSdk-Android 发布 `gurusdk-offlinelog` 模块到本地。
5. 刷新 Unity，确认 Package Manager、C# 编译和 Android Resolver 状态。
6. 构建 APK；若签名失败，检查本地 keystore 配置。
7. `adb devices` 确认只有目标真机在线，安装 APK。
8. 清空 logcat，强停并用正确 Activity 携带启动参数冷启动。
9. 如果当前工程确实存在对应 Runner，再等待完成标记并抓取 logcat 和应用日志文件；否则只执行普通运行时链路验证。
10. 逐项核验接受数、拒绝数、落盘行数、序号完整性和崩溃标记。
11. 将原始证据保存在 `Parking3D/BuildArtifacts/OfflineLogAB/<run>/`，报告只写结论、条件和证据路径。
12. 最终明确说明：本轮是否同条件、是否可用于严格新旧版性能结论。
