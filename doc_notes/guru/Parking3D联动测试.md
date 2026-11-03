# Parking3D 与 Guru SDK 联动测试

> 适用场景：在 Parking3D 中联动验证本地修改的 Unity SDK（`gurusdk-unikit`）和 Android SDK（`GuruSdk-Android`）。

## 一、给人看的快速流程

### 1. 准备两个 SDK 仓库

- Unity SDK：[/Users/bjit204/code/gurusdk-unikit](/Users/bjit204/code/gurusdk-unikit)
- Android SDK：[/Users/bjit204/code/GuruSdk-Android](/Users/bjit204/code/GuruSdk-Android)
- Unity 测试工程：[/Users/bjit204/unity_code/Parking3D](/Users/bjit204/unity_code/Parking3D)

先确认 Unity SDK 的本地包：

```text
/Users/bjit204/code/gurusdk-unikit/com.guru.unikit.offlinelog
```

Parking3D 的包链接应指向这个目录：

```text
Packages/com.guru.unikit.offlinelog
 -> /Users/bjit204/code/gurusdk-unikit/com.guru.unikit.offlinelog
```

Unity 刷新后，在 Package Manager 中确认加载的是本地包。

### 2. 发布 Android SDK 到本地 Maven

```bash
cd /Users/bjit204/code/GuruSdk-Android

./gradlew \
  :gurusdk-offlinelog:publishReleasePublicationToMavenLocal \
  :gurusdk-offlinelog-channel:publishReleasePublicationToMavenLocal \
  -PguruSdkVersion=版本号
```

两个模块都要发布：新版 Channel 对 `gurusdk-offlinelog` 有运行时依赖。Parking3D 的 `settingsTemplate.gradle` 已有 `mavenLocal()`，通常不需要再改仓库配置。

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

40 万条新版通道测试：

```bash
adb shell am start \
  -n parking.jam3d.traffic.jam.games.car.puzzle/com.google.firebase.MessagingUnityPlayerActivity \
  --ez offlineStressTest true \
  --ez offlineStress400k true \
  --es offlineLogMode channel
```

旧版通道测试只需改为：

```text
--es offlineLogMode legacy
```

测试由 Runner 自动执行，不需要手动点击。

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

### 目标与仓库角色

- `gurusdk-unikit`：Unity/C# 包，负责 Unity API、Channel 调用、组批和依赖声明。
- `GuruSdk-Android`：Android Kotlin 模块，负责 Channel bridge、Android 日志模块和文件落盘。
- `Parking3D`：集成宿主，负责 Unity Package 链接、Android Resolver、APK 构建、真机启动和结果核验。

除非用户明确要求，不要把 Parking3D 的测试 Runner 代码误当作 SDK 业务代码；Runner 位于：

```text
/Users/bjit204/unity_code/Parking3D/Assets/Scripts/Diagnostics/OfflineLogAbStressRunner.cs
```

### Unity SDK 接入规则

1. 先检查本地包版本、工作区改动和 `Editor/Dependencies.xml`。
2. OfflineLog 包路径必须是：
   `/Users/bjit204/code/gurusdk-unikit/com.guru.unikit.offlinelog`
3. 检查软链接：
   `Parking3D/Packages/com.guru.unikit.offlinelog`
4. `Dependencies.xml` 中声明的 Android Maven 坐标必须与本次发布的 `guruSdkVersion` 完全一致。
5. 不要重复修改 `Assets/Plugins/Android/settingsTemplate.gradle`；当前工程已有 `mavenLocal()`。
6. Unity 刷新后，必须确认 Package Manager 实际加载本地包，而不是旧目录或缓存包。

### Android SDK 发布规则

当前需要同步发布的模块：

```text
gurusdk-offlinelog
gurusdk-offlinelog-channel
```

原因：`gurusdk-offlinelog-channel` 对 `gurusdk-offlinelog` 使用 `implementation` 依赖，POM 会以 runtime 依赖表达，缺少任一模块都可能导致 Unity Android 构建解析不完整。

优先使用发布任务：

```bash
cd /Users/bjit204/code/GuruSdk-Android
./gradlew \
  :gurusdk-offlinelog:publishReleasePublicationToMavenLocal \
  :gurusdk-offlinelog-channel:publishReleasePublicationToMavenLocal \
  -PguruSdkVersion=<version>
```

如果需要先验证编译产物，可执行：

```bash
./gradlew \
  :gurusdk-offlinelog:assembleRelease \
  :gurusdk-offlinelog-channel:assembleRelease \
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

本项目当前常见入口：

```text
parking.jam3d.traffic.jam.games.car.puzzle/com.google.firebase.MessagingUnityPlayerActivity
```

启动参数含义：

- `offlineStressTest=true`：启用 `OfflineLogAbStressRunner`。
- `offlineStress400k=true`：执行连续 400,000 条测试；否则执行分档速率测试。
- `offlineLogMode=channel`：新版通道。
- `offlineLogMode=legacy`：旧版通道。

Runner 会先创建专用 logger，然后自动执行测试并输出：

```text
[OFFLINE_AB_400K_START]
[OFFLINE_AB_400K_WRITES_DONE]
[OFFLINE_AB_400K_DONE]
```

当前 40 万条 Runner 已移除每 8,192 条 `yield return null` 的 Unity 帧让出。若后续会话修改了这一点，必须在报告和 A/B 结论中明确标注测试条件。

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

不要再描述为“新版每条日志启动一个 Android 协程”，那是批量提交优化前的实现。以上因素是待量化的可能原因，不等于已被单独证明。要量化，需在同一 APK 和同一 Runner 下交替运行，并设置带/不带逐批诊断日志的对照组。

### 跨会话执行清单

AI 接手任务时按此顺序执行：

1. 读取本文件，确认三个绝对路径和当前 Runner 条件。
2. 检查两个 SDK 仓库 `git status`、版本和目标 diff；不要覆盖用户未提交改动。
3. 检查 Unity 包软链接和 `Dependencies.xml` 版本。
4. 在 GuruSdk-Android 发布两个 Maven 模块到本地。
5. 刷新 Unity，确认 Package Manager、C# 编译和 Android Resolver 状态。
6. 构建 APK；若签名失败，检查本地 keystore 配置。
7. `adb devices` 确认只有目标真机在线，安装 APK。
8. 清空 logcat，强停并用正确 Activity 携带启动参数冷启动。
9. 等待 `[OFFLINE_AB_400K_DONE]`，抓取 logcat 和应用日志文件。
10. 逐项核验接受数、拒绝数、落盘行数、序号完整性和崩溃标记。
11. 将原始证据保存在 `Parking3D/BuildArtifacts/OfflineLogAB/<run>/`，报告只写结论、条件和证据路径。
12. 最终明确说明：本轮是否同条件、是否可用于严格新旧版性能结论。
