---
name: git_rebase
description: 自动执行 git fetch + git rebase，遇到冲突由 Claude 智能合并，合并成功后停在本地等待人工确认，不自动 push。
---

## 目标

从远程拉取最新代码并 rebase 到当前分支，遇到冲突智能合并，保持提交历史线性干净。

## 执行步骤

### 第一步：准备检查

1. `git status` 确认工作区是否干净

- 有未提交的改动 → 停止执行，提示用户先 commit 或 stash，不继续往下走
- 工作区干净 → 继续

2. 记录当前分支名和 HEAD commit（用于失败时回滚参考）
   git branch --show-current
   git rev-parse HEAD

### 第二步：fetch 远程最新代码

git fetch origin

- 失败（网络问题等）→ 报告错误原因，停止执行

### 第三步：执行 rebase

先确认远程分支存在：若 `origin/<当前分支名>` 不存在（本地新建、从未 push 过的分支），说明无需 rebase，提示"远程无此分支"并停止。

git rebase origin/<当前分支名>

- 无冲突，rebase 成功 → 直接跳到第五步
- 有冲突 → 进入第四步冲突处理流程

### 第四步：冲突处理

#### 4.1 获取冲突文件列表

git status

找出所有标记为 `UU`（both modified）的文件。

#### 4.2 逐文件智能合并

对每个冲突文件：

1. `Read` 当前文件完整内容（包含 `<<<<<<<` `=======` `>>>>>>>` 冲突标记）
2. 分析冲突：

- `<<<<<<< HEAD` 和 `=======` 之间 = 本地改动
- `=======` 和 `>>>>>>> origin/xxx` 之间 = 远程改动

3. 合并原则：

- 两边改动物理不重叠（如各自新增不同 import、不同方法）→ 两边都保留
- 两边改动同一行 / 同一逻辑块 → 不自行取舍，进入 4.3 交人工处理
- 无法判断 → 标记为待人工确认，不强行合并，进入 4.3 失败流程

4. 写入合并后的完整文件内容（不能留任何冲突标记）
5. `git add <文件名>` 标记为已解决

#### 4.3 冲突过于复杂无法自动合并

满足以下任一条件时，判定为无法自动合并：

- 冲突涉及复杂业务逻辑，语义无法判断
- 同一区域两边改动差异过大
- 合并后代码逻辑存疑

处理方式：
git rebase --abort

回滚到 rebase 前的状态，并输出：

- 哪个文件哪个位置冲突无法自动解决
- 本地改动内容是什么
- 远程改动内容是什么
- 建议人工如何处理

#### 4.4 所有冲突解决后

GIT_EDITOR=true git rebase --continue

（`GIT_EDITOR=true` 用于禁用编辑器弹窗，避免 rebase 重放 commit 时卡在 commit message 编辑界面）

- 成功 → 进入第五步
- 还有新的冲突 → 重复 4.2 流程
- 失败 → 执行 `git rebase --abort`，报告失败原因

### 第五步：完成，等待人工确认

输出 rebase 结果摘要，不自动 push：
✅ rebase 完成，请确认后手动执行：
git push origin <当前分支名>
如需强制推送（rebase 后通常需要）：
git push origin <当前分支名> --force-with-lease

## 注意事项

- 全程不执行 `git push`，由人工决定是否推送
- rebase 失败一律执行 `git rebase --abort` 回到操作前状态，不留在中间态
- 每个冲突文件合并后必须重新 Read 确认没有残留冲突标记（`<<<<<<<` `=======` `>>>>>>>`）
- 合并理由必须说明，不能静默修改代码

## 输出格式

### 无冲突时

✅ rebase 完成，无冲突。
当前分支：<分支名>
领先远程：<N> 个 commit
请确认后执行：git push origin <分支名>
（仅当本地 commit 被 rebase 重写、hash 变化时才需要 --force-with-lease；纯 fast-forward 拉取无需 force）

### 有冲突且自动合并成功时

⚠️ 发现冲突，已自动合并：
📄 <文件名>

冲突位置：L42~L58
本地改动：xxx
远程改动：xxx
合并结果：xxx
合并理由：xxx

✅ 所有冲突已解决，rebase 完成。
请 review 以上合并内容后执行：git push origin <分支名> --force-with-lease

### 冲突无法自动合并时

❌ rebase 已中止（git rebase --abort 已执行）
以下冲突无法自动合并，需人工处理：
📄 <文件名> L<行号>

本地改动：xxx
远程改动：xxx
建议：xxx

请手动解决后重新执行 /git_rebase