# 客诉后台 MCP 服务（complaint-backend）

> 客诉后台对外提供的 MCP 接口。Claude 通过 `mcp__complaint-backend__*` 工具调用拉取客诉单、回填修复状态。

## 服务信息

- **server 名**：`complaint-backend`
- **连接地址**：`https://complaint.qh-safe.com/mcp`
- **用途**：拉取客服加工后的客诉单、回填修复状态。

## 连接配置

连接地址配置在项目根目录 `.mcp.json`，Claude Code 启动时自动加载：

```json
{
  "mcpServers": {
    "complaint-backend": {
      "type": "http",
      "url": "https://complaint.qh-safe.com/mcp",
      "headers": {
        "Authorization": "Bearer <token>"
      }
    }
  }
}
```

本 Skill 目录下附了一份 `../.mcp.json`，移植 Skill 时一并复制到项目根目录。

## 工具清单

### 1. get_complaints

拉取客诉单列表。

参数：

```json
{ "status": "pending" }
```

`status` 可选值：`pending`（待处理）/ `fixing`（修复中）/ `fixed`（已修复）/ `blocked`（阻塞）。

返回：

```json
[
  {
    "id": "CP-20260903-001",
    "title": "热榜页点击闪退",
    "page": "游戏热榜页",
    "description": "用户反馈进热榜页就闪退",
    "expect": "正常展示热榜列表",
    "actual": "进入页面即崩溃",
    "reproduce_steps": "主页 -> 点击游戏热榜入口"
  }
]
```

### 2. update_complaint_status

更新客诉单状态。

```json
{ "id": "CP-20260903-001", "status": "fixed" }
```

### 3. post_fix_comment

回填修复说明。

```json
{ "id": "CP-20260903-001", "comment": "根因：... / 改动：... / 验证：..." }
```

## 调用形式对照

| 逻辑动作 | 工具调用标识 |
|---------|-------------|
| 拉取待处理客诉 | `mcp__complaint-backend__get_complaints` |
| 标记修复中 / 已修复 / 阻塞 | `mcp__complaint-backend__update_complaint_status` |
| 回填修复说明 | `mcp__complaint-backend__post_fix_comment` |

## 与 Skill 的约定

- 客诉单的 `page` 字段必须使用 `references/page_mapping.md` 中的「标准页面名」。
- 无法定位页面时标 `blocked`，能修复时标 `fixed` 并附 `post_fix_comment` 修复说明。