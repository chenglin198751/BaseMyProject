---
name: git_rebase
description: 把当前分支 rebase 到上游最新，冲突由 AI 合并，成功后在本地停下，不自动 push。
---

不自动 push；判断不了就回滚，不猜取舍。

## 1 准备（任一异常即停止）

```bash
g=$(git rev-parse --git-dir)
ls -d "$g"/rebase-merge "$g"/rebase-apply "$g"/MERGE_HEAD "$g"/CHERRY_PICK_HEAD 2>/dev/null || true  # 有输出=中间态
git branch --show-current   # 空=detached HEAD
git status --porcelain      # 有输出=不干净：git stash / git stash -u
PRE=$(git rev-parse HEAD 2>/dev/null) || { echo "无提交，无需 rebase"; exit 0; }
```

## 2 上游

```bash
b=$(git branch --show-current)
r=$(git config "branch.$b.remote" 2>/dev/null || echo origin)
u=$(git rev-parse --abbrev-ref --symbolic-full-name '@{u}' 2>/dev/null || echo "$r/$b")
git fetch "$r" || { echo "fetch 失败"; exit 1; }
```

别写死 `origin`，fork 工作流上游常是 `upstream`。

## 3 rebase

```bash
git ls-remote --heads "$r" "$b" || { echo 远端不可达; exit 1; }
git rebase "$u"
```

`ls-remote` 输出为空 = 远端无此分支，停；不能改用 `origin/$b` 判断（单分支克隆会缺）。`rebase` 非零退出且未解决列表为空 = 非冲突失败（upstream 无效、脏树、hook 等），报原因并停（已有 rebase 状态先 abort）；有未解决文件才进第 4 节。无冲突也要走第 5 节判断是否重写。

## 4 冲突

```bash
git diff --name-only --diff-filter=U
```

`UU` 双方改、`AA` 双方新增、`DU`/`UD` 删改、`DD` 双方删，都要处理；非 `UU` 且取舍不明 → 中止。

方向别搞反，rebase 的 `HEAD` 一侧是远端：

| 标记 | 含义 |
| --- | --- |
| `<<<<<<< HEAD` 到 `=======` | 远端（上游） |
| `=======` 到 `>>>>>>> <hash>` | 本地（被重放的 commit） |

拿不准就用 `git show ":2:<文件>"`（远端）/ `":3:<文件>"`（本地）核对。

某一侧 `:2:` / `:3:` 不存在，说明该侧删除了文件，按删除处理（`git rm`），不要写内容。

合并：改动不重叠 → 全留；同行或同逻辑块、判断不了 → 中止。写完不留标记，`git add -- "<文件>"` 后复查：

```bash
rg -n '^(<{7}|={7}|>{7})' -- "<文件>"   # 有输出=没合干净
```

### 中止

**先记录再 abort**，否则现场就没了：

```bash
git show ":2:<文件>"; git show ":3:<文件>"   # 远端 / 本地
git rebase --abort
```

输出：文件与位置、远端改动、本地改动、建议；说明已解决的其它冲突也一并回滚。

### 继续

```bash
git diff --name-only --diff-filter=U   # 必须为空
GIT_EDITOR=true git rebase --continue
```

新冲突 → 回第 4 节；commit 变空 → `git rebase --skip`；漏了 `git add` → 补上；其它失败 → abort 并报原因。

## 5 完成

```bash
p=$(git config "branch.$b.pushRemote" 2>/dev/null || git config remote.pushDefault 2>/dev/null || echo origin)
git rev-list --count "$u..HEAD"
git merge-base --is-ancestor "$PRE" HEAD && echo 未重写 || echo 已重写   # PRE 来自第 1 步
```

推送目标是 `$p`，不是 fetch 用的 `$r`（fork 工作流里 `$r` 可能是 `upstream`）。已重写 → `git push "$p" "$b" --force-with-lease`；未重写 → `git push "$p" "$b"`；领先 0 → 不推。只给建议并写成具体命令，不执行 push。

输出：当前分支、上游、领先数、是否重写、推送命令；自动合并的冲突逐条列出文件/行号、远端改动、本地改动、合并结果、理由。
