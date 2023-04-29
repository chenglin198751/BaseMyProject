# python学习笔记

**1、获取文件的父路径 ：**

    parent_dir = os.path.dirname(dex_path)

**2、遍历当前路径 ：**

    sdk_files = os.listdir(sdkDestDir) 遍历当前目录

**3、判断字符串以xx开头和和以xx结尾 ：**

    if sdk_f.startswith('yxsdk_') and sdk_f.endswith(".jar"):

**4、文件路径拼接 ：**

    sdk_f2 = os.path.join(sdkDestDir, sdk_f)

**5、返回path中文件夹部分 ：**

    os.path.dirname(path)
    示例：如果是 E:/workspace/game2/decompile
    那么：会返回 E:/workspace/game2

**6、返回path的绝对路径 ：**

    os.path.abspath(af)

    示例：比如遍历文件夹时，输出遍历后的文件的绝对path：
    path = 'E:/360portalgamesdk/portal-sdk-tool'
    files = os.listdir(path)
    for af in files:
        abs_f = os.path.abspath(af)
        print "abspath = " + abs_f