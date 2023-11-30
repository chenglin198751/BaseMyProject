# python学习笔记

**1、获取文件的父路径 ：**

    parent_dir = os.path.dirname(dex_path)

**2、遍历当前路径 ：**

    sdk_files = os.listdir(sdkDestDir) 遍历当前目录

**3、判断字符串以xx开头和和以xx结尾 ：**

    if sdk_f.startswith('yxsdk_') and sdk_f.endswith(".jar"):

**4、文件路径拼接 ：**

    sdk_f2 = os.path.join(sdkDestDir, sdk_f)

**6、返回path的绝对路径 ：**

    os.path.abspath(af)

    示例：比如遍历文件夹时，输出遍历后的文件的绝对path：
    path = 'E:/360portalgamesdk/portal-sdk-tool'
    files = os.listdir(path)
    for af in files:
        abs_f = os.path.abspath(af)
        print "abspath = " + abs_f

**7、shutil使用方法 ：**

    shutil.copytree(src, dst)：复制文件夹，注意，目标路径dst必须不存在才能复制
    shutil.copy(src, dst)：目标dst即可以是文件路径，也可以是目录路径
    shutil.move(src, dst)：相当于rename重命名操作

**8、python3.5增加的os.scandir()获取文件 ：**

    with os.scandir(path) as entries:
        for entry in entries:
            print(entry.name)
            print(entry.path)
            print(entry.stat().st_ctime)

            print(entry.path)
            if entry.is_file():
                print("文件")
            elif entry.is_dir():
                print("目录")

**9、os.walk()获取一个文件夹下所有的文件：**

    # dir_path 当前遍历的文件夹的绝对路径
    # dir_names 当前文件夹下的所有子文件夹的名称（仅一层，孙子文件夹不包括）
    # file_names 保存当前文件夹下的所有文件的名称
    for dir_path, dir_names, file_names in os.walk(path):
        for f in file_names:
            print(os.path.abspath(f))