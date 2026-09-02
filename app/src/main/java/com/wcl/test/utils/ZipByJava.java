package com.wcl.test.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipByJava {

    public static void unZip(String inputZip, String destDir) {
        File srcFile = new File(inputZip);
        if (!srcFile.exists()) {
            System.out.println(srcFile.getAbsolutePath() + " not exist");
            return;
        }

        File destDirF = new File(destDir);
        if (destDirF.exists()) {
            deleteDirectory(destDirF);
        }

        try (ZipFile zipFile = new ZipFile(srcFile)) {
            Enumeration<?> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                File targetFile = new File(destDir + File.separator + entry.getName());
                if (entry.isDirectory()) {
                    targetFile.mkdirs();
                } else {
                    File parent = targetFile.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }
                    try (InputStream is = zipFile.getInputStream(entry);
                         FileOutputStream fos = new FileOutputStream(targetFile)) {
                        byte[] buf = new byte[8192];
                        int len;
                        while ((len = is.read(buf)) != -1) {
                            fos.write(buf, 0, len);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("zipUncompress error = " + e.toString());
        }
    }

    public static void fileToZip(String srcFile, String zipFile) {
        File file = new File(srcFile);
        String name = file.getName();
        try (FileInputStream inputStream = new FileInputStream(file);
             ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile))) {
            zipOutputStream.putNextEntry(new ZipEntry(name));

            byte[] buf = new byte[8192];
            int len;
            while ((len = inputStream.read(buf)) != -1) {
                zipOutputStream.write(buf, 0, len);
            }
        } catch (Exception e) {
            System.out.println("zip error1 = " + e.toString());
        }
    }

    public static boolean zip(String inputFileName, String zipFileName) {
        File inputFile = new File(inputFileName);
        if (!inputFile.exists()) {
            System.out.println("zip error: " + inputFile.getAbsolutePath() + " not exist");
            return false;
        }

        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName))) {
            zip(out, inputFile, "");
            out.flush();
            return true;
        } catch (Exception e) {
            System.out.println("zip error1 = " + e.toString());
            return false;
        }
    }

    private static void zip(ZipOutputStream out, File f, String base) {
        try {
            if (f.isDirectory()) {
                File[] files = f.listFiles();
                if (files == null) {
                    return;
                }
                out.putNextEntry(new ZipEntry(base + File.separator));
                String childBase = base.length() == 0 ? "" : base + File.separator;
                for (File file : files) {
                    zip(out, file, childBase + file.getName());
                }
            } else {
                out.putNextEntry(new ZipEntry(base));
                try (FileInputStream in = new FileInputStream(f)) {
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = in.read(buf)) != -1) {
                        out.write(buf, 0, len);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("zip error2 = " + e.toString());
        }
    }

    private static void deleteDirectory(File file) {
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

}
