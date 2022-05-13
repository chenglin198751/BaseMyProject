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

**3、gradle build 控制台输出中文乱码：**

    1、在gradle-wrapper.properties添加下面内容： org.gradle.jvmargs=-Dfile.encoding=UTF-8
    2、点击help -> edit custom vm options -> 打开sutdio64.exe.vmoptions -> 添加：-Dfile.encoding=UTF-8

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