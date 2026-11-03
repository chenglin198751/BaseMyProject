# 新旧版 Channel 压测方法

> 目的：在 Parking3D 真机上，对比旧版通道（Legacy）和新版通道（Channel）的连续写入性能、接受情况和最终落盘完整性。

## 一、给人看的快速流程

### 1. 准备环境

- Unity 工程：[/Users/bjit204/unity_code/Parking3D](/Users/bjit204/unity_code/Parking3D)
- Unity SDK：[/Users/bjit204/code/gurusdk-unikit](/Users/bjit204/code/gurusdk-unikit)
- Android SDK：[/Users/bjit204/code/GuruSdk-Android](/Users/bjit204/code/GuruSdk-Android)
- 真机连接检查：

```bash
adb devices
```

### 2. 发布新版 Android 模块

```bash
cd /Users/bjit204/code/GuruSdk-Android

./gradlew \
  :gurusdk-offlinelog:publishReleasePublicationToMavenLocal \
  :gurusdk-offlinelog-channel:publishReleasePublicationToMavenLocal \
  -PguruSdkVersion=26.11.0-rc.1
```

两个模块都必须发布，因为 `offlinelog-channel` 运行时依赖 `offlinelog`。

### 3. 准备 Unity 压测 Runner

压测 Runner 需要自动完成以下工作：

1. 创建专用 logger。
2. 连续调用 `OfflineLog.LogMessage` 400,000 次。
3. 记录写入循环总耗时。
4. 记录 `LogMessage` 调用累计耗时和平均单次耗时。
5. 等待异步 Channel 写入完成。
6. 输出唯一 Run ID，便于筛选日志。

新版通道和旧版通道必须使用同一份 Runner、同样的 400,000 条数据和同样的让帧设置。

### 4. 构建和安装 APK

```bash
adb install -r /path/to/Parking3D.apk
adb shell am force-stop parking.jam3d.traffic.jam.games.car.puzzle
adb logcat -c
```

当前 Parking3D 常用入口：

```text
parking.jam3d.traffic.jam.games.car.puzzle/com.google.firebase.MessagingUnityPlayerActivity
```

### 5. 启动新版通道压测

```bash
adb shell am start \
  -n parking.jam3d.traffic.jam.games.car.puzzle/com.google.firebase.MessagingUnityPlayerActivity \
  --ez offlineStressTest true \
  --ez offlineStress400k true \
  --es offlineLogMode channel
```

### 6. 启动旧版通道压测

先准备使用旧版 Android 调用实现的同一 APK 或测试分支，再执行：

```bash
adb shell am force-stop parking.jam3d.traffic.jam.games.car.puzzle
adb logcat -c
adb shell am start \
  -n parking.jam3d.traffic.jam.games.car.puzzle/com.google.firebase.MessagingUnityPlayerActivity \
  --ez offlineStressTest true \
  --ez offlineStress400k true \
  --es offlineLogMode legacy
```

正式 SDK 当前默认使用新版 Channel；`offlineLogMode=legacy` 只有在临时 A/B Runner 或测试分支保留切换代码时才有效，不能直接假设正式包支持该参数。

### 7. 抓取和核对结果

```bash
adb logcat -d -v threadtime > device-logcat.txt
adb logcat -d | rg \
  'OFFLINE_AB_400K_(START|WRITES_DONE|DONE)|OfflineLogChannelBridge|GuruOfflineLog|FATAL EXCEPTION|Fatal signal|ANR'
```

从应用私有目录导出对应 logger 文件：

```bash
adb shell run-as parking.jam3d.traffic.jam.games.car.puzzle \
  find files/guru/logs -maxdepth 1 -type f

adb exec-out run-as parking.jam3d.traffic.jam.games.car.puzzle \
  cat files/guru/logs/<logger-file> > offline.log
```

### 8. 需要记录的指标

| 指标 | 含义 |
| --- | --- |
| `writeLoopMs` | 400,000 条连续调用的总耗时 |
| `sumCallMs` | 每次 `LogMessage` 调用耗时的累计值 |
| `meanCallUs` | 平均单次调用耗时 |
| `callsPerSec` | 写入循环平均调用速率 |
| `csharpAccepted` | C# 侧接受的消息数 |
| `csharpRejected` | C# 侧拒绝的消息数 |
| `accepted / dropped` | Android bridge 批量处理结果 |
| 文件行数 | 最终落盘记录数 |

通过标准：两种模式都应接受 400,000 条、拒绝 0 条、最终落盘 400,000 行，序号从 0 到 399,999 连续，无重复、缺失或越界，并且没有崩溃和 ANR。

### 9. 结果解释

只有在同一台真机、同一 APK 基线、同一 Runner、同样的让帧策略、同样的诊断日志开关下交替跑多轮，才能把结果作为严格 A/B 性能结论。旧版带帧让出、最新版无帧让出的数据只能作为参考，不能直接计算性能差异。

---

## 二、给 AI 看的详细执行规范

### 目标

AI 需要完成一条可复现的联动压测链路：

```text
本地 Unity SDK + 本地 Android SDK
        ↓
本地 Maven 发布 + Unity 包刷新
        ↓
Unity 编译与 APK 构建
        ↓
ADB 安装与冷启动
        ↓
Legacy / Channel 自动写入 400,000 条
        ↓
logcat + 应用私有日志文件
        ↓
指标解析、完整性校验、对比报告
```

### 代码和仓库事实

- 正式 Unity OfflineLog 入口：
  `/Users/bjit204/code/gurusdk-unikit/com.guru.unikit.offlinelog/Runtime/OfflineLog.cs`
- 正式新版实现：
  `/Users/bjit204/code/gurusdk-unikit/com.guru.unikit.offlinelog/Runtime/AndroidChannelOfflineLogger.cs`
- Android bridge：
  `/Users/bjit204/code/GuruSdk-Android/gurusdk-offlinelog-channel/src/main/java/com/guru/sdk/offlinelog/channel/OfflineLogChannelBridge.kt`
- Android 落盘实现：
  `/Users/bjit204/code/GuruSdk-Android/gurusdk-offlinelog/src/main/java/com/guru/sdk/core/foundation/log/OfflineLog.kt`
- Unity 依赖声明：
  `/Users/bjit204/code/gurusdk-unikit/com.guru.unikit.offlinelog/Editor/Dependencies.xml`
- Unity 包链接应指向：
  `/Users/bjit204/unity_code/Parking3D/Packages/com.guru.unikit.offlinelog -> /Users/bjit204/code/gurusdk-unikit/com.guru.unikit.offlinelog`

历史压测 Runner 和构建辅助文件已从 Parking3D 清理。后续需要压测时，必须在临时测试分支或独立测试目录恢复 Runner，不要把 A/B 参数、40 万条循环和自动构建入口提交到正式 SDK 或宿主业务代码。

### 旧版 / 新版选择规则

- 新版：`OfflineLog.cs` 在 Android 下实例化 `AndroidChannelOfflineLogger`。
- 旧版：临时测试构建中实例化原有 `AndroidOfflineLogger`，它通过 `AndroidJavaObject` 逐条调用 Android 类。
- `offlineLogMode` 不是正式 SDK 的稳定 API。只有测试 Runner 显式读取该 Intent extra 并选择实现时，`channel` / `legacy` 参数才有意义。
- 不能仅给正式 APK 传 `offlineLogMode=legacy` 就声称跑了旧版；必须先确认 APK 内确实包含 Legacy 分支。

### 构建前检查

1. 检查两个 SDK 的 `git status`、当前版本和目标 diff。
2. 使用 Android SDK 仓库内的 codegraph：

```bash
cd /Users/bjit204/code/GuruSdk-Android
/Users/bjit204/.local/bin/codegraph sync .
```

3. 用 codegraph 查询 bridge、OfflineLog 和调用者；若工具不可用，再降级使用 `rg`。
4. 确认 `Dependencies.xml` 的版本与 `-PguruSdkVersion` 完全一致。
5. 发布 `gurusdk-offlinelog` 和 `gurusdk-offlinelog-channel` 两个 Maven 模块。
6. Unity 刷新包并确认 C# 编译通过。
7. 构建 APK。签名失败时检查项目已有 keystore 配置，不要把密码写入文档或提交。

### Runner 要求

Runner 至少应输出以下三条机器可解析日志：

```text
[OFFLINE_AB_400K_START] runId=<id> mode=<legacy|channel> target=400000
[OFFLINE_AB_400K_WRITES_DONE] runId=<id> mode=<mode> target=400000 attempted=<n> csharpAccepted=<n> csharpRejected=<n> writeLoopMs=<ms> meanCallUs=<us> sumCallMs=<ms> callsPerSec=<rate>
[OFFLINE_AB_400K_DONE] runId=<id> mode=<mode> target=400000 attempted=<n> csharpAccepted=<n> csharpRejected=<n> logRoot=<path>
```

Runner 是否使用 `yield return null` 必须记录在测试元数据中。删除帧让出后，写入循环会更接近连续 API 调用，但会更集中地占用 Unity 主线程；带帧让出和无帧让出不能混合比较。

### 真机启动和采集

1. `adb devices` 必须确认目标设备在线。
2. 安装 APK 后先 `force-stop`，再清空 logcat。
3. 使用 `dumpsys package <package>` 确认真实 Activity，不要假设一定是 `UnityPlayerActivity`。
4. 每轮生成独立 Run ID 和独立 logger 名称，避免读取到上一轮文件。
5. 等待 `[OFFLINE_AB_400K_DONE]` 后再抓取应用日志；新版 Channel 还要保留足够时间让 Android 写线程排空。
6. 原始证据放在：

```text
Parking3D/BuildArtifacts/OfflineLogAB/<run>/
```

建议保存：

```text
device-logcat.txt
offline.log
run-metadata.txt
analysis.md
```

### 自动校验脚本逻辑

对每个 run：

1. 从 `OFFLINE_AB_400K_WRITES_DONE` 解析耗时和接受数。
2. 从 logger 文件逐行解析测试序号。
3. 验证记录数为 400,000。
4. 验证最小序号为 0，最大序号为 399,999，集合大小为 400,000。
5. 验证不存在重复、缺失和越界序号。
6. 汇总 bridge 的 `accepted`、`dropped`；注意 logcat 采集可能漏掉部分回执日志，不能用回执行数替代文件完整性。
7. 搜索 `FATAL EXCEPTION`、`Fatal signal`、`ANR in`、`Force finishing`。
8. 输出循环耗时、API 累计耗时、平均调用耗时和完整性结论。

### 性能结论边界

新版额外成本可能来自：

- C# 组批、`GuruValue` 对象创建和数组构造。
- Channel/protobuf 编码、跨 native 边界传输和 Android bridge 解码。
- Android bridge 每批调度一个写入任务，但任务内部仍逐条写文件。
- C# 与 Android 的逐批诊断日志。

当前实现已不是“每条日志启动一个 Android 协程”。这类旧描述不能写进新报告。以上是可能的开销来源，除非完成关闭诊断日志、统一 Runner 和多轮交替测试，否则不能把某一项写成已证明的唯一瓶颈。

### 跨会话执行清单

1. 读取本文件并确认当前 Runner 是否存在。
2. 检查 `gurusdk-unikit`、`GuruSdk-Android` 和 Parking3D 的工作区状态。
3. 同步 codegraph，查询 Android bridge 和 OfflineLog 调用链。
4. 确认本地包软链接和 Maven 版本一致。
5. 发布两个 Android 模块到 `mavenLocal`。
6. 在临时测试分支恢复或生成 Runner；记录是否让出 Unity 帧。
7. 构建并安装 APK。
8. 分别以 `legacy` 和 `channel` 冷启动，交替执行多轮 400,000 条测试。
9. 采集 logcat、应用私有日志和运行元数据。
10. 自动核验计数、耗时、序号、落盘和崩溃标记。
11. 只有同条件多轮结果才写严格 A/B 结论；否则标记为参考数据。
12. 测试完成后删除 Runner、测试构建脚本、测试日志和 BuildArtifacts 压测产物，不把测试代码带入正式提交。
