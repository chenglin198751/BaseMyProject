---
name: complaint-auto-fix
description: 客诉 bug 自动修复闭环。当用户要求"修复客诉/处理客诉单/拉取客诉并修复"，或提到"客诉后台、客诉 bug、客服反馈的 bug"时使用。通过 MCP 连接客诉后台拉取客服加工后的中文 bug 描述，按页面映射表定位到具体 Activity/代码，遵循"证据优先"原则自动加日志、构建安装、ADB 复现、logcat 收集日志、定位根因并修复，回填客诉状态，循环直到问题被完全修复为止。
---

# 客诉 Bug 自动修复闭环

## 目标

把「客服加工过的中文客诉」变成「已修复并回填状态的 bug 单」，全流程由 AI 闭环完成：
拉取客诉 → 定位页面 → 读代码初判 → 加日志 → 构建安装 → ADB 复现 → 收集日志 → 定位根因 → 修复 → 验证 → 回填状态 → 循环。

## 前置资源

- **页面映射表**：`references/page_mapping.md`，客服按此表把模糊描述归一成中文页面名（如「首页」→「主页」）。
- **MCP 客诉后台**：server 名 `complaint-backend`，工具定义见 `references/mcp_complaint_backend.md`。

---

## 标准流程（每一步都要执行，不要跳步）

### 第 1 步：拉取客诉单

调用 MCP 拉取待处理的客诉：

```
mcp__complaint-backend__get_complaints(status="pending")
```

拿到每条的字段：`id`、`title`、`page`（客服归一后的中文页面名）、`description`、`expect`（预期表现）、`actual`（实际表现）、`reproduce_steps`（复现步骤）。

### 第 2 步：定位页面与代码

1. 用 `page` 字段查 `references/page_mapping.md`，得到 Activity 类名与包路径。
2. 找不到映射时**停下来**，用 `update_complaint_status(id, status="blocked")` 标注无法定位，不要瞎猜页面。

### 第 3 步：读代码初判

阅读对应 Activity 及其布局/逻辑代码，对照 `expect` 与 `actual` 尝试定位可疑点。

- **能确定根因** → 直接进入第 8 步修复。
- **不能确定** → 严格遵循「证据优先，推测靠后」，进入第 4 步加日志取证。

### 第 4 步：加日志（证据优先）

在可疑链路的关键节点补日志。遵守项目日志开关规范（`EnvToggle` / `ToggleSettings`），用统一 TAG：

```java
private static final String TAG = "ComplaintDebug";

// 关键入口
Log.d(TAG, "onCreate: intent=" + getIntent().getData());

// 关键分支 / 数据来源
Log.d(TAG, "loadList: page=" + page + ", size=" + list.size());

// 异常点
Log.e(TAG, "parse error", e);
```

> 原则：原因不明时**禁止**直接改业务逻辑，先埋日志，用日志说话。

### 第 5 步：构建 + 安装

```bash
# 构建 Debug 包
bash gradlew assembleDebug

# 安装（包名与 apk 路径按项目实际调整）
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 第 6 步：ADB 复现（自动点击跳转）

按 `reproduce_steps` 操作页面，常用命令骨架：

```bash
# 启动指定 Activity（显式启动，绕过首页，直达目标页面）
adb shell am start -n <packageName>/<Activity全限定类名>

# 拉取当前界面控件树，定位目标控件坐标
adb shell uiautomator dump /sdcard/window_dump.xml
adb shell cat /sdcard/window_dump.xml
# 从 dump 结果中读出目标控件的 bounds="[x1,y1][x2,y2]"，取中心点 (x,y)

# 点击中心点
adb shell input tap <x> <y>

# 滑动 / 输入文本
adb shell input swipe <x1> <y1> <x2> <y2> <ms>
adb shell input text "<text>"

# 截图留证（复现前后各一张）
adb exec-out screencap -p > before.png
```

复现失败时要先确认「是否真的走到了目标页面」，必要时回第 4 步继续补日志。

### 第 7 步：收集日志

```bash
# 先清空历史日志，避免被旧日志干扰
adb logcat -c

# 触发复现动作后，按 TAG 过滤抓取
adb logcat -v time -s ComplaintDebug:D *:E
```

- 用 `-s ComplaintDebug:D *:E` 同时抓本 TAG 的 Debug 和全局 Error。
- 把日志按时间轴对齐 `reproduce_steps`，确认每一跳、每一个数据来源的实际值。

### 第 8 步：定位根因并修复

基于日志证据定位根因，修改代码。修改时：

- 只改必要的最小范围，不顺手重构。
- 保留原有注释（除非注释本身有误）。
- 修复后补一句说明性注释（可选）。

### 第 9 步：重新构建验证

回到第 5、6、7 步，重新构建安装 → 复现 → 抓日志，确认 `actual` 已变为 `expect`，且无新报错。

### 第 10 步：回填状态

```bash
# 确认修复后关闭客诉单
mcp__complaint-backend__update_complaint_status(id, status="fixed")

# 附上修复说明（根因 + 改动点 + 验证结果）
mcp__complaint-backend__post_fix_comment(id, comment="<根因> / <改动> / <验证>")
```

### 第 11 步：循环

`get_complaints` 里还有下一条 `pending` 单，则回到第 1 步继续；否则报告「本轮客诉已全部处理」。

---

## 终止与失败条件

| 情况 | 处理 |
|------|------|
| 页面名在映射表找不到 | 标 `blocked`，注明「缺少页面映射」，人工补充映射表后重跑 |
| 复现步骤无法在设备执行 | 标 `blocked`，附截图与已收集日志，说明卡点 |
| 加日志后仍无法定位 | **继续补日志**，禁止反复猜测修改；确实无解则标 `blocked` 并附全量日志 |
| 构建失败 | 记录编译错误，先修复编译问题再继续 |

> 核心原则：**证据优先，推测靠后**。每次修改都必须有日志/日志分析作为依据。
