package main;

import java.io.File;

/**
 * 首先明确一个问题：aar配置了混淆，demo apk也配置了混淆，那么打包结果是取的二者全集
 * <p>
 * 编译SDK流程：
 * <p>
 * 1、第一次编译：aar内置自定义混淆规则，apk一定不要配置自定义混淆规则。二者混淆开关都设置为开启。
 * 编译apk，拿出各个library的aar。此时拿到的aar就是按照aar的自定义混淆规则生成的代码。
 * 这些aar不能作为对外输出给CP的aar，因为有可能混淆配置错误。所以需要第2步的再次编译验证。
 * <p>
 * 2、第二次编译：把上面的aar都复制到demo工程对应aar目录下，修改build.gradle的aar引用，
 * 把APK的混淆设置为对外CP的混淆（注意，不是aar的自定义混淆，一定要是对外让CP配置的规则），
 * 此时再编译apk，得到的apk就是真正模拟CP的真正apk。经过这一步aar的混淆也相当于经过了CP验证。
 * <p>
 * 3、对第2步进行补充说明：如果第一步打包输出的是多个aar，想把多个aar合并为一个aar，
 * 那么在第2步的时候，需要先把aar解压，然后建立一个空的library让demo工程引用，
 * 把解压aar的资源文件都复制到library的对应目录，此时再打包，这个library生成的aar就是把多个aar合并后的结果。
 * 当然，如果你不想合并aar，就不需要这么做了。
 * <p>
 * by weichenglin1 2022-10-10
 */
public class SeasMainPack {
    private static volatile int zip_count = 0;
    private static final String[] BUILD_TYPES = {"debug", "release"};
    private static String QBUILD_OUT;
    private static String QBUILD_PATH;
    private static long start_time;
    private static boolean isLocalPkg = false;

    public static void main(String[] args) {
        PackTools.Printer.print("----args[0]=" + args[0]);
        PackTools.Printer.print("----args[1]=" + args[1]);
        PackTools.Error_Msg = null;

        QBUILD_OUT = args[0];
        QBUILD_PATH = args[1];

        // ---java打包开始---
        PackTools.Printer.print("----java package start----");
        start_time = System.currentTimeMillis();

        pkgBefore();
        pkgSdking();
        pkgAfter();

        // ---java打包结束---
        float minutes = (System.currentTimeMillis() - start_time) / 1000f / 60f;
        minutes = PackTools.formatFloat(minutes, 2);
        PackTools.Printer.print("----java package finished,it takes " + minutes + " minutes----");
    }

    // 打包SDK开始前的准备工作
    private static void pkgBefore() {
        PackTools.Printer.print("----pkgBefore----");
        if (QBUILD_OUT.equals("game-overseas-sdk_output") && QBUILD_PATH.equals("game-overseas-sdk")) {
            isLocalPkg = true;
        }

        // 如果是本地打包，那么需要重新获取当前打包代码的路径
        if (isLocalPkg) {
            String sh_dir = new File("").getAbsolutePath();
            String master_src_dir = new File(sh_dir).getParentFile().getParent();

            QBUILD_OUT = new File(master_src_dir).getParent() + "/" + QBUILD_OUT;

            PackTools.Printer.print("deleting " + QBUILD_OUT);
            File file_qbuild_out = new File(QBUILD_OUT);
            FileUtils.delete(file_qbuild_out.getAbsolutePath());
            file_qbuild_out.mkdirs();

            QBUILD_PATH = master_src_dir;
            PackTools.Printer.print("local_package_sdk.sh--args[0]=" + QBUILD_OUT);
            PackTools.Printer.print("local_package_sdk.sh--args[1]=" + QBUILD_PATH);
        }
    }

    // 打包SDK结束后的善后工作
    private static void pkgAfter() {
        while (true) {
            try {
                Thread.sleep(15 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 打包失败了
            if (PackTools.Error_Msg != null) {
                PackTools.Printer.print("----\n\njava package failed:" + PackTools.Error_Msg + "\n\n----");
                System.exit(0);
            }

            // 打包成功
            if (zip_count == BUILD_TYPES.length) {
                PackTools.Printer.print("package sdk length is [" + zip_count + "] finished");
                System.exit(0);
                break;
            }
        }
    }

    // 打包SDK
    private static void pkgSdking() {
        zip_count = 0;

        for (int i = 0; i < BUILD_TYPES.length; i++) {
            final int index = i;
            new Thread() {
                public void run() {
                    if (PackTools.Error_Msg != null) {
                        System.exit(0);
                        return;
                    }

                    try {
                        final String SRC_PATH = new File(QBUILD_PATH).getParent() + "/game-overseas-sdk_" + BUILD_TYPES[index];
                        final String SDK_OUT = QBUILD_OUT + "/" + BUILD_TYPES[index];

                        if (isLocalPkg) {
                            PackTools.Printer.print("deleting SRC_PATH = " + SRC_PATH);
                            FileUtils.delete(SRC_PATH);
                            FileUtils.delete(SDK_OUT);
                        }

                        new File(SDK_OUT + "/proj-input/libs").mkdirs();
                        new File(SDK_OUT + "/demo").mkdirs();
                        new File(SDK_OUT + "/demo-src").mkdirs();

                        FileUtils.copyDirectory(QBUILD_PATH, SRC_PATH);
                        PackTools.modifyEnvConstants(SRC_PATH);
                        FileUtils.delete(SRC_PATH + "/.git");

                        // 执行第一次打包
                        PackTools.Printer.print("----1111 first package sdk start for " + BUILD_TYPES[index] + "----");
                        PackTools.modifyBuildGradleForFirst(SRC_PATH);
                        exe_build_demo_apk_sh(SRC_PATH, BUILD_TYPES[index]);

                        if (BUILD_TYPES[index].equals("release")) {
                            copyMappingToOutput(SRC_PATH, SDK_OUT);
                        }

                        PackTools.modifyBuildGradleForSecond(SRC_PATH);
                        copySdkAarToDemoForBuild(SRC_PATH, BUILD_TYPES[index]);

                        // 执行第二次打包
                        PackTools.Printer.print("----2222 second package sdk start for " + BUILD_TYPES[index] + "----");
                        exe_build_demo_apk_sh(SRC_PATH, BUILD_TYPES[index]);
                        copySdkAarsToOutput(SRC_PATH, SDK_OUT);
                        copyDemoApkToOutput(SRC_PATH, SDK_OUT, BUILD_TYPES[index]);
                        copyDemoSrc(SRC_PATH, SDK_OUT);
                        String sdkCode = PackTools.getSdkVersionCode(SRC_PATH);
                        String zipName = ("hayio-game-overseas-sdk-v111-222.zip").replace("111", sdkCode).replace("222", BUILD_TYPES[index]);
                        zipName = SDK_OUT.replace(BUILD_TYPES[index], zipName);
                        ZipUtils.zip(SDK_OUT, zipName);

                        // 打包完成删除无用文件
                        FileUtils.delete(SRC_PATH);
                        FileUtils.delete(SDK_OUT);

                        zip_count++;
                    } catch (Exception e) {
                        PackTools.Error_Msg = e.toString();
                        PackTools.Printer.print("打包失败：" + e.toString());
                        System.exit(0);
                    }
                }

                ;
            }.start();

        }

    }

    private static void copyMappingToOutput(final String SRC_PATH, final String SDK_OUT) {
        new File(SDK_OUT + "/mapping").mkdirs();
        FileUtils.copyFile(SRC_PATH + "/gamecenter_overseas_sdk/build/outputs/mapping/release/mapping.txt", //
                SDK_OUT + "/mapping/gamecenter_overseas_sdk_mapping.txt");
    }

    private static void copySdkAarToDemoForBuild(String SRC_PATH, String type) {
        String from_aar = SRC_PATH + ("/gamecenter_overseas_sdk/build/outputs/aar/gamecenter_overseas_sdk-xx.aar").replace("xx", type);
        String to_aar = SRC_PATH + ("/app_demo/libs/gamecenter_overseas_sdk.aar");
        FileUtils.copyFile(from_aar, to_aar);
    }

    private static void copySdkAarsToOutput(String SRC_PATH, String SDK_OUT) {
        String from_aars = SRC_PATH + ("/app_demo/libs");
        String to_aars = SDK_OUT + ("/proj-input/libs");
        FileUtils.copyDirectory(from_aars, to_aars);
    }

    private static void copyDemoApkToOutput(String SRC_PATH, String SDK_OUT, String type) {
        String from_apk = SRC_PATH + ("/app_demo/build/outputs/apk/xx/app_demo-xx.apk").replace("xx", type);
        String to_apk = SDK_OUT + ("/demo/sdk_demo_v111.apk").replace("111", PackTools.getSdkVersionCode(SRC_PATH));
        FileUtils.copyFile(from_apk, to_apk);
    }

    // 执行 ../proj_help/build/sh/build_demo_apk.sh
    private static void exe_build_demo_apk_sh(String SRC_PATH, final String type) {
        if (PackTools.Error_Msg != null) {
            throw new RuntimeException(PackTools.Error_Msg);
        }

        final String sh_path = SRC_PATH + "/proj_help/build/sh/build_demo_apk.sh";
        final String bashCommand = "sh " + sh_path + " " + SRC_PATH + " " + type;
        PackTools.Printer.print("-----exe_build_demo_apk_sh----" + bashCommand);
        CmdTask cmdTask = new CmdTask(bashCommand, null);
        cmdTask.run(true);
    }

    private static void copyDemoSrc(String SRC_PATH, String SDK_OUT) {
        FileUtils.delete(SRC_PATH + "/app_demo/build");
        FileUtils.delete(SRC_PATH + "/gamecenter_common");
        FileUtils.delete(SRC_PATH + "/plugin_sys_library_common");
        FileUtils.delete(SRC_PATH + "/plugin_sys_library_to_host");
        FileUtils.delete(SRC_PATH + "/plugin_sys_library_to_plugin_common");
        FileUtils.delete(SRC_PATH + "/plugin_sys_plugin_001");
        FileUtils.delete(SRC_PATH + "/gamecenter_overseas_sdk");
        FileUtils.delete(SRC_PATH + "/proj_help");
        FileUtils.delete(SRC_PATH + "/.safedk");
        FileUtils.delete(SRC_PATH + "/.idea");
        FileUtils.delete(SRC_PATH + "/.gradle");
        FileUtils.copyDirectory(SRC_PATH, SDK_OUT + "/demo-src");
        FileUtils.delete(SDK_OUT + "/demo-src/app_demo/build");
        FileUtils.delete(SDK_OUT + "/demo-src/build");
        FileUtils.delete(SDK_OUT + "/demo-src/key.properties");
    }
}
