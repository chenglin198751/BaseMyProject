# review_uncommitted

## Purpose
review当前已发生改动但是未 git commit 的代码

## Steps
1. git status 获取变更文件
2. git diff 查看未暂存改动
3. git diff --cached 查看已暂存改动
4. 对每个变更文件做 review

## Review 要求
- 必须逐文件 review，不允许跳过文件
- 必须指出具体问题（不要泛泛而谈）
- 如果没有问题，不要强行找问题，说明“未发现明显问题”并给出依据

## Output

- 文件名：
    - 问题：
    - 建议：