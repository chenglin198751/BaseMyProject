package main;

import java.io.File;

public class EnvUtils {
    public static final String temp_dir = System.currentTimeMillis() + "";
    public static final String JAR_PATH = new File(System.getProperty("java.class.path")).getAbsolutePath();
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static String mWinPath = null;

    public static boolean isWindows() {
        return OS.contains("windows");
    }

    public static boolean isMac() {
        return OS.contains("mac");
    }

    public static boolean isLinux() {
        return OS.contains("linux");
    }

    public static String getWorkPath() {
        return getWindowsCachePath() + "/check_workspace/" + temp_dir;
    }

    public static String getCurrentPath() {
        return new File(JAR_PATH).getParent();
    }

    public static void initTools() {
        File work_dir = new File(EnvUtils.getWorkPath());
        work_dir.mkdirs();

        String java_name = new File(EnvUtils.JAR_PATH).getName();
        String target_jar = EnvUtils.getWorkPath() + "/" + java_name;

        FileUtils.copyFile(EnvUtils.JAR_PATH, target_jar);
        ZipUtils.unZip(target_jar, EnvUtils.getWorkPath());
    }

    private static String getWindowsCachePath() {
        if (mWinPath == null) {
            mWinPath = System.getenv("USERPROFILE");
            if (!new File(mWinPath).exists()) {
                mWinPath = System.getenv("TEMP");
            }
            if (!new File(mWinPath).exists()) {
                mWinPath = System.getProperty("java.io.tmpdir");
            }
            if (mWinPath.endsWith(File.separator)) {
                mWinPath = mWinPath.substring(0, mWinPath.length() - 1);
            }
        }
        return mWinPath;
    }

    public static String getToolsPath() {
        return getWorkPath() + "/tools";
    }

    public static String getApksigner() {
        return EnvUtils.getToolsPath() + "/apksigner.jar";
    }

    public static String getAapt2() {
        return EnvUtils.getToolsPath() + "/aapt2.exe";
    }
}
