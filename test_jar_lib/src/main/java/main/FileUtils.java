package main;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


public class FileUtils {

    public static void delete(String file2) {
        file2 = replacePath(file2);
        File file = new File(file2);
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File files[] = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    delete(files[i].getAbsolutePath());
                }
            }
            file.delete();
        }
    }

    public static void copyDirectory(String fromDir2, String toDir2) {
        fromDir2 = replacePath(fromDir2);
        toDir2 = replacePath(toDir2);

        if (toDir2.contains(fromDir2 + File.separator)) {
            PackTools.Error_Msg = "copy dir has entered an endless loop:fromDir2=" + fromDir2 + ", toDir2=" + toDir2;
            throw new RuntimeException("package sdk failed:" + PackTools.Error_Msg);
        }

        try {
            File fromDir = new File(fromDir2);
            File toDir = new File(toDir2);

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
                    copyDirectory(strFrom, strTo);
                }
                if (file.isFile()) {
                    copyFile(strFrom, strTo);
                }
            }
        } catch (Exception e) {
            PackTools.Printer.print("copy Directory error:" + e.toString());
            PackTools.Error_Msg = e.toString();
            throw new RuntimeException("copy Directory error:" + e.toString());
        }

    }

    public static void copyFile(String source2, String dest2) {
        source2 = replacePath(source2);
        dest2 = replacePath(dest2);
        try {
            File source = new File(source2);
            File dest = new File(dest2);

            if (dest.exists()) {
//				PackTools.Printer.print("duplicate file name is " + source.getAbsolutePath());
                dest.delete();
            }
            Files.copy(source.toPath(), dest.toPath());
        } catch (Exception e) {
            PackTools.Printer.print("copy file error:" + e.toString());
            PackTools.Error_Msg = e.toString();
            throw new RuntimeException("copy file error:" + e.toString());
        }
    }

    public static void copyFileToDir(File source, String destDir) {
        destDir = replacePath(destDir);
        try {
            File dest = new File(destDir + File.separator + source.getName());
            if (dest.exists()) {
                dest.delete();
            }
            Files.copy(source.toPath(), dest.toPath());
        } catch (Exception e) {
            PackTools.Printer.print("copy file error:" + e.toString());
            PackTools.Error_Msg = e.toString();
            throw new RuntimeException("copy file error:" + e.toString());
        }
    }

    /**
     * 读取文件
     */
    public static String readFile(File file) {
        if (!file.exists()) {
            return null;
        }

        try {
            Path path = Paths.get(file.getAbsolutePath());
            Files.readAllBytes(path);
            String readString = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            return readString;
        } catch (Throwable e) {
            PackTools.Error_Msg = e.toString();
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 把字符串写入文件
     */
    public static void writeFile(File file, String value) {
        if (!file.exists()) {
            return;
        }

        // 写入文件
        try {
            Path path = Paths.get(file.getAbsolutePath());
            Files.write(path, value.getBytes(StandardCharsets.UTF_8));
        } catch (Throwable e) {
            PackTools.Error_Msg = e.toString();
            e.printStackTrace();
        }
    }

    public static String replacePath(String path) {
        if (path == null || path.length() <= 0) {
            return path;
        } else {
            return path.replace("/", File.separator);
        }
    }

    public static void rename(String from, String to) {
        File fromFile = new File(from);
        File toFile = new File(to);
        if (!fromFile.exists()) {
            PackTools.Error_Msg = "file not exists:" + from;
            throw new NullPointerException("file not exists:" + from);
        }
        fromFile.renameTo(toFile);
    }


    /**
     * 根据文件后缀名复制文件，支持数组格式的多个后缀名
     */
    public static void copyFilesBySuffix(String source_dir, String dest_dir, String[] files_suffix) {
        File[] source_files = new File(source_dir).listFiles();

        // 是否把source_dir整个文件夹的文件都复制过去
        boolean isCopyAllFiles = false;
        if (files_suffix.length == 1) {
            if (files_suffix[0] == null || files_suffix[0].length() <= 0) {
                isCopyAllFiles = true;
            }
        }

        if (source_files.length > 0) {
            for (File file : source_files) {
                String dest_file_path = dest_dir + File.separator + file.getName();

                if (isCopyAllFiles) {
                    copyFile(file.getAbsolutePath(), dest_file_path);
                } else {
                    for (String suffix : files_suffix) {
                        if (file.getName().endsWith(suffix)) {
                            copyFile(file.getAbsolutePath(), dest_file_path);
                        }
                    }
                }

            }
        }
    }

    /**
     * 根据文件后缀名复制文件，此方法只支持单个文件后缀名。
     * 如果suffix参数为空字符串""，那么就是复制所有的文件
     */
    public static void copyFilesBySuffix(String source_dir, String dest_dir, String suffix) {
        copyFilesBySuffix(source_dir, dest_dir, new String[] { suffix });
    }

    /**
     * 获取一个文件夹下所有的文件
     */
    public static void getAllFiles(File dir, List<File> fileList) {
        File[] files = dir.listFiles();
        if (files!= null && files.length > 0) {
            for (File file : files) {
                if (file.isDirectory()) {
                    getAllFiles(file, fileList);
                } else {
                    fileList.add(file);
                }
            }
        }
    }
}
