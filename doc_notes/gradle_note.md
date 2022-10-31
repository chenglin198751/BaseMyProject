# gradle知识记录

**1、SDK demo或者aar 引入外部路径jar或者aar的方法：**

    1、引入外部路径jar：
        implementation fileTree(dir: '../../tools/exclude_libs', include: ['*.jar'])

    2、引入别的module的aar：
        repositories {
            flatDir {
                dirs 'libs'
                dirs project(':BaseSdk').file('libs')
            }
        }
        implementation(name: 'analyse_sdk_v_100008', ext: 'aar')

**2、gradle编译完成之后做事情，比如执行复制apk之类的操作：**

        def copyMainPluginApk(final String build_type) {
            //$project.buildDir是当前build路径
            def from_path = "$project.buildDir/outputs/apk/release/"

            //this.getRootDir()是当前工程路径，是个File
            File file = this.getRootDir()
        }

        //在assembleDebug或者assembleRelease执行之后，再执行方法copyMainPluginApk()
        project.afterEvaluate {
            assembleDebug.doLast {
                copyMainPluginApk("assembleDebug")
            }
            assembleRelease.doLast {
                copyMainPluginApk("assembleRelease")
            }
        }

        //gradle各种生命周期：详细见：https://www.jianshu.com/p/2e19268bf387
        gradle.settingsEvaluated {
            println "settings：执行settingsEvaluated..."
        }

        gradle.projectsLoaded {
            println "settings：执行projectsLoaded..."
        }

        gradle.projectsEvaluated {
            println "settings: 执行projectsEvaluated..."
        }

        gradle.beforeProject { proj ->
            println "settings：执行${proj.name} beforeProject"
        }

        gradle.afterProject { proj ->
            println "settings：执行${proj.name} afterProject"
        }

        gradle.buildStarted {
            println "构建开始..."
        }

        gradle.buildFinished {
            println "构建结束..."
        }

**3、空白**

**4、gradle 读取文件内容：**

    def sdk_plat = getSdkPlat()
    def getSdkPlat() {
        def file = file('../build_variants')
        String sdk_plat = ''
        file.eachLine { line ->
            sdk_plat = line
        }
        sdk_plat = sdk_plat.replace('\n','')
        return sdk_plat;
    }

**5、gradle 加载文件内容为数组map：**

    1、定义的文件内容：
    keyAlias=xxxxx
    keyPassword=bbbbbbbb
    storePassword=aaaaaaaaaaaaaa

    2、读取文件：
    def keystorePropertiesFile = rootProject.file("../key.properties")
    def keystoreProperties = new Properties()
    keystoreProperties.load(new FileInputStream(keystorePropertiesFile))

    3、使用keystoreProperties：
    keystoreProperties['storeFile']

    4、按行读取文件：
    def file = file(file_pah)
    file.eachLine { line ->
        println(line)
    }

**6、gradle 编译添加监听：**

    // beforeEvaluate()要想生效，代码必须方到settings.gradle中
    gradle.addProjectEvaluationListener(new ProjectEvaluationListener() {
        @Override
        void beforeEvaluate(Project project) {
            println("${project.name} 项目配置之前调用")
        }

        @Override
        void afterEvaluate(Project project, ProjectState state) {
            println("${project.name} 项目配置之后调用")
        }
    })

    // gradle 监听项目的生命周期
    gradle.addBuildListener(new BuildListener() {
        @Override
        void buildStarted(Gradle gradle) {
            // println('构建开始')
        }

        @Override
        void settingsEvaluated(Settings settings) {
            // println('settings 文件解析完成')
        }

        @Override
        void projectsLoaded(Gradle gradle) {
            // println('项目加载完成')
        }

        @Override
        void projectsEvaluated(Gradle gradle) {
            // println('项目解析完成')
        }

        @Override
        void buildFinished(BuildResult result) {
            // println('构建完成')
        }
    })

**7、gradle 执行命令行：**

        exec {
            def mCommand = ["cmd", "/c","jar","-xvf", sdk_jar]
            workingDir "../sdk/config"
            commandLine mCommand
        }

**8、gradle 执行执行压缩和解压缩：**

    执行任务并传递参数：gradlew zip -Pparams=123
    在任务内获取参数：project.getProperty("params") 或者 "$params"
    判断是否有参数：project.hasProperty("apk_name")

    task zip(type: Zip){
        from 'C:/work/AndroidCode/BaseMyProject/app/libs'
        archiveFileName = 'libs.zip'
        destinationDirectory = file('C:/work/AndroidCode/BaseMyProject/app/src')
    }
    
    task unzip(type: Copy) {
        from zipTree('C:/work/AndroidCode/BaseMyProject/app/src/libs.zip')
        into 'C:/work/AndroidCode/BaseMyProject/test'
    }

**9、gradle 下载的aar路径：**

    C:\Users\weichenglin1\.gradle\caches\modules-2\files-2.1\androidx.viewpager2\viewpager2\1.0.0\91c378a09ddff66e1bb73e90edeac53487d2832b\viewpager2-1.0.0.aar

**10、gradle 定义变量，集合，map等：**

    1、定义变量：def str = 'aaa' ，def是弱类型的，groovy会自动根据情况来给变量赋予对应的类型 2、当然也可以直接用java定义：String str = 'aaa'
    1、定义一个集合：def list = ['a','b']  2、往list中添加元素：list << 'c'  3、取出list中第三个元素：list.get(2)
    1、定义一个map：def map = ['key1':'value1','key2':'value2'] 2、向map中添加键值对：map.key3 = 'value3' 3、取出key3的值：map.get('key3')

**11、gradle 遍历文件目录的方法：**

    1、
    def sdk_jar_parent_dir = 'D:/aaaa'
    files(file(sdk_jar_parent_dir).listFiles()).each { file ->
        println('xxxx=' + file)
    }

    2、用FileTree遍历，include表示包含的文件类型，exclude表示排除的文件类型
    def sdk_jar_parent_dir = 'D:/aaaa'
    FileTree tree = fileTree(dir: sdk_jar_parent_dir, include: ['**/*.java', '**/*.xml'], exclude: '**/*aapt*/**')
    tree.each { File file ->
        println('xxxx=' + file)
    }
    或者：
    tree.visit {FileTreeElement element ->
        println "$element.relativePath => $element.file"
    }