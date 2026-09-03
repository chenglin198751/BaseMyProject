# 页面映射表

> 客服加工客诉时，将用户模糊描述**归一**到本表「标准页面名」；
> AI 修复时用 `page` 字段反查本表，得到 Activity 类名与包路径。
> 新增页面时，由开发在此表**追加一行**，客服即可使用新页面名。

- **应用包名**：`com.qh.safe`
- **包路径规则**：`com.qh.safe.<模块子包>.<Activity类名>`，模块子包见各分组。

## 映射关系（共 40 个页面）

### 核心

| 标准页面名 | 用户常见说法（会被归一） | Activity 类名 | 包路径 |
|-----------|------------------------|--------------|--------|
| 主页 | 首页、主界面、首屏、home、大厅、开场页 | MainActivity | com.qh.safe.main.MainActivity |
| 登录页 | 登录、登陆、sign in、登录界面 | LoginActivity | com.qh.safe.login.LoginActivity |
| 注册页 | 注册、sign up、注册界面 | RegisterActivity | com.qh.safe.login.RegisterActivity |
| 详情页 | 详情、明细、点进去、详细信息、详情界面 | AppInfoActivity | com.qh.safe.app.AppInfoActivity |

### 游戏

| 标准页面名 | 用户常见说法（会被归一） | Activity 类名 | 包路径 |
|-----------|------------------------|--------------|--------|
| 游戏列表页 | 游戏列表、列表页、游戏页、游戏大厅、全部游戏 | GamesListActivity | com.qh.safe.game.GamesListActivity |
| 游戏详情页 | 游戏详情、游戏明细、单个游戏 | GameDetailActivity | com.qh.safe.game.GameDetailActivity |
| 游戏热榜页 | 热榜、游戏热榜、热门游戏、热门榜 | HotRankActivity | com.qh.safe.game.HotRankActivity |
| 排行榜页 | 排行、榜单、总榜、TOP 榜 | RankActivity | com.qh.safe.rank.RankActivity |
| 分类页 | 分类、频道、栏目 | CategoryActivity | com.qh.safe.category.CategoryActivity |

### 搜索

| 标准页面名 | 用户常见说法（会被归一） | Activity 类名 | 包路径 |
|-----------|------------------------|--------------|--------|
| 搜索页 | 搜一搜、搜索、查找 | SearchActivity | com.qh.safe.search.SearchActivity |
| 搜索结果页 | 搜索结果、搜出来的结果 | SearchResultActivity | com.qh.safe.search.SearchResultActivity |

### 用户

| 标准页面名 | 用户常见说法（会被归一） | Activity 类名 | 包路径 |
|-----------|------------------------|--------------|--------|
| 个人中心页 | 我的、个人中心、用户中心、我 | UserCenterActivity | com.qh.safe.user.UserCenterActivity |
| 个人资料页 | 资料、个人信息、编辑资料、头像 | ProfileActivity | com.qh.safe.user.ProfileActivity |
| 设置页 | 设置、系统设置、偏好设置 | SettingsActivity | com.qh.safe.user.SettingsActivity |
| 关于页 | 关于、关于我们、关于产品 | AboutActivity | com.qh.safe.user.AboutActivity |
| 会员页 | 会员、VIP、开通会员 | VipActivity | com.qh.safe.vip.VipActivity |
| 签到页 | 签到、每日签到、打卡 | CheckInActivity | com.qh.safe.task.CheckInActivity |
| 任务中心页 | 任务、任务中心、做任务 | TaskCenterActivity | com.qh.safe.task.TaskCenterActivity |

### 消息

| 标准页面名 | 用户常见说法（会被归一） | Activity 类名 | 包路径 |
|-----------|------------------------|--------------|--------|
| 消息列表页 | 消息、通知、消息中心、站内信 | MessageListActivity | com.qh.safe.message.MessageListActivity |
| 消息详情页 | 消息详情、单条消息、通知详情 | MessageDetailActivity | com.qh.safe.message.MessageDetailActivity |
| 通知设置页 | 通知设置、推送设置、消息提醒 | NotificationSettingsActivity | com.qh.safe.message.NotificationSettingsActivity |

### 内容 / 交互

| 标准页面名 | 用户常见说法（会被归一） | Activity 类名 | 包路径 |
|-----------|------------------------|--------------|--------|
| 下载管理页 | 下载管理、我的下载、下载列表 | DownloadManagerActivity | com.qh.safe.download.DownloadManagerActivity |
| 收藏页 | 收藏、我的收藏、收藏夹 | FavoriteActivity | com.qh.safe.favorite.FavoriteActivity |
| 历史记录页 | 历史、浏览历史、足迹、最近浏览 | HistoryActivity | com.qh.safe.history.HistoryActivity |
| 评论列表页 | 评论、评论区、留言 | CommentActivity | com.qh.safe.comment.CommentActivity |
| 评论详情页 | 评论详情、单条评论、回复 | CommentDetailActivity | com.qh.safe.comment.CommentDetailActivity |
| 分享页 | 分享、转发、分享界面 | ShareActivity | com.qh.safe.share.ShareActivity |

### 交易

| 标准页面名 | 用户常见说法（会被归一） | Activity 类名 | 包路径 |
|-----------|------------------------|--------------|--------|
| 充值页 | 充值、充钱、购买金币 | RechargeActivity | com.qh.safe.pay.RechargeActivity |
| 订单列表页 | 订单、我的订单、订单列表 | OrderListActivity | com.qh.safe.pay.OrderListActivity |
| 订单详情页 | 订单详情、单笔订单 | OrderDetailActivity | com.qh.safe.pay.OrderDetailActivity |
| 支付页 | 支付、付款、收银台 | PaymentActivity | com.qh.safe.pay.PaymentActivity |

### 媒体

| 标准页面名 | 用户常见说法（会被归一） | Activity 类名 | 包路径 |
|-----------|------------------------|--------------|--------|
| 视频播放页 | 视频播放、看视频、播放器 | VideoPlayerActivity | com.qh.safe.video.VideoPlayerActivity |
| 图片浏览页 | 图片查看、看图、大图浏览、相册预览 | ImagePreviewActivity | com.qh.safe.image.ImagePreviewActivity |
| 扫码页 | 扫码、扫一扫、二维码 | ScanActivity | com.qh.safe.scan.ScanActivity |

### 安全工具

| 标准页面名 | 用户常见说法（会被归一） | Activity 类名 | 包路径 |
|-----------|------------------------|--------------|--------|
| 权限设置页 | 权限、权限管理、授权 | PermissionActivity | com.qh.safe.permission.PermissionActivity |
| 安全中心页 | 安全中心、安全体检、安全报告 | SecurityCenterActivity | com.qh.safe.security.SecurityCenterActivity |
| 病毒查杀页 | 病毒查杀、杀毒、扫描病毒 | VirusScanActivity | com.qh.safe.security.VirusScanActivity |
| 垃圾清理页 | 垃圾清理、清理垃圾、清理加速 | JunkCleanActivity | com.qh.safe.clean.JunkCleanActivity |
| 缓存清理页 | 缓存清理、清缓存、释放空间 | CacheCleanActivity | com.qh.safe.clean.CacheCleanActivity |
| 流量监控页 | 流量、流量监控、流量统计 | TrafficMonitorActivity | com.qh.safe.traffic.TrafficMonitorActivity |

## 约定

1. **标准页面名唯一**，客服只允许使用「标准页面名」列的词。
2. 用户模糊说法（第二列）由客服归一成标准页面名，AI 只认标准页面名。
3. 包路径按 `com.qh.safe.<模块子包>.<类名>` 规整，非按业务页面硬塞进同一包。

## 扩展方式

新增页面时，在对应模块分组内追加一行：

```markdown
| 新页面标准名 | 用户常见说法 | NewXxxActivity | com.qh.safe.<模块>.NewXxxActivity |
```