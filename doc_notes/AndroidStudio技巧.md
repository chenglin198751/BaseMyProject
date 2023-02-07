# AndroidStudio技巧

**1、gradle build 控制台输出中文乱码：**

    1、在gradle-wrapper.properties添加下面内容： org.gradle.jvmargs=-Dfile.encoding=UTF-8
    2、点击help -> edit custom vm options -> 打开sutdio64.exe.vmoptions -> 添加：-Dfile.encoding=UTF-8

**2、AS快捷键：**

    1-1、双击 ctrl 不松开，再按上下键，是启用多光标，此时可以批量修改代码
    1-2、ctrl + shift + U 切换大小写
    1-3、将代码块包裹到 if , try catch ，while 等语句中：ctrl + alt + T
    1-4、AS书签功能：F11 添加书签，shift + F11 打开书签列表
    1-5、ctrl + D 复制一行

**3、新版AS无法使用git提交代码解决：** Version Control - Git - 勾选 Use credential helper