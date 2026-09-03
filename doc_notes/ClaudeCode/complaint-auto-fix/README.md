# complaint-auto-fix —— 客诉 Bug 自动修复 Skill

> 通过 MCP 连接客诉后台，自动闭环修复客诉 bug：拉取客诉 → 定位页面 → 加日志 → 复现 → 收集日志 → 修复 → 回填状态，循环直到完全修复。
> 团队拿到后，替换为自己的客诉后台 MCP、页面映射与包名即可投产。

## 它实现了什么

一条龙闭环，无需人工介入：

```
拉取客诉 → 定位页面 → 读代码初判 → 加日志(证据优先) → 构建安装
   → ADB 复现 → logcat 收集日志 → 定位根因 → 修复 → 验证 → 回填状态 → 循环
```

核心设计点：**客服按「页面映射表」把用户模糊描述归一成中文页面名**，AI 据此定位到具体 Activity/代码，全流程因此可自动化。

## 目录结构

```
complaint-auto-fix/
├── SKILL.md                              # 主指令（Claude 执行的完整流程）
├── .mcp.json                             # MCP 连接配置（complaint-backend 地址）
└── references/
    ├── page_mapping.md                   # 中文页面名 → Activity 映射表（客服加工依据）
    └── mcp_complaint_backend.md          # 客诉后台 MCP 工具说明
```

## 怎么用它

### 1. 直接看

先读 `SKILL.md`，再读 `references/` 两个文件，理解「流程 + 映射表 + MCP」三块如何咬合。

### 2. 移植到 skills 目录

放到 Claude Code 能识别的 skills 目录即可触发：

```bash
# 项目级（跟随仓库，全团队可用）
cp -r doc_notes/ClaudeCode/complaint-auto-fix .claude/skills/
```

同时把 `.mcp.json` 复制到项目根目录，MCP 才会被加载：

```bash
cp doc_notes/ClaudeCode/complaint-auto-fix/.mcp.json .mcp.json
```

之后在对话里说「修复客诉 CP-20260903-001」或「处理待处理的客诉单」，即可触发。

### 3. 接入你自己的系统（三步替换）

| 要接的东西 | 改哪里 |
|-----------|--------|
| 你的 MCP server 名 + 地址 + 工具 | `.mcp.json` 与 `references/mcp_complaint_backend.md`、`SKILL.md` 里的 `mcp__complaint-backend__*` |
| 你的客诉字段结构 | `SKILL.md` 第 1 步、`references/mcp_complaint_backend.md` 返回示例 |
| 你的页面与包路径 | `references/page_mapping.md` 映射表 |

## 为什么这么设计（给新同学）

1. **页面映射表是自动化前提** —— 没有它，AI 面对「首页」这种模糊词只能猜；有了它，客服负责归一、AI 负责定位，各司其职。
2. **「证据优先，推测靠后」贯穿始终** —— 原因不明先加日志、抓日志、再改，禁止拍脑袋改代码。这与项目 `CLAUDE.md` 的 Bug 排查原则一致。
3. **显式启动 + uiautomator 定位坐标** —— 用 `am start` 直达页面、用 `uiautomator dump` 找控件中心点，是「自动点击跳转」最通用、最不依赖具体 UI 框架的骨架。
4. **状态闭环** —— 每条单最终都落到 `fixed` / `blocked`，保证客诉系统状态可追溯，不会漏单、不会悬空。