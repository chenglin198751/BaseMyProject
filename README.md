## 工程各个工具类说明

**1、BaseActivity 类** ：基础Activity类，把最外层的View 给封装好了，用的时候调用setContentLayout塞进去你的layout 即可。
        好处：便于统一管理页面，比如可以在页面内部显示加载框，可以添加右侧菜单，添加空View，无网页面等等。
        其中里面包含了Post() 网络请求的方法，建议使用这个请求网络，可以防止内存泄漏。
        BaseFragment里面的方法和和BaseActivity大体类似。所有的Fragment都要继承自BaseFragment

**2、debug切换**：我是在SD卡下面放置一个特殊文件，判断是否有这个文件来切换debug 环境。好处：可以随时把一个正式包设置为测试包，并且提交代码时再也不用去配置环境了。

**3、内建广播**：BaseActivity 里面我基于LocalManagerBroadcast 封装了一个广播，可以很方便的发广播接收广播，不需要注册，因为我自动帮你注册了。使用很方便，性能很高且资源消耗也很低。
        建议以后所有的异步消息传递都用这个，解耦性强。甚至startActivityForResult 都可以弃用，都统一使用内建广播传递消息。

**4、MyApp 类**：程序的入口，这里我把Application的Context 给持有了，获取的方法叫做getApp() 。切记，工程里凡是要用到Context的地方，MyApp.getApp() 方法，以免造成内存泄漏。
        还有，这里面有一句话叫做AppHelper.isAppMainProcess() ，是判断的主UI 进程的。
        因为Application的onCreate()有多少个进程就会执行多少次，而有些代码我们只需要在UI 进程执行，所以这里可以用上述方法判断。

**5、BaseListViewAdapter 类 && BaseRecyclerViewAdapter 类**：对BaseAdapter简单的封装了下，统一了增加数据，删除数据的方法。不用大家每个adapter都要写增删数据的方法，使用也很简单。
        为了以后的维护方便，建议大家都使用这个作为baseAdapter 。

**6、HttpUtils 类**：Http请求类，是基于OKHttp3封装的，让使用起来变的很简单，一眼就知道怎么用。

**7、WebImageView 类**：图片加载类。使用的是square 公司出品的picasso ，并做了简单的封装，封装的目的是以后可以方便的换其他的图片加载工具。封装为：WebImageView .
        我已经写了支持GIF的方法，性能卓越。具体用法是一定要传入当前ImageView的宽和高，以便最大限度的节约内存空间，让滑动更流畅，并且避免OOM。

**8、PullToRefresh 类**：实现了上拉加载更多，下拉刷新。封装自https://github.com/scwang90/SmartRefreshLayout ，它可以很随意的定制自己的header 和footer ，并有很多属性可以设置，极为好用。

**9、ReplaceViewHelper 类**：一个封装的工具类，可以很方便的替换任意的View 为另一个View 。

**10、MyLog 类**：打Log日志的工具，debug 开启时才打Log .

**11、MyUtils 类**：统一的工具类，里面包含了很多常用的方法并写了注释，大家可以看看，基本常用的方法都包含了。

**12、特别说明**：MyUtils 里面有个方法叫做getHandler() ，这是一个全局的Handler ，当然，我获取的是MainLooper 。以后大家执行post 或者postDelay操作时就可以直接用这个全局Handler 了。
    （特别注意，如果使用postDelay方法时，一定要记得onDestroy时调用removeCallback 方法，要不很容易内存泄漏）

**13、MyDialog 类**：封装了dialog ，系统的dialog样式太难看，并且根据手机的不同显示的样式也不同，所以我自己封装了一个。使用很简单，看一眼就会。

**14、MyToast 类**：统一弹Toast 的类，系统的Toast 样式太丑，这个是我自己定制的，可以随意定制，并且解决了系统toast 一直弹出的问题。

**15、MyProgress 类**：自己封装的加载loading框，可以随意定制UI 。

**16、MyWebViewActivity 类**：基于腾讯X5浏览器内核封装的统一WebViewActivity ，性能卓越。具体介绍可以去看X5内核。

**17、EasyCache 类**：一个基于文件的存储工具，可以很方便的存储很长的字符串，且效率很高。如果大家想做缓存，建议用这个。

**18、PopupWindowUtils 类**：我封装的可以在任意View 下方弹出popupWindows 的工具。使用很方便。

**19、SDCardUtils 类**：我封装的获取SD 卡存储路径的工具类，统一操作存储路径，不能随便在SD卡建立文件夹而影响用户体验。

**20、Constants 类**：公共常量类，存放了屏幕分辨率，全局gson 之类的，可以根据需要继续添加。

**21、MyUri 类**：用schema 方式开发时的统一管理类，如果不用schema开发，就可以不用管这个。

**22、UserManager 类**：用户信息管理类，如果牵涉到用户登录之类的，要用这个类来管理，方便别人使用。

**23、FastBlurUtil 类**：被我改良过的高斯模糊（毛玻璃）工具，效率非常高，使用很灵活。

**24、OnFinishListener 类** ：我自己定义的一个接口，可以作为一个万能回调使用。别的地方任何如果只是临时性的回调，可以用它来做，不需要再写个回调接口了。

**25、BitmapUtils 类**：压缩图片 以及得到图片宽高的类，里面有个方法叫createScaledBitmap() ，可以按比例创建缩放的图片，性能卓越。具体使用看注释。

**26、MyTabLayout 类**：根据谷歌官方的TabLayout自己封装的ViewPager标题指示器，实现了自定义View。

**27、NoScrollGridView 类**：不可滚动的GridView ，适用于放在ListView ，RecyclerView中。

**28、NoScrollViewPager 类**：不可滚动的ViewPager，具体使用场景自己发挥。

**29、com.youth.banner.Banner 类**：github 地址：//https://github.com/youth5201314/banner 一个很优秀的实现自动滚动banner的库。
        另外，里面有WeakHandler这个防止内存泄露的Handler类使用。还有各种的ViewPager Transformer可以使用。 。

**30、KeepScaleImageView 类**：按照Bitmap的宽高比，保持ImageView宽高比。需要在设置图片源之前调用setWidth()方法设置控件宽度

**31、SelectPhotosActivity 类**：一个通用的相册图片选择类，支持单选和多选，用法看代码。

**32、EnglishCharFilter 类**：限制中文字符算作两个字，英文字符算作一个字的工具类。用法：editText.setFilters(new InputFilter[]{new EnglishCharFilter(MAX_COUNT)});

**33、SimpleAnimatorListener 类**：一个简单的动画监听类，目的是减少代码量。只监听了动画结束，因为动画结束是最常用的

**34、ZoomImageView 类**：一个支持手指缩放的ImageView ，支持在ViewPager中使用

**35、CustomViewPagerIndicator 类**：自定义的ViewPager指示器

**36、LongImageView 类**：自定义的用于显示长图的控件。基于WebView改造而来，性能卓越。

**37、CenterDrawable 类**：自定义的可以居中显示一个小图片的类。比如可以用于设置一个图片的未显示图片之前的默认图。
        用法：imageView.setImageDrawable(new CenterDrawable(R.mipmap.image_loadding_icon))

**38、PolygonImageView 类**：github 地址：https://github.com/AlbertGrobas/PolygonImageView
        一个实现多边形的ImageView 类。四边形，五边形，六边形。另外还有星星，撕纸形状等，还可以自定义形状。

**39、RoundedImageView 类**：一个可以实现圆形图片、圆角图片的类。github 地址：https://github.com/vinc3m1/RoundedImageView
        app:riv_corner_radius="30dp" 圆角的角度
        app:riv_border_width="2dp" 图片边框的宽度
        app:riv_border_color="#333333" 图片边框的颜色
        app:riv_mutate_background="false" 是否需要显示控件的背景色，默认是显示
        app:riv_oval="true" 是否展示为圆形，如果true ，那么riv_corner_radius 不生效

**40、CornerLinearLayout && CornerRelativeLayout 类**：自定义的可以显示圆角的View 。在布局中设置圆角用android:tag="20"

**41、UpdateDialog 类**：一套完善的检查更新的对话框，稍作改动就可以实现复杂的检查更新逻辑