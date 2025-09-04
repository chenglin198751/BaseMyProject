package main;

import java.io.File;
import java.util.Map;
import java.util.Objects;

public class Main {
    private final static String ASSETS_PLUGINS = "assets/plugins";
    private static String mPluginDir;
    private static String mPluginJarName;
    private static String mHostPath;

    public static void main(String[] args) {
        EnvUtils.initTools();
        Map<String, String> configMap = ProFileReader.readProFile("plugin_configs.properties");

        if (configMap.isEmpty()) {
            PackTools.Printer.print("plugin_configs.properties is empty!");
            System.exit(0);
        }

        mHostPath = EnvUtils.getCurrentPath() + "/host.apk";
        //示例：D:\AndroidCode\develop_xmkw\plugins\app_info
        mPluginDir = configMap.get("plugin_dir");
        //示例：recommend.jar
        mPluginJarName = configMap.get("plugin_jar_name");

        if (!(new File(mHostPath).exists())) {
            PackTools.Printer.print(mHostPath);
            PackTools.Printer.print("host.apk not exists");
            System.exit(0);
        }

        // 1.先编译构建指定的插件apk
        String pluginApkPath = gradlewPluginApk();
        if (pluginApkPath == null) {
            PackTools.Printer.print("gradlew plugin apk failed");
            System.exit(0);
        }

        // 2.把编译好的插件apk复制到工作目录
        copyPluginApk(pluginApkPath);

        // 3.使用WinRAR无损替换插件apk
        String tempHostApk = replacePluginByWinRAR();

        // 4.对齐替换后的apk
        String alignedApkPath = alignedHostApk(tempHostApk);

        // 5.重新签名apk
        String signedApkPath = signedHostApk(alignedApkPath);

        // 6.安装apk
        installApk(signedApkPath);

        // 7.删除无用文件
        deleteUselessFiles();
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
        String dir = EnvUtils.getCurrentPath() + "/" + ASSETS_PLUGINS;
        String pluginJarPath = dir + "/" + mPluginJarName;
        File dir_f = new File(dir);
        if (!dir_f.exists()) {
            dir_f.mkdirs();
        }
        FileUtils.copyFile(pluginApkPath, pluginJarPath);
        return pluginJarPath;
    }

    /**
     * 使用WinRAR无损替换插件apk
     */
    private static String replacePluginByWinRAR() {
        String tempHostApk = mHostPath.replace(".apk", "_temp.apk");
        FileUtils.copyFile(mHostPath, tempHostApk);
        String relativePath = ASSETS_PLUGINS + "/";
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

    private static void installApk(String signedApkPath) {
        String[] cmds = {"adb", "install", signedApkPath};
        CmdTask2 cmdTask = new CmdTask2(cmds);
        cmdTask.run(true);
    }

    private static void deleteUselessFiles() {
        FileUtils.delete(EnvUtils.getCurrentPath() + "/host_temp.apk");
        FileUtils.delete(EnvUtils.getCurrentPath() + "/signed.apk.idsig");
        FileUtils.delete(EnvUtils.getCurrentPath() + "/aligned.apk");
    }
}
