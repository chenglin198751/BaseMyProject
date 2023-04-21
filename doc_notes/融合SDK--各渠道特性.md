# 融合SDK笔记

##### 1、4399测试版不能支付，必须用测试账号：

	账号：sytest4399   密码：sy4399

##### 2、应用宝渠道：
2-1、portal-sdk-tool\games\game18\config.xml中，<param name="YSDK_URL" value="https://ysdk.qq.com"/>。
YSDK_URL的配置必须是 https://ysdk.qq.com ，不能是 https://ysdk.qq.com/，后面有个/一定无法登录；

2-2、如果是沙盒支付环境，就配置：https://ysdktest.qq.com ，正式配置https://ysdk.qq.com

2-3、支付是米大师，测试阶段要发布沙盒，游戏配置也要是沙盒的AppKey。
正式阶段才能发布正式，游戏配置也要改成正式的AppKey。AppKey示例：
沙箱AppKey：ML2Daxf6E4dYgt20
现网AppKey：4iRyowZBZvJgQpvqLVgu9Q69bLWU9zCQ

2-4、融合后台，应用宝的兑换比例一定要设置，否则之后后，服务器传递金额为0，支付失败：
![](https://p0.ssl.qhimg.com/t01220f7e4224823b34.png)

2-5、应用宝渠道在应用宝后台必须设置和游戏相对应的区服信息，否则无法支付

3、B站渠道在B站客户端授权登录时，如果出现 target_appkey is not match ，那么不用管，这个上线之后会自己好，看如下截图：

![](https://p0.ssl.qhimg.com/t01e39f6f6b81c97cf5.png)

##### 3、黑鲨渠道：

	打包时资源错误提示，比如黑鲨渠道：
	error: Multiple substitutions specified in non-positional format; did you mean to add the formatted="false" attribute?
	错误原因：因为是aapt打包资源，所以会出错。如果改成aapt2打包资源就不会出错了。
	解决方案：加上formatted="false"，把下面的第一行改成第二行就行了。
	<string name="shark_install_service_app_hint">您尚未%s新版“黑鲨游戏服务”插件%s</string>
	<string name="shark_install_service_app_hint" formatted="false">您尚未%s新版“黑鲨游戏服务”插件%s</string>
