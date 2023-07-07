package com.wcl.test.utils;

import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import com.wcl.test.base.BaseApp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
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

    /**
     * 不需要存储权限
     * <p>
     * 现在的Android应用将文件放到SD卡上时总是随便创建一个目录，那这样有个问题就是卸载应用时，
     * 这些垃圾还留在用户的SD卡上导致占用存储空间（猎豹清理大师这样的工具由此应用而生）。
     * 其实Android系统已经帮我们提供了相关的API可以将文件缓存到data/data目录下，
     * 当APP卸载时，这些垃圾文件也跟着自动卸载清除了。
     * <p>
     * 2021-05-21 修正补充：
     * 由于安卓11对文件存储有很大限制，导致sdcard/data/data无法正常使用。故此方法弃用.
     * 所以存储统一改为使用 getExternalPath() 方法
     * 获取外部存储卡路径：比如：sdcard/Android/data/data/包名/cache
     */
    public static String getExternalPath() {
        return BaseApp.getApp().getExternalCacheDir().getAbsolutePath();
    }

    /**
     * 得到文件夹大小
     */
    public static long getFolderSize(File file) {
        long size = 0;
        File[] fileList = file.listFiles();
        if (fileList == null || fileList.length == 0) {
            return size;
        }

        for (File value : fileList) {
            if (value.isDirectory()) {
                size = size + getFolderSize(value);
            } else {
                size = size + value.length();
            }
        }
        return size / (1024 * 1024);
    }

    /**
     * 删文件或者目录
     */
    public static void deleteDirectory(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File files[] = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    deleteDirectory(files[i]);
                }
            }
            file.delete();
        }
    }

    /**
     * 清空文件夹下的文件，而不删除文件夹
     */
    public static boolean clearDirectory(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                clearDirectory(path + File.separator + tempList[i]);
                flag = true;
            }
        }
        return flag;
    }


    /**
     * 把String字符串写入文件
     */
    public static void writeFile(String file_path, String text) {
        if (TextUtils.isEmpty(file_path) || !new File(file_path).exists()) {
            return;
        }

        try {
            FileWriter fileWriter = new FileWriter(file_path, true);
            fileWriter.write(text);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取文件，返回String
     */
    public static String readFile(String file_path) {
        if (TextUtils.isEmpty(file_path) || !new File(file_path).exists()) {
            return null;
        }
        StringBuilder line = new StringBuilder();

        try {
            FileReader reader = new FileReader(file_path);
            int character;
            while ((character = reader.read()) != -1) {
                line.append((char) character);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line.toString();
    }

    public static List<String> readLines(String filePath) {
        List<String> lines = new ArrayList<>();

        try {
            FileReader reader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
            bufferedReader.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lines;
    }

    public static void copyDirectory(File fromDir, File toDir) {
        try {
            if (!fromDir.isDirectory()) {
                return;
            }

            if (!toDir.exists()) {
                toDir.mkdirs();
            }

            File[] files = fromDir.listFiles();
            for (File file : files) {
                String strFrom = fromDir + File.separator + file.getName();
                String strTo = toDir + File.separator + file.getName();
                if (file.isDirectory()) {
                    copyDirectory(new File(strFrom), new File(strTo));
                }
                if (file.isFile()) {
                    copyFile(new File(strFrom), new File(strTo));
                }
            }
        } catch (Exception e) {
            System.out.println("copy Directory error = " + e.toString());
        }

    }

    public static void copyFile(File source, File dest) {
        if (source == null || dest == null) {
            return;
        }

        try {
            if (dest.exists()) {
                dest.delete();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Files.copy(source.toPath(), dest.toPath());
            } else {
                FileInputStream fis = new FileInputStream(source);
                FileOutputStream fos = new FileOutputStream(dest);
                FileChannel srcChannel = fis.getChannel();
                FileChannel dstChannel = fos.getChannel();
                dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

                srcChannel.close();
                dstChannel.close();
                fis.close();
                fos.close();
            }
        } catch (IOException e) {
            System.out.println("copy file error = " + e);
        }
    }
}
