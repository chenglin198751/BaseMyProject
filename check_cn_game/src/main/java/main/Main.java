package main;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        initTools();
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
        boolean isGoogleSigner = false;
        boolean isSignerShort = false;
        boolean containsChinese = false;

        String[] cmd_print_certs = {"java", "-jar", EnvUtils.getApksigner(), "verify", "--print-certs", apkPath};
        CmdTask2 cmdTask = new CmdTask2(cmd_print_certs);
        CmdTask2.Outs outs1 = cmdTask.run(false);
        List<String> list1 = outs1.getInputList();

        String[] cmd_aapt2_dump_resources = {EnvUtils.getAapt2(), "dump", "resources", apkPath};
        CmdTask2 cmdTask2 = new CmdTask2(cmd_aapt2_dump_resources);
        CmdTask2.Outs outs2 = cmdTask2.run(false);
        List<String> list2 = outs2.getInputList();

        for (String str : list1) {
            if (str.contains("CN=Android, OU=Android, O=Google Inc., L=Mountain View, ST=California, C=US")) {
                // 1.第一个检查规则：只要是谷歌签名的，则大概率是英文的
                isGoogleSigner = true;
            } else if (str.contains("certificate DN:")) {
                // 2.第二个检查规则：如果证书主题（certificate DN）较短，则大概率是中文。
                // 原因是国内一些小作坊不专业，生成证书时很多名称不填。
                String str2 = str.split("certificate DN:")[1].trim();
                PackTools.Printer.print(str2);
                if (str.split("certificate DN:")[1].trim().length() <= 25) {
                    isSignerShort = true;
                }
            }
        }

        // 3.第三个检查规则：如果values/strings.xml有中文，大概率是汉化。
        // 原因是国内小作坊在汉化游戏时，并不会可以区分不同的多国语言，而是放到默认语言目录
        for (String str : list2) {
            if (str.contains("resource") && str.contains("string/")) {
                if (CommonUtils.containsChinese(str)) {
                    containsChinese = true;
                    break;
                }
            }
        }

        PackTools.Printer.print("isGoogleSigner:" + isGoogleSigner + ",isSignerShort:" + isSignerShort + ",containsChinese:" + containsChinese);
        String outStr = "游戏名:"+getLabel(apkPath) + " 路径:" + apkPath + '\n';
        if (!isGoogleSigner || isSignerShort || containsChinese) {
            PackTools.Printer.print("已汉化 " + outStr);
        } else {
            PackTools.Printer.print("未汉化 " + outStr);
        }
    }

    private static void initTools() {
        File work_dir = new File(EnvUtils.getWorkPath());
        work_dir.mkdirs();

        String java_name = new File(EnvUtils.JAR_PATH).getName();
        String target_jar = EnvUtils.getWorkPath() + "/" + java_name;

        FileUtils.copyFile(EnvUtils.JAR_PATH, target_jar);
        ZipUtils.unZip(target_jar, EnvUtils.getWorkPath());
    }

    private static String getLabel(final String apkPath) {
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
                    return m.group(1);
                }
            }
        }
        return "";
    }
}
