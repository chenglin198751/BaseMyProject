# 工程各个工具类说明

1、BaseActivity 类 ：里面把最外层的View 给封装好了，用的时候调用setContentLayout塞进去你的layout 即可。好处：便于统一管理页面，比如可以在页面内部显示加载框，可以添加右侧菜单，添加空View，
无网页面等等。

2、debug切换：我是在SD卡下面放置一个特殊文件，判断是否有这个文件来切换debug 环境。好处：可以随时把一个正式包设置为测试包，并且提交代码时再也不用去配置环境了。

3、内建广播：BaseActivity 里面我基于LocalManagerBroadcast 封装了一个广播，可以很方便的发广播接收广播，不需要注册，因为我自动帮你注册了。使用很方便，性能很高且资源消耗也很低。建议以后所有
的异步消息传递都用这个，解耦性强。甚至startActivityForResult 都可以启用，都统一使用内建广播传递消息。

4、MyApplication 类：程序的入口，这里我把Application的Context 给持有了，获取的方法叫做getApp() 。切记，工程里凡是要用到Context的地方，都要引用MyApplication.getApp() 方法，以免造成内存泄漏
。还有，这里面有一句话叫做mAppHelper.isAppMainProcess() ，是判断的主UI 进程的。因为Application的onCreate()有多少个进程就会执行多少次，而有些代码我们只需要在UI 进程执行，所以这里可以用上述
方法判断。

5、MyBaseAdapter 类 ：对BaseAdapter简单的封装了下，统一了增加数据，删除数据的方法。不用大家每个adapter都要写增删数据的方法，使用也很简单。为了以后的维护方便，建议大家都使用这个作为
baseAdapter 。

6、HttpUtils 类：Http请求类，是基于OKHttp3封装的，让使用起来变的很简单，一眼就知道怎么用。

7、WebImageView 类：图片加载类。使用的是square 公司出品的picasso ，并做了简单的封装，封装的目的是以后可以方便的换其他的图片加载工具。封装为：WebImageView ，具体用法是一定要传入当前
ImageView的宽和高，以便最大限度的节约内存空间，让滑动更流畅，并且避免OOM。

8、PullToRefresh 类：实现了上拉加载更多，下拉刷新。封装自android-Ultra-Pull-To-Refresh-With-Load-More ，它可以包裹任意的View ，还可以很随意的定制自己的header 和footer 。目前发现的唯一的缺
点是：上拉加载更多时尾部会回弹回去。这个缺点暂时能忍受，如果以后想换别的下拉刷新工具，也可以随时换，因为PullToRefresh 是我封装过的，只需要实现我封装的这几个方法就可以。这也是设计模式中包装
者模式的好处。

9、ReplaceViewHelper 类：一个封装的工具类，可以很方便的替换任意的View 为另一个View 。

10、MyLog 类：打Log日志的工具，debug 开启时才打Log .

11、MyUtils 类：统一的工具类，里面包含了很多常用的方法并写了注释，大家可以看看，基本常用的方法都包含了。

12、特别说明：MyUtills 里面有个方法叫做getHandler() ，这是一个全局的Handler ，当然，我获取的是MainLooper 。以后大家执行post 或者postDelay操作时就可以直接用这个全局Handler 了。（特别注意，
如果使用postDelay方法时，一定要记得onDestroy时调用removeCallback 方法，要不很容易内存泄漏）

13、MyDialog 类：封装了dialog ，系统的dialog样式太难看，并且根据手机的不同显示的样式也不同，所以我自己封装了一个。使用很简单，看一眼就会。

14、MyToast 类：统一弹Toast 的类，系统的Toast 样式太丑，这个是我自己定制的，可以随意定制，并且解决了系统toast 一直弹出的问题。

15、MyProgress 类：自己封装的加载loading框。

16、MyWebViewActivity 类：基于腾讯X5浏览器内核封装的统一WebViewActivity ，性能卓越。具体介绍可以去看X5内核。

17、EasyCache 类：一个基于文件的存储工具，可以很方便的存储很长的字符串，且效率很高。如果大家想做缓存，建议用这个。

18、PopupWindowUtils 类：我封装的可以在任意View 下方弹出popupWindows 的工具。使用很方便。

19、SDCardUtils 类 ：我封装的获取SD 卡存储路径的工具类，统一操作存储路径，不能随便在SD卡建立文件夹而影响用户体验。

20、Constants 类 ：公共常量类，存放了屏幕分辨率，全局gson 之类的，可以根据需要继续添加。

21、MyUri 类：用schema 方式开发时的统一管理类，如果不用schema开发，就可以不用管这个。

22、UserManager 类：用户信息管理类，如果牵涉到用户登录之类的，要用这个类来管理，方便别人使用。

23、FastBlurUtil 类：被我改良过的高斯模糊（毛玻璃）工具，效率非常高，使用很灵活。

24、OnFinishListener 类 ：我自己定义的一个接口，可以作为一个万能回调使用。别的地方任何如果只是临时性的回调，可以用它来做，不需要再写个回调接口了。

25、BitmapUtils 类：压缩图片 以及得到图片宽高的类，具体使用看注释。

