# 融合SDK--开发点注意

** 1、关于闪屏配置：**

    拿game2举例：
	game2/config.xml中，属性：<param name="splash" value="21"/>
	splash字段：0是没有闪屏 21是横屏 11是竖屏

    闪屏图片所在目录必须为：
	横屏：360portalgamesdk\portal-sdk-tool\games\game2\channels\215\splash\21\drawable-xhdpi\yx_splash.png
	竖屏：360portalgamesdk\portal-sdk-tool\games\game2\channels\215\splash\11\drawable-xhdpi\yx_splash.png

    其中game2为游戏对应的gameId，215为渠道号，闪屏可以分渠道配置。
    相同的游戏，每个渠道可以有不同的闪屏。其中game2为游戏对应的gameId ，215为渠道号，闪屏可以分渠道配置。
    相同的游戏，每个渠道可以有不同的闪屏。

**2、为每个游戏单独配置是否关闭隐私协议弹框：**

    情景1、 关闭整个游戏的隐私协议弹框：
        在AndroidManifest.xml中用python写入meta-data值，close_privacy=1表示关闭融合SDK自带隐私协议：
        需要配置在单个游戏的scripts中，路径如：portal-sdk-tool\games\game2\scripts。代码如下：
    
        def modifyManifest(game, channel, decompileDir, packageName):
            manifest = decompileDir + '/AndroidManifest.xml'
            ET.register_namespace('android', androidNS)
            tree = ET.parse(manifest)
            root = tree.getroot()
            appNode = root.find('application')
            name = '{' + androidNS + '}name'
            value = '{' + androidNS + '}value'
            childNode = SubElement(appNode, 'meta-data')
            childNode.set(name, 'close_privacy')
            childNode.set(value, '1') 
            tree.write(manifest, 'UTF-8')
            return 0

    情景2、 关闭游戏对应的某个渠道的隐私协议弹框：
        在对应的渠道，包路径com.yxme.sdk下，从bili渠道复制类YxmeFirstClass.java，然后在此类的构造方法中将变量赋值为true即可：
    
        public YxmeFirstClass() {
            com.yxme.sdk.Constants.isClosePrivacy = true;
        }


**3、华为SDK在非华为手机上，不弹出更新HMS框的原因是缺少如下配置：**

        <meta-data 
			android:name="com.huawei.hms.client.channel.androidMarket" 
			android:value="false"/>

**4、为每个渠道配置单独的游戏名字：**

        <channel>
            <param name="id" value="1908"/>
            <param name="suffix" value="com.tencent.tmgp.dljh"/>
            <param name="gameName" value="斗笠江湖"/>
            <sdk-params>
                .....................
            </sdk-params>
        </channel>


**5、用自己的yxme-sdk.jar替换游戏的yxme-sdk.jar：**

    只需要在路径放置：portal-sdk-tool\games\game18\yxme-sdk.jar,即可用自己的jar覆盖游戏接入的jar。
    因为游戏出包很耗时，所以是为了不让游戏重新接入jar。此目的是为了方便自己测试。正式打包，还是需要游戏正式接入新的jar。

**6、YxmeFirstClass 最先执行主Activity onCreate()：**

    此类是在主Activity onCreate()中最先初始化的类。由于隐私协议问题，所以init方法延后了，
    导致某些必须提前设置的方法无法被设置。所以，此类最先执行游戏主Activity onCreate()。

**7、games.xml中添加数数appid，注意，每个游戏appid一定要单独申请。示例：**

    <param name="taAppId" value="9ae899c5dd1243318a545a0a20cc1f82"/>

**8、有的渠道需要单独的keystore，比如应用宝，因为战火使命游戏，之前上线过应用宝，所以必须用旧的签名文件才行**
    
    参考如下路径：
    ...\games\game27\channels\2707\keystore\keystore.xml

**9、360独代，对外的投放渠道，配置投放渠道的各个app_id如下：**
    
    //字节跳动的巨量引擎的app id
    <param name="ByteDance_toufang_id" value="481568"/>