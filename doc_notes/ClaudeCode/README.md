# 1. Claude Code 安装：

1. 查看最新版本：npm view @anthropic-ai/claude-code version
2. 首次安装：npm install -g @anthropic-ai/claude-code
3. 升级：npm install -g @anthropic-ai/claude-code@latest
4. 安装claude.exe：claude install

# 2. 常用命令

1. /init：初始化整个工程，生成工程级CLAUDE.MD
2. /resume：恢复之前的对话
3. /review：审查未提交的代码
4. /refactor：自定义用来重构代码的skills
5. /model：切换模型
6. /config：全局配置，比如Language是切换默认语言为中文
7. /clear：清空context上下文
8. /export：导出当前对话
9. /review git diff：基于本地改动（git未commit）做代码审查
10.
    - /code-review：风险导向（防炸）——代码是否有 bug、边界问题、异常风险或潜在隐患
    - /simplify：结构导向（变干净）——减少复杂度、去冗余、提升可读性与表达清晰度
    - 总结：code-review 看“会不会错”，simplify 看“写得好不好”。两者互补