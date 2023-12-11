# AndroidStudio技巧

**1、gradle build 控制台输出中文乱码：**

    1、在gradle-wrapper.properties添加下面内容： org.gradle.jvmargs=-Dfile.encoding=UTF-8
    2、点击help -> edit custom vm options -> 打开sutdio64.exe.vmoptions -> 添加：-Dfile.encoding=UTF-8

**2、AS快捷键：**

    1、双击 ctrl 不松开，再按上下键，是启用多光标，此时可以批量修改代码
    2、ctrl + shift + U 切换大小写
    3、将代码块包裹到 if , try catch ，while 等语句中：ctrl + alt + T
    4、AS书签功能：F11 添加书签，shift + F11 打开书签列表
    5、ctrl + D 复制一行

**3、新版AS无法使用git提交代码解决：** 

    Version Control - Git - 勾选 Use credential helper

**4、显示上方的ToolBar：** 

    View - Appearance - ToolBar

**5、用Android Studio创建的jks签名文件，使用时报错，解决办法：**

    1、点击右上角 Project Structure 按钮，打开 SDK location
    2、把JDK location 修改为AS自带的jre，比如：D:\Android\Android Studio\jre

**6、一些操作：**

    Navigate → Type Hierarchy  ：查看类的继承关系
    类中右键：Generate ：产生XXX
    自动生成parcelable 的插件：Android Parcelable code generator
    自动转化 json 为类的插件 ：GsonFormat

**7、自定义上放主导航按钮：**

    File--->Settings--->Appearance& Behavior--->Menus and Toolbars--->Main Toolbar Left--->右上角+ Add Actions

**8、修改AS tab展示多个文件，默认AS中打开的文件个数是10个，当超过10个时，超过的部分会直接隐藏：**

    File-Settings-Editor-General-Editor Tabs-Show tabs in(Multiple rows)