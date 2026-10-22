---
name: html_doc_design
description: 把技术内容做成"明快卡片式"单页 HTML——高饱和配色、卡片与表格驱动、适合讲给同事或投屏演示。用于迁移手册、方案对比、技术分析、复盘报告等需要"好看"的网页文档。
---

# 明快卡片式文档设计

一套已验证的设计系统：把 Markdown 里的技术内容，重排成一个可以在浏览器里讲给别人看的单页 HTML。
本文档自包含：样式和骨架都在文末，复制即可用。

## 什么时候用

**用**：要给同事讲的技术文档、迁移/升级手册、方案对比、事故复盘、架构说明。
**不用**：长篇阅读型文档（Markdown 更好）、需要严格印制的正式文件、真正的数据仪表盘。

判断标准：**这份东西会不会被人投影或投屏讲一遍？** 会 → 用这套。只是自己查 → Markdown。

---

## 三条铁律

违反任何一条，成品就会从"有设计感"掉成"花里胡哨"。

### 铁律 1：全页只有一个深色块

整页 90% 是浅底白卡，只允许**一个**深色大色块当视觉锚点，通常放在开篇的"一句话结论"。

颜色密集 ≠ 颜色分散。明艳感来自"一个很重的色块 + 周围大量留白 + 少量高饱和点缀"。
如果每个区块都想抢戏，结果是每块都记不住。

### 铁律 2：颜色是语义，不是装饰

只允许 **5 个色相**，每个色相必须配齐 `主色 / 浅底 / 深字` 三档。
同一个概念在全页永远用同一个颜色——标题里的词、卡片顶边条、表格里的胶囊，全部对齐。

读者会在潜意识里建立颜色→概念的映射，这才是"看起来专业"的真正来源。每张卡随机配色的页面更热闹，但更廉价。

### 铁律 3：改完必须渲染截图

**没有截图检查过的页面 = 没做完。**

用无头 Chrome 渲染、切片、逐段看。这一步能抓到纯读代码绝对发现不了的问题（对比度、溢出、孤立卡片）。
"看起来还行"和"确实好看"之间的差距，就是这一轮。

---

## 色彩系统

**具体色值只在文末 CSS 的 `:root` 里写一份**，本节只规定"哪个颜色表达什么"。换主题只改那一处。

| 语义 | 变量 | 典型用途 |
|---|---|---|
| 旧 / 反对照 | `--c-old` | old 侧、错误、禁止事项 |
| 新 / 成功 | `--c-new` | new 侧、已验证、验收通过 |
| 结构 / 中性 | `--c-struct` | 章节色条、编号、中性标签 |
| 警示 | `--c-warn` | 易漏步骤、未验证、风险 |
| 信息 | `--c-info` | 时间线、证据、序号 |

每个色相必须配齐主色、`-soft`（浅底）、`-deep`（深字）三档，否则组件里会被迫写死颜色。

中性色同理：`--ink`（正文）、`--ink-2`（次要）、`--ink-3`（弱化）、`--bg`、`--surface`、`--line`。

**分配原则**：先决定内容里哪两个东西是对立的（旧/新、错/对、改前/改后），它们吃 红/绿 两色。
剩下的颜色给结构、警示和信息，不要用来表达对立关系。

---

## 字体分工

| 角色 | 变量 | 说明 |
|---|---|---|
| 标题 | `--display` | 与正文同一套系统字体，靠字号和字重拉开层级 |
| 正文 | `--sans` | 系统默认：苹方 / 微软雅黑 / Segoe UI |
| 代码 / hash / 数字 | `--mono` | 系统等宽：SF Mono / Consolas |

**不加载任何外部字体**——不引 Google Fonts，不嵌 woff。打开即用，离线也不掉样式。
完整的字体栈、字号、行高只在 CSS 里定义。

标题的层级感靠三件事：字号放大到 `clamp()` 上限、字重压到 700、字距收紧。
换字体不会改变这个结论，所以不需要为了"设计感"引入特殊字体。

**负字距是廉价感与设计感的分界线。** 中文大标题默认字距偏松，收紧 `0.02em ~ 0.03em` 之后
立刻从"文档标题"变成"海报标题"。

---

## 组件选择表

先出内容大纲，再给每个 section 指定组件类型。**不允许临场发明新样式。**

| 内容特征 | 用哪个组件 | 约束 |
|---|---|---|
| 开篇标题 + 核心结论 | Hero（大标题 + 4 个统计卡） | 统计卡 4 个，多了就削 |
| 全文浓缩成一句 | 深色 Callout | 全页唯一深色块 |
| 有 before / after | 左右对照卡 + 中间箭头 | 两侧字段必须一一对应 |
| 多字段映射 | 速查表 | 表头染色，隔行染色，hover 高亮 |
| 3–5 个先后事件 | 时间线卡 | 每张配渐变色数字块 + hash 黑底 |
| 3–6 步流程 | 步骤卡网格 | 必须有一个通栏强调项（见"版式规则"） |
| 3 条并列注意事项 | 提示卡三列 | 顶部小圆点编号 |
| 禁止 / 红线 | 红线卡（双列菱形 bullet） | 粉色描边，全页最多一处 |
| 症状 → 处理 | 问题表 | 必须有"症状"列，否则没法对号入座 |
| 结论 + 可信度 | 状态表 + 彩色胶囊 | 胶囊只有"已验证/未验证"两类 |
| 一分钟口播稿 | 渐变底大卡 | 口语，不是书面语 |

---

## 版式规则

**卡片三件套**：所有卡片共用 `顶部渐变条（5px） + 1px 极浅描边 + 几乎看不见的投影`。
一致性比单个精致更重要——成套的组件本身就产生高级感。

**表格当主角做**，不能是浏览器默认样式：
表头用浅紫底的中等字重小字，隔行微微染色，hover 时整行提亮；
状态用彩色胶囊不用纯文字；外层圆角容器 + `overflow-x:auto`。

**打破网格必须有理由**。五步流程在两列网格里会剩一个孤立空位，处理方式不是缩小卡片，
而是把**语义最重要的那一步**拉成通栏并换色。没有理由的不对称，看起来就是排版事故。

**数字放大**。凡是能变成数字的结论（删除多少行、改了几次、几个文件），都做成大号统计卡，比写在句子里强十倍。

### 标题和引导语必须撑满容器，不许折行

这是最容易翻车、也最容易被指出来的一条，三件事要一起做：

1. **不设宽度上限**。`h1`、`.lede`、`.sec-head p` 一律 `max-width:100%`。
   写成 `60ch` / `74ch` 会把文字锁死在窄列里——容器明明有 1092px，文字只占了 740px，
   右边空一大片，看起来就是"折行了"。**`ch` 单位在中文排版下尤其坑**：它按字符 `0` 的宽度算，
   18px 字号下 `1ch ≈ 10px`，而 1 个汉字是 18px，所以 `74ch` 只能装 41 个汉字，不是 74 个。
2. **标题里不用 `<br>` 强制断行**。固定在某个词后断开会随着屏幕宽度变化，
   变成"两行都短、中间空一大块"的怪样子。让它自然流动。
3. **引导语的字数要按实测宽度砍**。不折行不是写了就自动成立——超宽的字数照样折。
   先渲染量一次，超了就删字，不要靠缩小字号或留着让它折。

> 经验值：容器 `max-width:1140px`、正文字号 18px 时，
> 中文引导语控制在 **47 个中文字符以内**（英文单词按 2 个字算）基本就是一行。
> 但这只是估算，**以实测为准**，见"强制验证 · 5. 不折行检查"。

---

## 动效

只保留两处：

1. 滚动淡入（`IntersectionObserver` + `opacity/translateY`）
2. 顶部导航随滚动高亮当前章节

两处都必须尊重 `prefers-reduced-motion: reduce`。
不要加悬停形变、视差、打字机效果——多一处就掉一档。

---

## 执行流程

1. **读源内容**，先写成大纲（只有标题层级，不写 HTML）
2. 给每个 section 从组件选择表指派类型
3. 定语义色分配：谁=旧、谁=新、谁=警告、谁=结构
4. 复制文末的 CSS 与骨架作为起点，**不要从空白写**
5. 填内容；长度不够的卡片不要硬凑，合并到相邻 section
6. 按"强制验证"渲染检查，修问题 → 再渲染，直到干净

---

## 起手模板

### 1. 设计令牌与组件样式

整段复制进 `<style>`。换主题只改 `:root` 里的五个 `--c-*` 色相，不要在组件里写死颜色。

```css
:root{
  --ink:#171335; --ink-2:#4b4468; --ink-3:#837b9e;
  --bg:#f5f3fa; --surface:#fff; --line:rgba(23,19,53,.11);

  /* 五个语义色：每个都必须配齐 主 / 浅底 / 深字 */
  --c-old:#ff4d6d;    --c-old-soft:#ffe4ea;    --c-old-deep:#c81f47;
  --c-new:#00b894;    --c-new-soft:#d3f8ed;    --c-new-deep:#00795f;
  --c-struct:#7c3aed; --c-struct-soft:#ece6ff; --c-struct-deep:#5b21b6;
  --c-warn:#f59e0b;   --c-warn-soft:#fff0d1;   --c-warn-deep:#a86500;
  --c-info:#0ea5e9;   --c-info-soft:#dcf1fe;   --c-info-deep:#0369a1;

  --radius:20px; --radius-sm:12px;
  --shadow-sm:0 1px 2px rgba(23,19,53,.05),0 4px 14px -8px rgba(23,19,53,.22);
  --shadow-md:0 2px 4px rgba(23,19,53,.04),0 18px 40px -24px rgba(23,19,53,.35);

  /* 全部使用系统字体，不加载任何外部字体 */
  --sans:-apple-system,BlinkMacSystemFont,'PingFang SC','Hiragino Sans GB',
         'Microsoft YaHei','Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;
  --display:var(--sans);
  --mono:ui-monospace,SFMono-Regular,'SF Mono',Menlo,Consolas,
         'Liberation Mono','Courier New',monospace;
}

*{box-sizing:border-box}
html{scroll-behavior:smooth;scroll-padding-top:78px}
body{margin:0;font-family:var(--sans);background:var(--bg);color:var(--ink);
  -webkit-font-smoothing:antialiased;line-height:1.7}

/* 背景光晕：四团柔色，是"明艳但不刺眼"的来源 */
body::before{content:"";position:fixed;inset:0;z-index:-1;opacity:.85;
  background:
    radial-gradient(760px 520px at 6% -6%, #ffe1e9 0%, transparent 62%),
    radial-gradient(700px 500px at 96% 2%, #d6f8ee 0%, transparent 60%),
    radial-gradient(820px 620px at 76% 96%, #e8e1ff 0%, transparent 62%),
    radial-gradient(600px 460px at 18% 88%, #d8f0ff 0%, transparent 62%);}

.wrap{max-width:1140px;margin:0 auto;padding:0 24px}

/* ---- 导航 ---- */
.nav{position:sticky;top:0;z-index:50;
  backdrop-filter:blur(14px) saturate(1.4);-webkit-backdrop-filter:blur(14px) saturate(1.4);
  background:rgba(245,243,250,.78);border-bottom:1px solid var(--line)}
.nav-in{max-width:1140px;margin:0 auto;padding:11px 24px;display:flex;align-items:center;
  gap:18px;overflow-x:auto;scrollbar-width:none}
.nav-in::-webkit-scrollbar{display:none}
.nav-brand{display:flex;align-items:center;gap:9px;
  font:600 14px/1 var(--display);letter-spacing:.01em;white-space:nowrap}
.nav-dot{width:10px;height:10px;border-radius:50%;
  background:linear-gradient(135deg,var(--c-old),var(--c-struct));
  box-shadow:0 0 0 3px rgba(124,58,237,.14)}
.nav-links{display:flex;gap:4px;margin-left:auto}
.nav-links a{font-size:13px;color:var(--ink-2);text-decoration:none;padding:6px 11px;
  border-radius:999px;white-space:nowrap;transition:background .18s,color .18s}
.nav-links a:hover{background:rgba(255,255,255,.9);color:var(--ink)}
.nav-links a.on{background:var(--ink);color:#fff}

/* ---- Hero ---- */
.hero{padding:64px 0 20px}
.eyebrow{display:inline-flex;align-items:center;gap:8px;font:600 12px/1 var(--mono);
  letter-spacing:.08em;text-transform:uppercase;color:var(--c-struct-deep);
  background:var(--c-struct-soft);border:1px solid rgba(124,58,237,.22);
  padding:8px 13px;border-radius:999px}
h1{font-family:var(--display);font-size:clamp(38px,6.2vw,68px);line-height:1.06;
  letter-spacing:-.028em;font-weight:700;margin:22px 0 0;text-wrap:balance}

/* 荧光笔式高亮 */
.hl{position:relative;white-space:nowrap;padding:0 .08em}
.hl::after{content:"";position:absolute;left:0;right:0;bottom:.06em;height:.34em;
  border-radius:3px;z-index:-1}
.hl-old::after{background:var(--c-old-soft)} .hl-new::after{background:var(--c-new-soft)}
.hl-old{color:var(--c-old-deep)} .hl-new{color:var(--c-new-deep)}

.lede{margin:26px 0 0;font-size:18px;color:var(--ink-2);
  max-width:100%;              /* 绝不设 ch 上限，否则大屏上提前折行 */
  text-wrap:pretty}

.stats{display:grid;grid-template-columns:repeat(4,1fr);gap:14px;margin-top:38px}
.stat{background:var(--surface);border:1px solid var(--line);border-radius:var(--radius-sm);
  padding:18px 18px 16px;box-shadow:var(--shadow-sm);position:relative;overflow:hidden}
.stat::before{content:"";position:absolute;left:0;top:0;bottom:0;width:4px}
.stat:nth-child(1)::before{background:var(--c-old)}
.stat:nth-child(2)::before{background:var(--c-struct)}
.stat:nth-child(3)::before{background:var(--c-new)}
.stat:nth-child(4)::before{background:var(--c-warn)}
.stat b{display:block;font:700 30px/1.1 var(--display);letter-spacing:-.02em}
.stat span{display:block;margin-top:6px;font-size:13px;color:var(--ink-3)}

/* ---- 章节 ---- */
section{padding:46px 0}
.sec-head{display:flex;align-items:flex-start;gap:14px;margin-bottom:22px}
.sec-bar{width:5px;align-self:stretch;min-height:44px;border-radius:99px;margin-top:3px;flex:none}
.sec-head h2{margin:0;font-family:var(--display);font-size:clamp(22px,2.6vw,29px);
  letter-spacing:-.02em;line-height:1.25}
.sec-head p{margin:5px 0 0;color:var(--ink-3);font-size:14px}
.c-old{background:var(--c-old)} .c-new{background:var(--c-new)}
.c-struct{background:var(--c-struct)} .c-warn{background:var(--c-warn)}
.c-info{background:var(--c-info)}

.card{background:var(--surface);border:1px solid var(--line);
  border-radius:var(--radius);box-shadow:var(--shadow-sm)}

/* ---- 深色 Callout：全页唯一深色块 ---- */
.callout{position:relative;padding:34px 36px;border-radius:24px;color:#fff;overflow:hidden;
  background:linear-gradient(122deg,#241a52 0%,#3d1f6e 48%,#6d2160 100%);
  box-shadow:0 26px 60px -34px rgba(61,31,110,.9)}
.callout::after{content:"";position:absolute;inset:0;pointer-events:none;
  background:
    radial-gradient(420px 240px at 88% 12%, rgba(255,77,109,.45), transparent 68%),
    radial-gradient(380px 220px at 6% 100%, rgba(0,184,148,.34), transparent 66%)}
.callout-label{position:relative;z-index:1;font:600 12px/1 var(--mono);letter-spacing:.1em;
  text-transform:uppercase;color:rgba(255,255,255,.62);margin-bottom:14px}
.callout p{position:relative;z-index:1;margin:0;font-size:clamp(18px,2.3vw,23px);
  line-height:1.62;font-weight:500}
.callout code{font-family:var(--mono);font-size:.88em;background:rgba(255,255,255,.2);
  border:1px solid rgba(255,255,255,.28);color:#fff;   /* 必须显式写，否则继承深色紫字 */
  padding:2px 7px;border-radius:6px;white-space:nowrap}

/* ---- 左右对照 ---- */
.compare{display:grid;grid-template-columns:1fr 52px 1fr;align-items:stretch}
.cmp{padding:26px 26px 22px;border-radius:var(--radius);border:1px solid var(--line);
  background:var(--surface);box-shadow:var(--shadow-md);position:relative;overflow:hidden}
.cmp::before{content:"";position:absolute;top:0;left:0;right:0;height:5px}
.cmp--old::before{background:linear-gradient(90deg,var(--c-old),#ff9a76)}
.cmp--new::before{background:linear-gradient(90deg,var(--c-new),#3dd6ff)}
.cmp--old{background:linear-gradient(180deg,#fff5f7 0%,#fff 46%)}
.cmp--new{background:linear-gradient(180deg,#f0fdf9 0%,#fff 46%)}
.cmp-top{display:flex;align-items:center;gap:10px;margin-bottom:6px}
.cmp-top h3{margin:0;font-family:var(--display);font-size:21px;letter-spacing:-.01em}
.cmp--old .cmp-top h3{color:var(--c-old-deep)}
.cmp--new .cmp-top h3{color:var(--c-new-deep)}
.cmp-sub{margin:0 0 18px;font-size:13px;color:var(--ink-3)}

.kv{list-style:none;margin:0;padding:0}
.kv li{display:flex;align-items:baseline;gap:10px;padding:9px 0;
  border-bottom:1px dashed var(--line);font-size:14px}
.kv li:last-child{border-bottom:0}
.kv .k{color:var(--ink-3);flex:none;width:82px;font-size:13px}
.kv .v{color:var(--ink);font-weight:500}
.arrow-mid{display:flex;align-items:center;justify-content:center}
.arrow-mid svg{width:36px;height:36px;color:var(--c-struct)}

/* ---- 状态胶囊 ---- */
.chip{display:inline-flex;align-items:center;font:600 11px/1 var(--sans);
  padding:6px 10px;border-radius:999px;letter-spacing:.02em;white-space:nowrap}
.chip--old{background:var(--c-old-soft);color:var(--c-old-deep)}
.chip--new{background:var(--c-new-soft);color:var(--c-new-deep)}
.chip--struct{background:var(--c-struct-soft);color:var(--c-struct-deep)}
.chip--warn{background:var(--c-warn-soft);color:var(--c-warn-deep)}
.chip--info{background:var(--c-info-soft);color:var(--c-info-deep)}
.chip--plain{background:#eceaf3;color:var(--ink-2)}

/* ---- 表格：必须做成设计过的样子 ---- */
.table-wrap{border:1px solid var(--line);border-radius:var(--radius);overflow:hidden;
  box-shadow:var(--shadow-sm);background:var(--surface)}
.scroll-x{overflow-x:auto}
table{border-collapse:separate;border-spacing:0;width:100%;min-width:600px;font-size:14px}
thead th{text-align:left;font:600 12px/1 var(--sans);letter-spacing:.04em;
  padding:14px 18px;background:#f0edf9;color:var(--ink-2);
  border-bottom:1px solid var(--line);white-space:nowrap}
thead th:first-child{border-top-left-radius:var(--radius)}
tbody td{padding:13px 18px;border-bottom:1px solid var(--line);
  vertical-align:top;color:var(--ink-2)}
tbody tr:last-child td{border-bottom:0}
tbody tr:nth-child(even) td{background:#fbfaff}
tbody tr:hover td{background:#f6f3ff}
tbody td:first-child{color:var(--ink);font-weight:500}
td code,p code,li code{font-family:var(--mono);font-size:12.5px;background:#f2effa;
  color:var(--c-struct-deep);padding:1.5px 6px;border-radius:5px;
  overflow-wrap:anywhere}      /* 长包名不顶到边缘 */
.t-old{color:var(--c-old-deep);font-weight:500}
.t-new{color:var(--c-new-deep);font-weight:500}

/* ---- 时间线 ---- */
.timeline{display:grid;grid-template-columns:repeat(3,1fr);gap:16px}
.tl{padding:22px 22px 20px;position:relative}
.tl-dot{width:34px;height:34px;border-radius:11px;display:grid;place-items:center;
  font:700 13px/1 var(--display);color:#fff;margin-bottom:14px}
.tl:nth-child(1) .tl-dot{background:linear-gradient(135deg,var(--c-old),#ff8a5c)}
.tl:nth-child(2) .tl-dot{background:linear-gradient(135deg,var(--c-struct),#a855f7)}
.tl:nth-child(3) .tl-dot{background:linear-gradient(135deg,var(--c-new),#22d3ee)}
.tl h3{margin:0 0 4px;font-size:16px;letter-spacing:-.01em}
.tl .date{font:500 12px/1 var(--mono);color:var(--ink-3)}
.tl .hash{display:inline-block;margin:12px 0 10px;font:600 12px/1 var(--mono);
  background:var(--ink);color:#fff;padding:5px 9px;border-radius:6px}
.tl p{margin:0;font-size:13.5px;color:var(--ink-2);line-height:1.65}

/* ---- 步骤卡 ---- */
.steps{display:grid;grid-template-columns:repeat(3,1fr);gap:16px}
.step{padding:24px 24px 22px;position:relative;overflow:hidden;display:flex;gap:16px}
.step-n{flex:none;width:42px;height:42px;border-radius:13px;display:grid;place-items:center;
  font:700 17px/1 var(--display);background:var(--c-struct-soft);color:var(--c-struct-deep)}
.step h3{margin:0 0 7px;font-size:16.5px;letter-spacing:-.01em}
.step p{margin:0;font-size:13.5px;color:var(--ink-2);line-height:1.66}

/* 通栏强调项：只给语义最重要的那一步 */
.step--flag{grid-column:1 / -1;
  background:linear-gradient(118deg,#fff8e8 0%,#fffdf8 58%,#fff 100%);
  border-color:rgba(245,158,11,.42);
  box-shadow:0 2px 4px rgba(245,158,11,.08),0 20px 44px -28px rgba(245,158,11,.7)}
.step--flag::before{content:"";position:absolute;left:0;top:0;bottom:0;width:5px;
  background:linear-gradient(180deg,var(--c-warn),#ff8a5c)}
.step--flag .step-n{background:var(--c-warn);color:#fff}

/* 通栏验收项：用胶囊列检查点 */
.step--check{grid-column:1 / -1;
  background:linear-gradient(118deg,#f0fdf9 0%,#fbfffd 58%,#fff 100%);
  border-color:rgba(0,184,148,.36);
  box-shadow:0 2px 4px rgba(0,184,148,.06),0 20px 44px -28px rgba(0,184,148,.6)}
.step--check .step-n{background:var(--c-new);color:#fff}
.checks{display:flex;flex-wrap:wrap;gap:8px;margin-top:12px}
.checks span{font-size:12.5px;color:var(--c-new-deep);background:rgba(0,184,148,.1);
  border:1px solid rgba(0,184,148,.24);padding:5px 11px;border-radius:999px;
  white-space:nowrap}

/* ---- 三列提示卡 ---- */
.notes{display:grid;grid-template-columns:repeat(3,1fr);gap:16px}
.note{padding:22px 22px 20px;position:relative}
.note-n{display:inline-grid;place-items:center;width:26px;height:26px;border-radius:8px;
  font:600 12px/1 var(--mono);background:var(--c-info-soft);color:var(--c-info-deep);
  margin-bottom:12px}
.note h3{margin:0 0 7px;font-size:15.5px}
.note p{margin:0;font-size:13.5px;color:var(--ink-2);line-height:1.66}

/* ---- 红线卡：全页最多一处 ---- */
.redlines{padding:26px 28px;border-radius:var(--radius);
  border:1px solid rgba(255,77,109,.3);
  background:linear-gradient(140deg,#fff4f6 0%,#fff 60%);box-shadow:var(--shadow-sm)}
.redlines h3{margin:0 0 16px;font-size:16px;color:var(--c-old-deep);
  display:flex;align-items:center;gap:9px}
.redlines ul{margin:0;padding:0;list-style:none;display:grid;
  grid-template-columns:1fr 1fr;gap:10px 26px}
.redlines li{position:relative;padding-left:20px;font-size:14px;
  color:var(--ink-2);line-height:1.62}
.redlines li::before{content:"";position:absolute;left:2px;top:.62em;width:7px;height:7px;
  border-radius:2px;background:var(--c-old);transform:rotate(45deg)}  /* 菱形替代圆点 */

/* ---- 口播稿 ---- */
.script{padding:30px 32px;border-radius:var(--radius);
  background:linear-gradient(135deg,#eef6ff 0%,#fbf5ff 52%,#f2fffa 100%);
  border:1px solid var(--line);box-shadow:var(--shadow-sm)}
.script p{margin:0 0 14px;font-size:16px;line-height:1.85;color:var(--ink)}
.script p:last-child{margin-bottom:0}
.script strong{background:linear-gradient(180deg,transparent 62%,rgba(124,58,237,.22) 62%);
  font-weight:700}
.script code{font-family:var(--mono);font-size:.86em;background:var(--c-struct-soft);
  color:var(--c-struct-deep);padding:2px 7px;border-radius:6px}

/* ---- 页脚 ---- */
footer{padding:46px 0 60px;color:var(--ink-3);font-size:13px;
  border-top:1px solid var(--line);margin-top:30px}
footer .wrap{display:flex;justify-content:space-between;gap:20px;flex-wrap:wrap}
footer code{font-family:var(--mono);font-size:12px}

/* ---- 动效：两处，且必须尊重 reduced-motion ---- */
.rv{opacity:0;transform:translateY(16px);
  transition:opacity .55s ease,transform .55s ease}
.rv.in{opacity:1;transform:none}
@media (prefers-reduced-motion:reduce){
  .rv{opacity:1;transform:none;transition:none}
  html{scroll-behavior:auto}
}

/* ---- 响应式 ---- */
@media (max-width:1080px){
  .steps{grid-template-columns:repeat(2,1fr)}
  .step:nth-child(3){grid-column:1 / -1}
}
@media (max-width:900px){
  .stats{grid-template-columns:repeat(2,1fr)}
  .compare{grid-template-columns:1fr;gap:14px}
  .arrow-mid{height:28px}
  .arrow-mid svg{transform:rotate(90deg)}
  .timeline,.steps,.notes{grid-template-columns:1fr}
  .redlines ul{grid-template-columns:1fr}
}
@media (max-width:560px){
  .wrap{padding:0 16px}
  .nav-in{padding:10px 16px}
  .nav-links{display:none}
  .hero{padding:40px 0 10px}
  .lede{font-size:16px}
  .stats{gap:10px}
  .stat b{font-size:24px}
  .callout{padding:26px 22px;border-radius:18px}
  .callout code{white-space:normal;overflow-wrap:anywhere}  /* 长依赖坐标必须能换行 */
  .cmp{padding:22px 20px 18px}
  .step{flex-direction:column;gap:12px}
  .kv li{flex-direction:column;gap:2px}
  .kv .k{width:auto}
}
@media print{
  .nav{display:none}
  body::before{display:none}
  body{background:#fff}
  .card,.table-wrap,.callout{box-shadow:none}
  section{padding:22px 0;break-inside:avoid}
}
```

### 2. 页面骨架

每个组件都在这里出现一次，替换 `[[占位符]]` 即可。

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>[[页面标题，2–4 个词]]</title>
<!-- 不引外部字体，系统字体直接可用 -->
<style>/* 粘贴上面的 CSS */</style>
</head>
<body>

<nav class="nav">
  <div class="nav-in">
    <div class="nav-brand"><span class="nav-dot"></span>[[项目名]]</div>
    <div class="nav-links">
      <a href="#compare">对照</a>
      <a href="#timeline">时间线</a>
      <a href="#steps">步骤</a>
      <a href="#pitfalls">坑</a>
      <a href="#evidence">证据</a>
    </div>
  </div>
</nav>

<header class="hero">
  <div class="wrap">
    <span class="eyebrow">[[领域]] · [[文档类型]]</span>
    <!-- 不要在标题里用 <br> 强制断行，让它按容器宽度自然流动 -->
    <h1>把 <span class="hl hl-old">[[旧方案]]</span> 换成 <span class="hl hl-new">[[新方案]]</span></h1>
    <p class="lede">[[一句话说清问题、结论、读者该知道什么。控制在 47 字以内。]]</p>
    <div class="stats">
      <div class="stat"><b>[[N]]</b><span>[[统计项]]</span></div>
      <div class="stat"><b>[[−N]]</b><span>[[统计项]]</span></div>
      <div class="stat"><b>[[+N]]</b><span>[[统计项]]</span></div>
      <div class="stat"><b>[[N]]</b><span>[[统计项]]</span></div>
    </div>
  </div>
</header>

<main class="wrap">

  <!-- 深色 Callout：全页唯一深色块，放核心结论 -->
  <section>
    <div class="callout rv">
      <div class="callout-label">一句话说清楚</div>
      <p>[[用一句完整的话概括结论，行内代码不超过 3 个。]]</p>
    </div>
  </section>

  <!-- 左右对照：两侧字段必须一一对应 -->
  <section id="compare">
    <div class="sec-head rv">
      <div class="sec-bar c-old"></div>
      <div><h2>迁移前 / 迁移后</h2><p>[[一句话说明两侧本质差异。]]</p></div>
    </div>
    <div class="compare rv">
      <article class="cmp cmp--old">
        <div class="cmp-top"><span class="chip chip--old">迁移前</span><h3>[[old 名称]]</h3></div>
        <p class="cmp-sub">[[一句话描述]]</p>
        <ul class="kv">
          <li><span class="k">[[字段]]</span><span class="v">[[值]]</span></li>
          <li><span class="k">[[字段]]</span><span class="v"><code>[[值]]</code></span></li>
        </ul>
      </article>
      <div class="arrow-mid" aria-hidden="true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
             stroke-linecap="round" stroke-linejoin="round">
          <path d="M4 12h15"/><path d="M13 6l6 6-6 6"/>
        </svg>
      </div>
      <article class="cmp cmp--new">
        <div class="cmp-top"><span class="chip chip--new">迁移后</span><h3>[[new 名称]]</h3></div>
        <p class="cmp-sub">[[一句话描述]]</p>
        <ul class="kv">
          <li><span class="k">[[字段]]</span><span class="v">[[值]]</span></li>
          <li><span class="k">[[字段]]</span><span class="v"><code>[[值]]</code></span></li>
        </ul>
      </article>
    </div>
  </section>

  <!-- 速查表 -->
  <section>
    <div class="sec-head rv">
      <div class="sec-bar c-struct"></div>
      <div><h2>对照速查表</h2><p>[[这张表是给人照着改的。]]</p></div>
    </div>
    <div class="table-wrap scroll-x rv">
      <table>
        <thead><tr><th style="width:20%">项目</th><th style="width:40%">old</th><th style="width:40%">new</th></tr></thead>
        <tbody>
          <tr><td>[[项目]]</td><td class="t-old"><code>[[值]]</code></td><td class="t-new">[[值]]</td></tr>
          <tr><td>[[项目]]</td><td>[[值]]</td><td><code>[[值]]</code></td></tr>
        </tbody>
      </table>
    </div>
  </section>

  <!-- 时间线：3–5 个先后事件 -->
  <section id="timeline">
    <div class="sec-head rv">
      <div class="sec-bar c-info"></div>
      <div><h2>真实时间线</h2><p>[[提醒读者别把某一步当成起点。]]</p></div>
    </div>
    <div class="timeline rv">
      <article class="card tl">
        <div class="tl-dot">1</div><h3>[[阶段名]]</h3>
        <div class="date">[[YYYY-MM-DD]]</div><div class="hash">[[短hash]]</div>
        <p>[[这一步做了什么。]]</p>
      </article>
      <article class="card tl">
        <div class="tl-dot">2</div><h3>[[阶段名]]</h3>
        <div class="date">[[YYYY-MM-DD]]</div><div class="hash">[[短hash]]</div>
        <p>[[这一步做了什么。]]</p>
      </article>
      <article class="card tl">
        <div class="tl-dot">3</div><h3>[[阶段名]]</h3>
        <div class="date">[[YYYY-MM-DD]]</div><div class="hash">[[短hash]]</div>
        <p>[[这一步做了什么。]]</p>
      </article>
    </div>
  </section>

  <!-- 步骤卡：必须有一个通栏强调项 -->
  <section id="steps">
    <div class="sec-head rv">
      <div class="sec-bar c-new"></div>
      <div><h2>要做的事</h2><p>[[提示哪一步最容易漏。]]</p></div>
    </div>
    <div class="steps rv">
      <article class="card step"><div class="step-n">1</div>
        <div><h3>[[步骤名]]</h3><p>[[做什么。]]</p></div></article>
      <article class="card step"><div class="step-n">2</div>
        <div><h3>[[步骤名]]</h3><p>[[做什么。]]</p></div></article>
      <article class="card step"><div class="step-n">3</div>
        <div><h3>[[步骤名]]</h3><p>[[做什么。]]</p></div></article>
      <article class="card step step--flag"><div class="step-n">4</div>
        <div><h3>[[最容易漏的那一步]]</h3>
          <p>[[为什么危险、漏了会怎样、正确顺序是什么。]]</p></div></article>
      <article class="card step step--check"><div class="step-n">5</div>
        <div><h3>[[验证]]</h3><p>[[验证范围。]]</p>
          <div class="checks"><span>[[检查点]]</span><span>[[检查点]]</span><span>[[检查点]]</span></div>
        </div></article>
    </div>
  </section>

  <!-- 三列提示卡 -->
  <section>
    <div class="sec-head rv">
      <div class="sec-bar c-warn"></div>
      <div><h2>三条边界</h2><p>[[哪些结论不在范围内。]]</p></div>
    </div>
    <div class="notes rv">
      <article class="card note"><div class="note-n">01</div><h3>[[边界一]]</h3><p>[[说明。]]</p></article>
      <article class="card note"><div class="note-n">02</div><h3>[[边界二]]</h3><p>[[说明。]]</p></article>
      <article class="card note"><div class="note-n">03</div><h3>[[边界三]]</h3><p>[[说明。]]</p></article>
    </div>
  </section>

  <!-- 红线卡：全页最多一处 -->
  <section>
    <div class="redlines rv">
      <h3>
        <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor"
             stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M12 9v4"/><path d="M12 17h.01"/>
          <path d="M10.3 3.9 1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0z"/>
        </svg>
        [[N]] 条红线
      </h3>
      <ul>
        <li>[[禁止事项，可带 <code>行内代码</code>]]</li>
        <li>[[禁止事项]]</li>
        <li>[[禁止事项]]</li>
        <li>[[禁止事项]]</li>
      </ul>
    </div>
  </section>

  <!-- 问题表：症状 → 处理 -->
  <section id="pitfalls">
    <div class="sec-head rv">
      <div class="sec-bar c-old"></div>
      <div><h2>已知的 N 个坑</h2><p>按症状对号入座，比按步骤排查快得多。</p></div>
    </div>
    <div class="table-wrap scroll-x rv">
      <table style="min-width:720px">
        <thead><tr><th style="width:6%">#</th><th style="width:30%">坑</th>
          <th style="width:34%">症状</th><th style="width:30%">处理</th></tr></thead>
        <tbody>
          <tr><td>1</td><td>[[坑]]</td><td>[[症状]]</td><td>[[处理]]</td></tr>
          <tr><td>2</td><td>[[坑]]</td><td>[[症状]]</td><td>[[处理]]</td></tr>
        </tbody>
      </table>
    </div>
  </section>

  <!-- 状态表：结论 + 可信度 -->
  <section id="evidence">
    <div class="sec-head rv">
      <div class="sec-bar c-info"></div>
      <div><h2>哪些被证明了，哪些没有</h2><p>这张表决定读者还要去几处确认。</p></div>
    </div>
    <div class="table-wrap scroll-x rv">
      <table style="min-width:680px">
        <thead><tr><th style="width:52%">结论</th><th style="width:48%">状态</th></tr></thead>
        <tbody>
          <tr><td>[[结论]]</td><td><span class="chip chip--new">已验证</span> <code>[[依据]]</code></td></tr>
          <tr><td>[[结论]]</td><td><span class="chip chip--warn">未验证</span> [[原因]]</td></tr>
        </tbody>
      </table>
    </div>
  </section>

  <!-- 口播稿：口语，不是书面语 -->
  <section>
    <div class="sec-head rv">
      <div class="sec-bar c-struct"></div>
      <div><h2>一分钟讲完</h2><p>开会时照着念就行。</p></div>
    </div>
    <div class="script rv">
      <p>[[第一段：要做什么，结论是什么。]]</p>
      <p>[[第二段：为什么这么做，有什么前提。]]</p>
      <p>[[第三段：最需要注意的一点。]]</p>
    </div>
  </section>

</main>

<footer>
  <div class="wrap">
    <div>[[来源 / 版本 / 日期]]</div>
    <div>[[补充信息]]</div>
  </div>
</footer>

<script>
/* 滚动淡入 */
(function(){
  var items = document.querySelectorAll('.rv');
  if (!('IntersectionObserver' in window)) {
    items.forEach(function(el){ el.classList.add('in'); }); return;
  }
  var io = new IntersectionObserver(function(entries){
    entries.forEach(function(e){
      if (e.isIntersecting) { e.target.classList.add('in'); io.unobserve(e.target); }
    });
  }, { rootMargin: '0px 0px -8% 0px', threshold: 0.08 });
  items.forEach(function(el){ io.observe(el); });
})();

/* 导航随滚动高亮当前章节 */
(function(){
  var links = Array.prototype.slice.call(document.querySelectorAll('.nav-links a'));
  if (!links.length) return;
  var map = {};
  links.forEach(function(a){ map[a.getAttribute('href').slice(1)] = a; });
  var targets = Object.keys(map).map(function(id){
    return document.getElementById(id);
  }).filter(Boolean);
  var io = new IntersectionObserver(function(entries){
    entries.forEach(function(e){
      if (!e.isIntersecting) return;
      links.forEach(function(a){ a.classList.remove('on'); });
      var a = map[e.target.id];
      if (a) a.classList.add('on');
    });
  }, { rootMargin: '-20% 0px -65% 0px' });
  targets.forEach(function(t){ io.observe(t); });
})();
</script>

</body>
</html>
```

---

## 强制验证

### 1. 结构校验

```bash
python3 - <<'PY'
from html.parser import HTMLParser
import re, sys
VOID={'area','base','br','col','embed','hr','img','input','link','meta','param','source','track','wbr'}
class P(HTMLParser):
    def __init__(s):
        super().__init__(convert_charrefs=True); s.stack=[]; s.errors=[]
    def handle_starttag(s,t,a):
        if t not in VOID: s.stack.append((t,s.getpos()))
    def handle_endtag(s,t):
        if t in VOID: return
        if not s.stack: s.errors.append(f"stray </{t}> {s.getpos()}"); return
        o,p=s.stack.pop()
        if o!=t: s.errors.append(f"<{o}> {p} closed by </{t}> {s.getpos()}")
src=open(sys.argv[1] if len(sys.argv)>1 else 'page.html',encoding='utf-8').read()
p=P(); p.feed(src)
print("errors:",p.errors or "none"); print("unclosed:",p.stack or "none")
print("raw &:",[m.group(0) for m in re.finditer(r'&(?![a-zA-Z][a-zA-Z0-9]*;|#\d+;|#x[0-9a-fA-F]+;)',src)] or "none")
PY
```

### 2. 整页截图

```bash
"/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" \
  --headless=new --disable-gpu --hide-scrollbars \
  --window-size=1400,7200 --screenshot=/tmp/shot/all.png \
  --virtual-time-budget=6000 "file:///绝对路径/page.html"
```

窗口高度给足（7000+），这样滚动淡入的元素在截图时都已经触发，不会拍到空白。

如果截到的是"内容若隐若现、半透明"的画面，说明拍到了淡入动画的中间帧。
在**验证用的临时副本**里把过渡关掉再截：

```css
.rv{opacity:1!important;transform:none!important;transition:none!important}
```

### 3. 切片细看

整页缩略图只能看结构，看不清细节，必须切片：

```bash
sips -c 1400 1400 --cropOffset 0 0 /tmp/shot/all.png --out /tmp/shot/p0.png
# cropOffset 的 y 每次 +1400，依次生成 p1/p2/p3...
```

每片都看一遍。

### 4. 移动端验证

**无头 Chrome 在 macOS 上有最小窗口宽度（约 500px），直接设 `--window-size=375` 会得到错误的裁剪结果——看起来像布局溢出，其实不是。**

正确做法是用 iframe 套一层：

```html
<!doctype html><html><body style="margin:0">
<iframe src="file:///绝对路径/page.html" style="width:375px;height:4200px;border:0"></iframe>
</body></html>
```

然后用 `--window-size=420,4200` 截这个包装页。

### 5. 不折行检查

把这段注入到页面临时副本的 `</body>` 前面，用 `--dump-dom` 读 `title`：

```js
window.addEventListener('load', function () {
  var h = document.querySelector('h1');
  var l = document.querySelector('.lede');
  var lh = parseFloat(getComputedStyle(l).lineHeight);
  document.title =
    'H1核数=' + h.getClientRects().length +
    ' 引导语行数=' + Math.round(l.getBoundingClientRect().height / lh) +
    ' 文字宽=' + Math.round(l.getBoundingClientRect().width) +
    ' 容器宽=' + Math.round(l.scrollWidth);
});
```

```bash
"/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" \
  --headless=new --disable-gpu --window-size=1400,900 --dump-dom \
  --virtual-time-budget=3000 "file:///tmp/probe.html" 2>/dev/null \
  | grep -o '<title>[^<]*</title>'
```

判定标准：

- **H1 核数 = 1**（1400px 与 1200px 宽都要是 1）
- **引导语行数 = 1**
- **文字宽 ≈ 容器宽**（差几 px 内算撑满；文字宽明显小于容器宽，说明还有残留 `max-width`）

不达标就改文案长度，不要靠缩字号糊过去。1200px 达不到，1000px 就更达不到。

### 必须逐项确认的清单

- [ ] 深色块里的行内代码对比度够（紫字压紫底是最常见翻车点）
- [ ] 长包名 / 长 class 名在窄卡片里没有顶到边缘（`overflow-wrap:anywhere`）
- [ ] 网格没有孤立空位
- [ ] 表格在窄屏是横向滚动而不是撑破页面
- [ ] 375px 下没有任何横向溢出
- [ ] 深色块里的长 token 在小屏能换行
- [ ] H1 与引导语在 1400px / 1200px 下都是单行，且宽度撑满容器

---

## 反模式

| 不要 | 原因 |
|---|---|
| 全页低饱和灰紫 | 读者抓不到重点 |
| 每个卡片一个颜色 | 热闹但廉价，破坏语义映射 |
| 浏览器默认表格样式 | 一眼看出是"不得不放的表" |
| 多处滚动动画 / 视差 | 多一处掉一档 |
| `max-width: 60ch` / `74ch` 的正文或标题 | 大屏上提前折行，右边空一片，最容易被指出来 |
| 标题里用 `<br>` 强制断行 | 换屏宽就变成两行都短的怪样子 |
| 网格里留孤立卡片 | 看起来像排版事故 |
| 用 emoji 当图标 | 跨平台渲染不一致，且廉价 |
| 导航锚点超过 6 个 | 手机上挤成一条，且说明页面结构没收敛 |
| 统计卡放 5 个以上 | 超过 4 个就没人看了 |
| 为了填满网格硬凑内容 | 宁可合并 section |

---

## 交付

产物是单个自包含 `.html` 文件，双击即可打开，不依赖本地服务器，**也不依赖任何外部字体或 CDN**。
断网、内网、离线都能正常显示。
