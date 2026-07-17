---
name: git-strip-big-files
description: 清理 git 历史中体积过大的旧文件，用仓库自带的 BFG 把历史里已删除或旧版本的大 blob 剥掉，缩小 .git 体积
---

# git-strip-big-files

用 BFG 清理 git **历史**中的大文件，缩小 `.git` 体积。BFG 默认保护 HEAD（最新 commit），
所以**当前工作树里还在的文件不会被删**，只清理历史中已删除的、或旧版本的大 blob。

本 skill 目录内自带 `bfg.jar`（约 14MB），无需另外下载。运行需要本机有 Java（`java -version`）。

> **触发方式**：仅在用户**显式调用** `git-strip-big-files` 时才开始工作。用户只是随口提到
> 「.git 太大」「历史大文件」「清理 git 历史」「瘦身仓库」「BFG」等，**不要**自动启动本流程。

## ⚠️ 高风险前置确认（必须先问用户）

重写历史**不可逆**、会改变所有 commit hash、需要 `push --force`，会影响所有协作者。
执行前务必确认：

1. 🧵 **是否已 push 到远程？是否个人仓库 / 有无其他协作者？**
   - 个人仓库：安全，可直接重写。
   - 有协作者：重写后所有人必须重新 clone，需提前通知——先征得同意再做。
2. 📏 **要删多大的文件？必须先问用户阈值**（比如 10MB、100MB），
   **等用户明确回答后再开始工作**，不要自行假设默认值。

## 执行步骤

### 🔍 1. 排查历史大文件（先看清楚要删什么）

```bash
# 列出历史中所有 blob 按体积倒序（前 50）
git rev-list --objects --all \
  | git cat-file --batch-check='%(objecttype) %(objectname) %(objectsize) %(rest)' \
  | awk '/^blob/ {print $3, $4}' | sort -rn | head -50
```

同时看当前还跟踪哪些、`.gitignore` 有没有忽略这些目录，区分「纯历史膨胀」和「现存文件」。

### 📊 2. 记录清理前体积作为基准

```bash
du -sh .git
```

### 💾 3.（可选）备份

```bash
git branch backup-before-bfg
```

### ✂️ 4. 运行 BFG 按体积剥离

`<SKILL_DIR>` 替换为本 skill 所在目录（bfg.jar 与本文件同级）。
`<阈值>` 替换为**用户确认过的**大小（如 `10M`、`100M`）。

```bash
java -jar "<SKILL_DIR>/bfg.jar" --strip-blobs-bigger-than <阈值> .git
```

其他常用姿势：
- 按文件名删：`--delete-files "文件名"`
- 按文件夹删：`--delete-folders 目录名`
- 删敏感信息：`--replace-text passwords.txt`

### ✅ 5. 关键：跑 gc 之前先验证 HEAD 未被误删

BFG 的删除报告里可能出现「现存文件」的名字——那通常是它的**旧版本 blob**，
HEAD 版本受保护仍在。**必须验证后再 gc**：

```bash
# 确认现存大文件仍在 HEAD
git ls-tree -r -l HEAD | grep -E '你关心的文件名'
# 确认工作树未提交改动还在
git status --short
```

若 HEAD 里目标文件仍在、blob 被 HEAD 引用，则 gc 不会回收它，符合「不删现存文件」。

### 🧹 6. 回收空间

```bash
git reflog expire --expire=now --all && git gc --prune=now --aggressive
du -sh .git   # 对比清理效果
```

### 🚀 7. force push（交给用户执行，Claude 不代劳）

历史已被重写，本地清理完毕，但远程仍是旧的。**操作完成后，最后一步务必提醒用户手动执行**：

```bash
git push --force origin master
```

个人仓库安全；有协作者务必先通知。

### 🧾 8. 收尾

- 删除 BFG 日志目录：`rm -rf .git.bfg-report`
- 若想根治（阻止目录再次进历史）：`git rm -r --cached <dir>` + 写入 `.gitignore`。
  注意这会改变文件的跟踪状态，属于「动现存文件」，需另行征得用户同意。

## 注意事项

- **绝不自动 `push --force`**：始终留给用户手动执行，并提示 `git push --force origin master`。
- **gc 前必须验证 HEAD**：避免误判现存文件被删。
- **阈值必须先问用户**：删多大的文件由用户决定，确认后再动手。
- BFG 不会碰工作树的未提交改动，可放心。
- 遵循本项目「证据优先、不确定先问」原则：范围/阈值/协作者情况没确认清楚前不动手。
