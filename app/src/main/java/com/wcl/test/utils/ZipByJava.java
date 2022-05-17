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

    public static void unZip(String inputZip,String destDir) {
        try {
            File srcFile = new File(inputZip);
            if (!srcFile.exists()) {
                System.out.println(srcFile.getAbsolutePath() + " not exist");
            }

            File destDirF = new File(destDir);
            if (destDirF.exists()) {
                deleteDirectory(destDirF);
            }

            ZipFile zipFile = new ZipFile(srcFile);
            Enumeration<?> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                if (entry.isDirectory()) {
                    srcFile.mkdirs();
                } else {
                    File targetFile = new File(destDir + File.separator + entry.getName());

                    if (!targetFile.getParentFile().exists()) {
                        targetFile.getParentFile().mkdirs();
                    }
                    targetFile.createNewFile();

                    InputStream is = zipFile.getInputStream(entry);
                    FileOutputStream fos = new FileOutputStream(targetFile);
                    int len;
                    byte[] buf = new byte[1024];
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }

                    fos.close();
                    is.close();
                }
            }
            zipFile.close();
        } catch (Exception e) {
            System.out.println("zipUncompress error = " + e.toString());
        }
    }

    public static void fileToZip(String srcFile, String zipFile) {
        try {
            File file = new File(srcFile);
            String name = file.getName();
            FileInputStream inputStream = new FileInputStream(file);
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
            zipOutputStream.putNextEntry(new ZipEntry("Image\\01.jpg"));

            int temp = 0;
            while ((temp = inputStream.read()) != -1) {
                zipOutputStream.write(temp);
            }
            zipOutputStream.close();
            inputStream.close();
        } catch (Exception e) {
            System.out.println("zip error1 = " + e.toString());
        }

    }

    public static Boolean zip(String inputFileName, String zipFileName) {
        try {
            File inputFile = new File(inputFileName);
            if (inputFile.exists()) {
                inputFile.delete();
            } else {
                inputFile.createNewFile();
            }

            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
            zip(out, inputFile, "");
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("zip error1 = " + e.toString());
        }

        return true;
    }

    private static void zip(ZipOutputStream out, File f, String base) {
        try {
            if (f.isDirectory()) {
                File[] fl = f.listFiles();
                out.putNextEntry(new ZipEntry(base + File.separator));
                base = base.length() == 0 ? "" : base + File.separator;
                for (int i = 0; i < fl.length; i++) {
                    zip(out, fl[i], base + fl[i].getName());
                }
            } else {
                out.putNextEntry(new ZipEntry(base));
                FileInputStream in = new FileInputStream(f);
                int b;
                while ((b = in.read()) != -1) {
                    out.write(b);
                }
                in.close();
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
