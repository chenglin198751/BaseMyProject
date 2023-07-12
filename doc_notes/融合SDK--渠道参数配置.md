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

**7、对整个游戏配置，games.xml中添加数数appid，注意，每个游戏appid一定要单独申请。示例：**

    <param name="taAppId" value="9ae899c5dd1243318a545a0a20cc1f82"/>

**8、有的渠道需要单独的keystore，比如应用宝，因为战火使命游戏，之前上线过应用宝，所以必须用旧的签名文件才行**
    
    参考如下路径：
    ...\games\game27\channels\2707\keystore\keystore.xml

**9、配置投放渠道参数等：对游戏下的某个渠道配置，示例，game27下的config.xml中，360独代渠道，对外投放，配置投放渠道的各个参数如下：**
    
    //字节跳动的巨量引擎的app id
    <param name="ByteDance_toufang_id" value="481568"/>

**10、配置一个游戏是否开启接入推送SDK，并且配置各个推送通道的参数，在每个游戏下的games.xml配置：**

    <push>
        <param name="push_sdk_switch" value="true"/>
        <param name="um_push_app_key" value="6465c3a38c5c724f27370192"/>
        <param name="um_push_message_secret" value="30da8ce99e03bd605c54930ab3dc598f"/>
        <param name="xiaomi_push_app_id" value="2882303761518668828"/>
        <param name="xiaomi_push_app_key" value="5141866839828"/>
        <param name="oppo_push_app_key" value="34a18bd669aa4610a47ea15aed4712dd"/>
        <param name="oppo_push_master_secret" value="503557ff3bd64f05b2fb47304491016e"/>
		<param name="vivo_push_app_key" value="405f1d43ca35dadf84fd49290e948db5"/>
		<param name="vivo_push_app_id" value="104481124"/>
    </push>

    参数说明：
    1、push_sdk_switch:总开关，是否开启推送
    2、xiaomi,oppo,vivo参数，如果开通了厂商通道就配置，未开通就不要配置；华为比较特殊，必须写在manifest中（默认已写入）
    3、xiaomi,vivo,友盟，必须配置游戏的多个包名；华为,oppo不支持多包名配置，所以华为oppo只能在当前应用商店的包名才能收到push；
    4、友盟推送可以用随意的手机调试，但是调试4大厂商推送时，必须用对应厂商手机调试

**11、微信app_id参数配置，用于分享，在每个游戏下的games.xml配置：**

    <param name="wx_app_id" value="wxe0abe2c40588394b"/>

**12、while打包apkpkg.jar的机关：**

    1、portal-sdk-tool\BUILD_CONFIG\reset_switch : 放置此文件，可以重置所有的打包状态。切记，用完要删除此文件。
    2、portal-sdk-tool\BUILD_CONFIG\chmod777.config : 放置此文件，内容配置如路径：portal-sdk-tool/games/game2/channels，
        比如以portal-sdk-tool开头，以回车符分割，那么这些文件夹或者文件会被修改为chmod777的权限。切记，用完一定要删除。

**13、自动替换一下字符串为包名：**

    ${applicationId}
    yxme_sdk_application_id