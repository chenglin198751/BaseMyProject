package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final String PC_PNG_PATH = "D:/TestScreenshots/";
    private static final String ERROR_LOG = PC_PNG_PATH + "/error.log";
    private static String APPLICATION_LABEL = "";
    private static String PACKAGE_NAME = "";
    private static List<String> launchableActivities = new ArrayList<>();

    public static void main(String[] args) {
        EnvUtils.initTools();
        if (args.length == 0) {
            PackTools.Printer.print("请输入apk路径，或者apk所在文件夹");
            System.exit(0);
        }

        if (args[0].toLowerCase().endsWith(".apk")) {
            checkSingleApk(args[0]);
        } else {
            File dir = new File(args[0]);
            if (dir.isDirectory() && dir.length() > 0) {
                File[] apks = dir.listFiles();
                for (File apk : apks) {
                    checkSingleApk(apk.getAbsolutePath());
                }
            } else {
                PackTools.Printer.print("请输入apk所在文件夹");
            }
        }
    }

    private static void checkSingleApk(final String apkPath) {
        try {
            checkSingleApk2(apkPath);
        } catch (Exception e) {
            PackTools.Printer.print(e.toString());
            appendLog(e + "\n\n");
        }
    }

    private static void checkSingleApk2(final String apkPath) {
        if (!apkPath.toLowerCase().endsWith(".apk")) {
            PackTools.Printer.print("当前apk路径不合法：" + apkPath);
            return;
        }

        // 1、解析需要的包名等数据
        aapt2_dump_badging(apkPath);
        String png_path = "/sdcard/" + PACKAGE_NAME + ".png";

        // 2、安装apk，并且打开
        CmdTask2.Outs outs1 = new CmdTask2(new String[]{"adb", "install", apkPath}).run(true);
        if (outs1.getInputList().toString().contains("failed to install")) {
            appendLog(outs1.getInputList() + "\n\n");
        }

        // 有的游戏不正规，Manifest.xml中配置了多个启动launch activity，其实只有一个生效，别的无用。所以这里只能通过循环以此尝试打开
        for (String launcher : launchableActivities) {
            CmdTask2.Outs outs2 = new CmdTask2(new String[]{"adb", "shell", "am", "start", "-n", PACKAGE_NAME + '/' + launcher}).run(true);
            String outStr = outs2.getInputList().toString();
            if (!outStr.contains("Error: Activity class") && !outStr.contains("does not exist")) {
                break;
            } else {
                PackTools.Printer.print("previous launcher failed,try next launcher activity...");
            }
        }


        try {
            // 延时几秒后再截图
            final int seconds = 10;
            PackTools.Printer.print("sleep " + seconds + " seconds");
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 3、截图
        new CmdTask2(new String[]{"adb", "shell", "screencap", png_path}).run(true);

        // 4、如果pull到电脑硬盘成功，则删除手机上的图片
        CmdTask2.Outs outs = new CmdTask2(new String[]{"adb", "pull", png_path, PC_PNG_PATH}).run(true);
        for (String line : outs.getInputList()) {
            if (line.contains("1 file pulled")) {
                // 删除截图和卸载游戏
                new CmdTask2(new String[]{"adb", "shell", "rm", "-r", png_path}).run(true);
                new CmdTask2(new String[]{"adb", "uninstall", PACKAGE_NAME}).run(true);
                break;
            }
        }

        System.out.println('\n');
    }

    private static void aapt2_dump_badging(final String apkPath) {
        // 一定要清空这些全局变量
        APPLICATION_LABEL = "";
        PACKAGE_NAME = "";
        launchableActivities = new ArrayList<>();

        String[] cmds = {EnvUtils.getAapt2(), "dump", "badging", apkPath};
        CmdTask2 cmdTask = new CmdTask2(cmds);
        CmdTask2.Outs outs = cmdTask.run(false);
        List<String> list = outs.getInputList();
        for (String str : list) {
            if (str.contains("application: label=")) {
                String pattern = "application: label='([^']+)'";
                Pattern r = Pattern.compile(pattern);
                Matcher m = r.matcher(str);
                if (m.find()) {
                    APPLICATION_LABEL = m.group(1);
                }
            } else if (str.contains("package: name=")) {
                String pattern = "package: name='([^']+)'";
                Pattern r = Pattern.compile(pattern);
                Matcher m = r.matcher(str);
                if (m.find()) {
                    PACKAGE_NAME = m.group(1);
                }
            } else if (str.contains("launchable-activity: name=")) {
                String pattern = "launchable-activity: name='([^']+)'";
                Pattern r = Pattern.compile(pattern);
                Matcher m = r.matcher(str);
                while (m.find()) {
                    launchableActivities.add(m.group(1));
                }
            }
        }
    }

    private static void appendLog(String log) {
        File log_f = new File(ERROR_LOG);
        if (!log_f.exists()) {
            try {
                log_f.createNewFile();
            } catch (IOException e) {
                PackTools.Printer.print(e.toString());
            }
        }
        FileUtils.appendFile(log_f, log);
    }
}
