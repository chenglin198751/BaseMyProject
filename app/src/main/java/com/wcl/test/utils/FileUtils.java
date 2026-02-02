package com.wcl.test.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.wcl.test.base.BaseApp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    private static final String TAG = "FileUtils";

    /**
     * 获取应用私有的可写文件目录路径（不需要存储权限）
     * 1、优先使用外部存储的 App 私有目录：
     * /storage/emulated/0/Android/data/{packageName}/files
     * 2、当外部存储不可用时，回退到内部存储：
     * /data/data/{packageName}/files
     */
    public static String getAppFilesPath() {
        Context context = BaseApp.getApp();
        File dir = context.getExternalFilesDir(null);
        if (dir == null) {
            dir = context.getFilesDir();
        }
        return dir.getAbsolutePath();
    }

    /**
     * 递归计算文件或文件夹的总大小
     *
     * @param folder 文件或目录
     * @return 字节数，异常时返回 0
     */
    public static long getFolderSize(File folder) {
        if (folder == null || !folder.exists()) {
            return 0;
        }

        if (folder.isFile()) {
            return folder.length();
        }

        long size = 0;
        File[] files = folder.listFiles();
        if (files == null) {
            return 0;
        }

        for (File file : files) {
            size += getFolderSize(file);
        }
        return size;
    }

    /**
     * 删除文件或目录（递归）
     * - 如果是文件，直接删除
     * - 如果是目录，先删除子文件再删除目录本身
     *
     * @param path 文件或目录路径
     */
    public static void delete(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        deleteInternal(new File(path));
    }

    private static void deleteInternal(File file) {
        if (!file.exists()) {
            return;
        }

        if (file.isFile()) {
            if (!file.delete()) {
                Log.e(TAG, "Failed to delete file: " + file.getAbsolutePath());
            }
            return;
        }

        File[] files = file.listFiles();
        if (files != null) {
            for (File sub : files) {
                deleteInternal(sub);
            }
        }

        if (!file.delete()) {
            Log.e(TAG, "Failed to delete directory: " + file.getAbsolutePath());
        }
    }

    /**
     * 追加写入文本到文件（UTF-8）
     * - 文件不存在会自动创建
     * - 父目录不存在会自动创建
     * - 以追加方式写入
     *
     * @param filePath 文件路径
     * @param text     要写入的内容
     */
    public static void writeFile(String filePath, String text) {
        if (TextUtils.isEmpty(filePath) || text == null) {
            return;
        }

        File file = new File(filePath);
        ensureParentDir(file);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(text);
        } catch (IOException e) {
            Log.e(TAG, "Failed to write file: " + filePath, e);
        }
    }

    /**
     * 以 UTF-8 编码读取整个文件内容为字符串
     *
     * @param filePath 文件路径
     * @return 文件内容，失败返回 null
     */
    private static String readFileString(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }

        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read file: " + filePath, e);
        }
        return null;
    }

    /**
     * 按行读取文件（UTF-8）
     *
     * @param filePath 文件路径
     * @return 行列表，失败返回空列表
     */
    public static List<String> readFileLines(String filePath) {
        List<String> lines = new ArrayList<>();
        if (TextUtils.isEmpty(filePath)) {
            return lines;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            return lines;
        }

        try {
            lines.addAll(Files.readAllLines(file.toPath(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            Log.e(TAG, "Failed to read file lines: " + filePath, e);
        }
        return lines;
    }

    /**
     * 覆盖写入多行文本到文件（UTF-8）
     *
     * <p>
     * - 原文件内容会被清空
     * - 每行自动追加系统换行符
     *
     * @param filePath 文件路径
     * @param lines    文本行集合
     */
    public static void writeFileLines(String filePath, Iterable<String> lines) {
        if (TextUtils.isEmpty(filePath) || lines == null) {
            return;
        }

        File file = new File(filePath);
        ensureParentDir(file);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to write file lines: " + filePath, e);
        }
    }

    /**
     * 递归复制目录
     *
     * @param fromDir 源目录
     * @param toDir   目标目录
     */
    public static void copyDirectory(File fromDir, File toDir) {
        if (fromDir == null || toDir == null || !fromDir.isDirectory()) {
            return;
        }

        if (!toDir.exists() && !toDir.mkdirs()) {
            Log.e(TAG, "Failed to create directory: " + toDir.getAbsolutePath());
            return;
        }

        File[] files = fromDir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            File target = new File(toDir, file.getName());
            if (file.isDirectory()) {
                copyDirectory(file, target);
            } else {
                copyFile(file, target);
            }
        }
    }

    /**
     * 复制单个文件
     *
     * @param source 源文件
     * @param dest   目标文件
     */
    public static void copyFile(File source, File dest) {
        if (source == null || dest == null || !source.exists()) {
            return;
        }

        ensureParentDir(dest);

        try {
            Files.copy(source.toPath(), dest.toPath());
        } catch (IOException e) {
            Log.e(TAG, "Failed to copy file from " + source + " to " + dest, e);
        }
    }

    // ---------------- Internal ----------------

    /**
     * 确保父目录存在
     */
    private static void ensureParentDir(File file) {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }
}
