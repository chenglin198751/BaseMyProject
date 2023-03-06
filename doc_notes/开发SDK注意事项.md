
**1、如果是游戏SDK，那么所有的R资源引用必须用getIdentifier（参看工具类getResUtils），否则如果使用者使用eclipse接入就会报R文件找不到的问题。**

**2、混淆配置很重要，流程如下：**

    首先明确一个问题：aar配置了混淆，demo apk也配置了混淆，那么打包结果是取的二者全集

**3、编译SDK流程：**

    1、第一次编译：aar内置自定义混淆规则，apk一定不要配置自定义混淆规则。二者混淆开关都设置为开启。
    编译apk，拿出各个library的aar。此时拿到的aar就是按照aar的自定义混淆规则生成的代码。
    这些aar不能作为对外输出给CP的aar，因为有可能混淆配置错误。所以需要第2步的再次编译验证。

    2、第二次编译：把上面的aar都复制到demo工程对应aar目录下，修改build.gradle的aar引用，
    把APK的混淆设置为对外CP的混淆（注意，不是aar的自定义混淆，一定要是对外让CP配置的规则），
    此时再编译apk，得到的apk就是真正模拟CP的真正apk。经过这一步aar的混淆也相当于经过了CP验证。

    3、对第2步进行补充说明：如果第一步打包输出的是多个aar，想把多个aar合并为一个aar，
    那么在第2步的时候，需要先把aar解压，然后建立一个空的library让demo工程引用，
    把解压aar的资源文件都复制到library的对应目录，此时再打包，这个library生成的aar就是把多个aar合并后的结果。
    当然，如果你不想合并aar，就不需要这么做了。

**4、混淆结果的一级目录不能出现a.a,a.b这种路径。因为如果别的SDK也混淆了也会产生a.a,a.b这种路径，那么就会导致类冲突**

    所以需要保留固定包路径：
    -keeppackagenames com.devnn.*   一颗星表示保留com.devnn下的包名不被混淆，而子包下的包名和类名都会被混淆；
    -keeppackagenames com.devnn.**  两颗星表示保留com.devnn下的所有包名不被混淆，类都会被混淆。
    特别注意，使用-keeppackagenames时，必须保证指定的包名下，至少有一个类，否则不生效。

**5、引用的第三方Library越少越好；**
    
    某些常用库，比如图片库，网络库，可以修改包名后引用，避免和接入方版本冲突

**6、对外提供的方法，越精简越好。**

    所有的方法，最好都有回调，回调用json格式。比如初始化方法，无论任何原因的初始化失败，都要回调给接入方；
    一个小技巧，初始化可以用CustomInitProvider实现，这样第一是方便接入方，第二是防止接入方初始化的太晚引起SDK异常。



**最后附上一些常用的混淆代码：**

    # 不混淆某个类的类名，及类中的内容
    -keep class cn.coderpig.myapp.example.Test { *; }
    
    # 不混淆指定包名下的类名，不包括子包下的类名
    -keep class cn.coderpig.myapp*
    
    # 不混淆指定包名下的类名，及类里的内容
    -keep class cn.coderpig.myapp* {*;}
    
    # 不混淆指定包名下的类名，包括子包下的类名
    -keep class cn.coderpig.myapp**
    
    # 不混淆某个类的子类
    -keep public class * extends cn.coderpig.myapp.base.BaseFragment
    
    # 不混淆实现了某个接口的类
    -keep class * implements cn.coderpig.myapp.dao.DaoImp
    
    # 不混淆类名中包含了"entity"的类，及类中内容
    -keep class **.*entity*.** {*;}
    
    # 不混淆内部类中的所有public内容
    -keep class cn.coderpig.myapp.widget.CustomView$OnClickInterface {
        public *;
    }
    
    # 不混淆指定类的所有方法
    -keep cn.coderpig.myapp.example.Test {
        public <methods>;
    }
    
    # 不混淆指定类的所有字段
    -keep cn.coderpig.myapp.example.Test {
        public <fields>;
    }
    
    # 不混淆指定类的所有构造方法
    -keep cn.coderpig.myapp.example.Test {
        public <init>;
    }
    
    # 不混淆指定参数作为形参的方法
    -keep cn.coderpig.myapp.example.Test {
        public <methods>(java.lang.String);
    }
    
    # 不混淆类的特定方法
    -keep cn.coderpig.myapp.example.Test {
        public test(java.lang.String);
    }
    
    # 不混淆native方法
    -keepclasseswithmembernames class * {
        native <methods>;
    }
    
    # 不混淆枚举类
    -keepclassmembers enum * {
      public static **[] values();
      public static ** valueOf(java.lang.String);
    }
    
    #不混淆资源类
    -keepclassmembers class **.R$* {
        public static <fields>;
    }
    
    # 不混淆自定义控件
    -keep public class * entends android.view.View {
        *** get*();
        void set*(***);
        public <init>;
    }

