package main;

import java.io.File;
import java.util.Objects;

public class Main {
    private static String mPluginDir;
    private static String mPluginJarName;
    private static String mHostPath;

    public static void main(String[] args) {
        EnvUtils.initTools();

//        if (args.length != 2) {
//            PackTools.Printer.print("Please enter plugin path and jar name");
//            System.exit(0);
//        }
//
//        mPluginDir = args[0]; //D:\AndroidCode\develop_xmkw\plugins\app_info
//        mApkJarName = args[1]; //recommend.jar
        mHostPath = EnvUtils.getCurrentPath() + "/host.apk";
        if (!(new File(mHostPath).exists())) {
            PackTools.Printer.print(mHostPath);
            PackTools.Printer.print("host.apk not exists");
            System.exit(0);
        }

        mPluginDir = "D:/AndroidCode/develop_xmkw/plugins/app_info";
        mPluginJarName = "recommend.jar";

//        String pluginApkPath = gradlewPluginApk();
//        if (pluginApkPath == null) {
//            PackTools.Printer.print("gradlew plugin apk failed");
//            System.exit(0);
//        }

        String pluginApkPath = "D:/AndroidCode/develop_xmkw/plugins/app_info/app/build/outputs/apk/debug/app-debug.apk";

        String pluginJarPath = copyPluginApk(pluginApkPath);

        String tempHostApk = replacePluginByWinRAR(pluginJarPath);

        String alignedApkPath = alignedHostApk(tempHostApk);

        signedHostApk(alignedApkPath);
    }


    /**
     * 编译构建插件apk
     */
    private static String gradlewPluginApk() {
        String pluginApkPath = null;
        File file = new File(mPluginDir);
        String name = file.getName();
        String[] cmds = {"cmd", "/c", "gradlew", ":plugins:" + name + ":app:assembleDebug"};
        CmdTask2 cmdTask = new CmdTask2(cmds, mPluginDir);
        cmdTask.run(true);
        File debugDir = new File(mPluginDir + "/app/build/outputs/apk/debug");
        if (debugDir.listFiles() != null) {
            for (File apk : Objects.requireNonNull(debugDir.listFiles())) {
                if (apk.getName().endsWith(".apk")) {
                    pluginApkPath = apk.getAbsolutePath();
                    break;
                }
            }
        }
        return pluginApkPath;
    }

    /**
     * 把编译好的插件apk复制到工作目录
     */
    private static String copyPluginApk(String pluginApkPath) {
        String dir = EnvUtils.getCurrentPath() + "/assets/plugins";
        String pluginJarPath = dir + "/" + mPluginJarName;
        File dir_f = new File(dir);
        if (!dir_f.exists()) {
            dir_f.mkdirs();
        }
        FileUtils.copyFile(pluginApkPath, pluginJarPath);
        return pluginJarPath;
    }

//    /**
//     * 使用WinRAR把host.apk中的插件jar给替换掉
//     */
//    private static String replacePluginByWinRAR(String pluginJarPath) {
//        String tempHostApk = mHostPath.replace(".apk", "_temp.apk");
//        FileUtils.copyFile(mHostPath, tempHostApk);
//
//        PackTools.Printer.print("1111111="+pluginJarPath);
////        String jarPath2 = EnvUtils.getCurrentPath().replace(EnvUtils.getCurrentPath() + "/","");
////        PackTools.Printer.print("22222="+jarPath2);
//        //"-ibck"
//        String[] cmds = {"tools/WinRAR/WinRAR.exe", "a", "-ep1","-o+", tempHostApk, "assets/plugins/"+mPluginJarName};
//        PackTools.Printer.print("2222222="+EnvUtils.getCurrentPath());
//        PackTools.Printer.print("333333333="+"assets/plugins/"+mPluginJarName);
//
////        String[] cmds = {"tools/WinRAR/WinRAR.exe", "a", "-ibck", "-o+", tempHostApk, "D:/work/assets/plugins/" + mApkJarName};
//
//
//        CmdTask2 cmdTask = new CmdTask2(cmds,EnvUtils.getCurrentPath());
//        cmdTask.run(true);
//        return tempHostApk;
//    }

private static String replacePluginByWinRAR(String pluginJarPath) {
    String tempHostApk = mHostPath.replace(".apk", "_temp.apk");
    FileUtils.copyFile(mHostPath, tempHostApk);

    // 用相对路径，并确认工作目录下有该文件
    String relativePath = "assets/plugins/";
    boolean exists = new File(EnvUtils.getCurrentPath(), relativePath).exists();
    System.out.println("File exists for WinRAR: " + exists); // 必须为 true

    String[] cmds = {"tools/WinRAR/WinRAR.exe", "a", "-ibck", "-o+", tempHostApk, relativePath};
    CmdTask2 cmdTask = new CmdTask2(cmds, EnvUtils.getCurrentPath());
    cmdTask.run(true);
    return tempHostApk;
}

    /**
     * 对齐替换后的apk
     */
    private static String alignedHostApk(String tempHostApk) {
        String alignedApkPath = EnvUtils.getCurrentPath() + "/aligned.apk";
        String[] cmds = {"tools/zipalign.exe", "-f", "4", tempHostApk, alignedApkPath};
        CmdTask2 cmdTask = new CmdTask2(cmds);
        cmdTask.run(true);
        return alignedApkPath;
    }

    /**
     * 重新签名apk
     */
    private static String signedHostApk(String alignedApkPath) {
        String signedApkPath = EnvUtils.getCurrentPath() + "/signed.apk";
        FileUtils.delete(signedApkPath);

        String[] cmds = {"java", "-jar", "tools/apksigner.jar", "sign",//
                "--v1-signing-enabled", "true", "--v2-signing-enabled", "true",//
                "--ks", "tools/debug.keystore", "--ks-key-alias",//
                "androiddebugkey", "--ks-pass", "pass:android", "--key-pass", "pass:android",//
                "--out", signedApkPath, alignedApkPath};

        CmdTask2 cmdTask = new CmdTask2(cmds);
        cmdTask.run(true);
        return signedApkPath;
    }
}
