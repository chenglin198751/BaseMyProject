package main;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PackTools {
    public static String Error_Msg = null;
    private static String SDK_VERSION_CODE = null;

    public static float formatFloat(float f, int scale) {
        BigDecimal b = new BigDecimal(f);
        return b.setScale(scale, RoundingMode.HALF_UP).floatValue();
    }

    public static final class Printer {
        private static final DateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);

        public static void print(String message) {
            String dateStr = format.format(new Date());
            System.out.println("[" + dateStr + "] " + message);
        }
    }

    /**
     * 修改gradle文件和一些配置，以便第1次打包
     */
    public static void modifyBuildGradleForFirst(String SRC_PATH) {
        try {
            List<String> aar_name_list = new ArrayList<>();
            File[] aars = new File(SRC_PATH + "/gamecenter_overseas_sdk/libs").listFiles();
            for (File aar_file : aars) {
                if (aar_file.getName().toLowerCase().endsWith(".aar")) {
                    aar_name_list.add(aar_file.getName().replace(".aar", ""));
                }
            }

            // 如果SDK的build.gradle文件中包含aar的配置，那么需要把关键字api换成compileOnly
            if (aar_name_list.size() > 0) {
                final String SDK_BUILD_GRADLE = SRC_PATH + "/gamecenter_overseas_sdk/build.gradle";
                Path path = Paths.get(new File(SDK_BUILD_GRADLE).getAbsolutePath());
                final List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

                for (int i = 0; i < lines.size(); i++) {
                    for (String aar_name : aar_name_list) {
                        if (lines.get(i).contains(aar_name)) {
                            lines.set(i, lines.get(i).replace("api", "compileOnly"));
                        }
                    }
                }
                Files.write(path, lines);
            }

        } catch (Throwable e) {
            PackTools.Error_Msg = e.toString();
            e.printStackTrace();
        }
    }

    /**
     * 修改gradle文件和一些配置，以便第2次打包
     */
    public static void modifyBuildGradleForSecond(String SRC_PATH) {
        modify_1(SRC_PATH);
        modify_2(SRC_PATH);
        modify_3(SRC_PATH);
    }

    /**
     * 把demo的build.gradle的dependencies{}， 修改为SDK的build.gradle的dependencies{}
     */
    private static void modify_1(String SRC_PATH) {
        final String START_STR = "--dependencies_start--";
        final String END_STR = "--dependencies_end--";
        final String START_STR_3 = "--flatDir_start--";
        final String END_STR_3 = "--flatDir_end--";

        // SDK的build.gradle文件
        final String BUILD_GRADLE_1 = SRC_PATH + "/gamecenter_overseas_sdk/build.gradle";
        final String TEXT_BUILD_GRADLE_1 = FileUtils.readFileString(new File(BUILD_GRADLE_1));
        int start_index_1 = TEXT_BUILD_GRADLE_1.indexOf(START_STR);
        start_index_1 = start_index_1 + START_STR.length();
        int end_index_1 = TEXT_BUILD_GRADLE_1.indexOf(END_STR);
        String sub_text_1 = TEXT_BUILD_GRADLE_1.substring(start_index_1, end_index_1);
        sub_text_1 = sub_text_1.replace("api", "implementation").replace("compileOnly", "implementation");


        // demo的build.gradle文件
        final String BUILD_GRADLE_2 = SRC_PATH + "/app_demo/build.gradle";
        final String TEXT_BUILD_GRADLE_2 = FileUtils.readFileString(new File(BUILD_GRADLE_2));
        int start_index_2 = TEXT_BUILD_GRADLE_2.indexOf(START_STR);
        start_index_2 = start_index_2 + START_STR.length();
        int end_index_2 = TEXT_BUILD_GRADLE_2.indexOf(END_STR);
        String sub_text_2 = TEXT_BUILD_GRADLE_2.substring(start_index_2, end_index_2);

        int start_index_3 = TEXT_BUILD_GRADLE_2.indexOf(START_STR_3);
        start_index_3 = start_index_3 + START_STR_3.length();
        int end_index_3 = TEXT_BUILD_GRADLE_2.indexOf(END_STR_3) - 4;
        String sub_text_3 = TEXT_BUILD_GRADLE_2.substring(start_index_3, end_index_3);

        // 1、把demo的build.gradle的dependencies{}，修改为SDK的build.gradle的dependencies{}
        // 2、把demo的build.gradle的 dirs 'libs', '../gamecenter_overseas_sdk/libs' 修改为dirs
        // 'libs'
        String TEXT_DEMO_GRADLE = TEXT_BUILD_GRADLE_2.replace(sub_text_2, sub_text_1)//
                .replace("//xxx-", "").replace(sub_text_3, "\n dirs 'libs'\n");
        FileUtils.writeFile(new File(BUILD_GRADLE_2), TEXT_DEMO_GRADLE);
    }

    /**
     * 把demo的settings.gradle的特定引用的include部分置空
     */
    private static void modify_2(String SRC_PATH) {
        final String START_STR3 = "--include_start--";
        final String END_STR3 = "--include_end--";

        // demo工程的settings.gradle文件
        final String BUILD_GRADLE_3 = SRC_PATH + "/settings.gradle";
        final String TEXT_SETTINGS_GRADLE_3 = FileUtils.readFileString(new File(BUILD_GRADLE_3));
        int start_index_3 = TEXT_SETTINGS_GRADLE_3.indexOf(START_STR3);
        start_index_3 = start_index_3 + START_STR3.length();
        int end_index_3 = TEXT_SETTINGS_GRADLE_3.indexOf(END_STR3);
        String sub_text_3 = TEXT_SETTINGS_GRADLE_3.substring(start_index_3, end_index_3);

        // 把demo的settings.gradle的特定引用的include部分置空
        String TEXT_SETTING_GRADLE = TEXT_SETTINGS_GRADLE_3.replace(sub_text_3, "");
        FileUtils.writeFile(new File(BUILD_GRADLE_3), TEXT_SETTING_GRADLE);
    }

    /**
     * 把SDK的libs目录复制到demo的libs目录下，并删除里面的jar。
     * 因为jar已经被打包到SDK的gamecenter_overseas_sdk.aar里了，所以无需再次引用。
     */
    private static void modify_3(String SRC_PATH) {
        final String SDK_LIBS = SRC_PATH + "/gamecenter_overseas_sdk/libs";
        final String DEMO_LIBS = SRC_PATH + "/app_demo/libs";
        FileUtils.copyDirectory(SDK_LIBS, DEMO_LIBS);
        File[] demoLibsDir = new File(DEMO_LIBS).listFiles();
        for (File file : demoLibsDir) {
            if (file.getName().toLowerCase().endsWith(".jar")) {
                file.delete();
            }
        }
    }

    /**
     * 修改EnvConstants.java文件中的值
     */
    public static void modifyEnvConstants(String SRC_PATH) {
        final String JAVA_FILE = SRC_PATH + //
                "/gamecenter_overseas_sdk/src/main/java/com/stimute/playcenter/common/env/EnvConstants.java";
        final String TEXT_JAVA_FILE = FileUtils.readFileString(new File(JAVA_FILE));
        String text_java2 = null;
        if (SRC_PATH.contains("_debug")) {
            text_java2 = TEXT_JAVA_FILE.replace("false", "true");
        } else if (SRC_PATH.contains("_release")) {
            text_java2 = TEXT_JAVA_FILE.replace("true", "false");
        }
        FileUtils.writeFile(new File(JAVA_FILE), text_java2);
    }

    // 获取SDK的版本号，以便打包时给zip命名
    public static String getSdkVersionCode(final String SRC_PATH) {
        if (SDK_VERSION_CODE != null) {
            return SDK_VERSION_CODE;
        }

        String version_path = SRC_PATH + //
                "/gamecenter_overseas_sdk/src/main/java/com/stimute/playcenter/sdk/overseas/version/HostSDKVersion.java";
        version_path = FileUtils.replacePath(version_path);

        String text = FileUtils.readFileString(new File(version_path));
        text = text.replace("\n", "").replace("\r", "").replace(" ", "");

        String arrs[] = text.split(";");
        for (int i = 0; i < arrs.length; i++) {
            if (arrs[i].contains("OVERSEAS_SDK_VERSION_CODE")) {
                String arrs2[] = arrs[i].split("=");
                for (int j = 0; j < arrs2.length; j++) {
                    if (arrs2[j].contains("OVERSEAS_SDK_VERSION_CODE")) {
                        if (arrs2[0].contains("OVERSEAS_SDK_VERSION_CODE")) {
                            SDK_VERSION_CODE = arrs2[1].replace("\"", "");
                        }
                    }
                }
            }
        }

        return SDK_VERSION_CODE;
    }
}
