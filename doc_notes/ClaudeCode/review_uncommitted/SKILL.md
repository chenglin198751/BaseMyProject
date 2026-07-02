---
name: review_uncommitted
description: review 当前已改动但未 git commit 的代码（包括已暂存、未暂存、新建文件）。commit 前的本地代码检查。
---

## 目标
review 当前工作区所有未提交的改动，包括未暂存、已暂存、以及新建（untracked）文件。

## 步骤
1. `git status` 获取全部变更文件（含 untracked）
2. `git diff` 查看未暂存改动
3. `git diff --cached` 查看已暂存改动
4. 对 untracked 新文件，直接读取文件内容 review
5. 直接跳过二进制/资源文件（图片、so、jar 等）
6. 逐个变更文件做 review

## Review 要求
- 只关注 diff 改动的代码是否有 bug（逻辑错误、边界条件、空指针、崩溃风险等），不做风格/优化建议
- 必须逐文件 review，不允许跳过文件
- 必须指出具体问题并标注行号，不要泛泛而谈
- 如果没有问题，说明“未发现明显问题”并给出依据，不要强行找问题

## 输出要求
按严重级别标注：🔴严重 / 🟡建议 / 🟢可选

- 文件名：
    - [级别] 问题（含行号）：
    - 建议：
