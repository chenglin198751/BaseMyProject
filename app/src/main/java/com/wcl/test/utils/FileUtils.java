package com.wcl.test.utils;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.wcl.test.base.BaseApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    private static final String TAG = "FileUtils";

    /**
     * 不需要存储权限
     * <p>
     * 现在的Android应用将文件放到SD卡上时总是随便创建一个目录，那这样有个问题就是卸载应用时，
     * 这些垃圾还留在用户的SD卡上导致占用存储空间（猎豹清理大师这样的工具由此应用而生）。
     * 其实Android系统已经帮我们提供了相关的API可以将文件缓存到data/data目录下，
     * 当APP卸载时，这些垃圾文件也跟着自动卸载清除了。
     * <p>
     * 2021-05-21 修正补充：
     * 由于安卓11对文件存储有很大限制，导致data/data无法正常使用。故此方法弃用.
     * 所以存储统一改为使用 getExternalFilesDir() 方法
     * 获取外部存储卡路径：比如：/storage/emulated/0/Android/data/包名/files
     */
    public static String getExternalPath() {
        File file = BaseApp.getApp().getExternalFilesDir("");
        if (file == null) {
            file = BaseApp.getApp().getFilesDir();
        }
        return file.getAbsolutePath();
    }

    public static long getFolderSize(File folder) {
        long size = 0;
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files == null) {
                return size;
            }
            for (File file : files) {
                if (file.isFile()) {
                    size += file.length();
                } else {
                    size += getFolderSize(file);
                }
            }
        } else {
            size += folder.length();
        }
        return size;
    }

    public static void delete(String file2) {
        File file = new File(file2);
        if (!file.exists()) return;

        if (file.isFile()) {
            boolean deleted = file.delete();
            if (!deleted) {
                Log.e(TAG, "Failed to delete file: " + file2);
            }
            return;
        }

        File[] files = file.listFiles();
        if (files != null) {
            for (File subFile : files) {
                delete(subFile.getAbsolutePath());
            }
        }

        boolean deleted = file.delete();
        if (!deleted) {
            Log.e(TAG, "Failed to delete directory: " + file2);
        }
    }

    public static void writeFile(String file_path, String text) {
        if (TextUtils.isEmpty(file_path)) return;

        File file = new File(file_path);
        if (!file.exists()) {
            try {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            } catch (IOException e) {
                Log.e(TAG, "Failed to create file: " + file_path, e);
                return;
            }
        }

        try (FileWriter fileWriter = new FileWriter(file_path, true)) {
            fileWriter.write(text);
        } catch (IOException e) {
            Log.e(TAG, "Failed to write file: " + file_path, e);
        }
    }

    private static String readFileString(String file_path) {
        if (TextUtils.isEmpty(file_path)) return null;

        try {
            Path path = Paths.get(file_path);
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read file as string: " + file_path, e);
        }
        return null;
    }

    public static List<String> readFileLines(String filePath) {
        List<String> lines = new ArrayList<>();
        if (TextUtils.isEmpty(filePath)) return lines;

        try {
            Path path = Paths.get(filePath);
            lines.addAll(Files.readAllLines(path, StandardCharsets.UTF_8));
        } catch (IOException e) {
            Log.e(TAG, "Failed to read file lines: " + filePath, e);
        }
        return lines;
    }

    public static void writeFileLines(String filePath, Iterable<String> lines) {
        if (TextUtils.isEmpty(filePath)) return;

        File file = new File(filePath);
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try (FileWriter fileWriter = new FileWriter(file, false)) {
                for (String line : lines) {
                    fileWriter.write(line);
                    fileWriter.write(System.lineSeparator());
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to write file lines: " + filePath, e);
        }
    }

    public static void copyDirectory(File fromDir, File toDir) {
        if (!fromDir.isDirectory()) return;

        if (!toDir.exists() && !toDir.mkdirs()) {
            Log.e(TAG, "Failed to create target directory: " + toDir.getAbsolutePath());
            return;
        }

        File[] files = fromDir.listFiles();
        if (files == null) return;

        for (File file : files) {
            File targetFile = new File(toDir, file.getName());
            if (file.isDirectory()) {
                copyDirectory(file, targetFile);
            } else {
                copyFile(file, targetFile);
            }
        }
    }

    public static void copyFile(File source, File dest) {
        if (source == null || dest == null) return;

        try {
            if (dest.exists() && !dest.delete()) {
                Log.e(TAG, "Failed to delete existing file: " + dest.getAbsolutePath());
                return;
            }

            if (!dest.getParentFile().exists() && !dest.getParentFile().mkdirs()) {
                Log.e(TAG, "Failed to create parent directories for: " + dest.getAbsolutePath());
                return;
            }

            Files.copy(source.toPath(), dest.toPath());
        } catch (IOException e) {
            Log.e(TAG, "Failed to copy file from " + source.getAbsolutePath() + " to " + dest.getAbsolutePath(), e);
        }
    }
}
