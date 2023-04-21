# 融合SDK笔记

**1、修改 intent-filter 中的data值，比如修改下方的 wexin_appid ，和修改android:taskAffinity="applicationId.diff**

```xml
        <activity
            android:name="com.tencent.ysdk.module.user.impl.wx.YSDKWXEntryActivity"
            android:taskAffinity="applicationId.diff">
            <!-- TODO GAME android:taskAffinity这里为游戏微信登录的配置，游戏需要修改为自己的包名加.diff -->
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <!-- TODO GAME android:scheme这里为游戏微信登录的配置，游戏需要修改为自己的微信appid -->
                <data android:scheme="wexin_appid" />
            </intent-filter>
        </activity>
```
##### 写法如下：
```python
    if activityNodes != None and len(activityNodes) > 0:
        for activityNode in activityNodes:
            activityName = activityNode.get(name)
            if activityName == 'com.tencent.ysdk.module.user.impl.wx.YSDKWXEntryActivity':
				### 修改android:taskAffinity的值
                activityNode.set(taskAffinity, packageName + ".diff")
                intentFilters = activityNode.findall('intent-filter')
                if intentFilters != None and len(intentFilters) > 0:
                    for intentNode in intentFilters:
                        dataNodes = intentNode.findall('data')
                        for dataNode in dataNodes:
						### 修改 wexin_appid：
                            if dataNode.get(scheme).find('wexin_appid') >= 0:
                                dataNode.set(scheme, wxAppID)
                                break
                        break
```
#### 2、在AndroidManifest.xml中加入一行meta-data，比如：
```xml
 <meta-data android:name="close_privacy" android:value="1"/>
```
##### 写法如下：
```python
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
```
#### 3、apk_utils.merge_res_xml_values()方法是自动去除xml重复值。目前注释掉了，原因：现在用AndroidStudo开发生成jar了，会自动检查重复资源文件
```python
在生成R文件(generateNewRFile()方法)之前，需要把res/values下的xml去除重复值
apk_utils.merge_res_xml_values(decompileDir)
```