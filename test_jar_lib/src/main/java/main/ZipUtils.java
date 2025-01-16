package main;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class ZipUtils {

    // 基于ant_v1.10.12.jar实现的压缩zip
    public static void unZip(String inputZip, String destDir) {
        Project prj1 = new Project();
        Expand expand = new Expand();
        expand.setProject(prj1);
        expand.setSrc(new File(inputZip));
        expand.setOverwrite(true);
        File file = new File(destDir);
        if (!file.exists()) {
            file.mkdir();
        }
        expand.setDest(file);
        expand.execute();
    }

    // 基于ant_v1.10.12.jar实现的解压zip
    public static void zip(String inputFile, String outputZipFile) {
        File srcDir = new File(inputFile);
        if (!srcDir.exists()) {
            PackTools.Error_Msg = "zip error:" + srcDir.getAbsolutePath() + " not exists";
            throw new RuntimeException(srcDir.getAbsolutePath() + " not exists");
        }
        Project prj = new Project();
        Zip zip = new Zip();
        zip.setProject(prj);
        zip.setDestFile(new File(outputZipFile));
        FileSet fileSet = new FileSet();
        fileSet.setProject(prj);
        fileSet.setDir(srcDir);
        zip.addFileset(fileSet);
        zip.execute();
    }

    // 基于commons-compress-1.27.1.jar实现的压缩zip
    public static void zip2(String sourceDir, String zipFile) {
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(fos)) {
            File dirToZip = new File(sourceDir);
            if (!dirToZip.exists() || !dirToZip.isDirectory()) {
                throw new IllegalArgumentException("Source must be a directory");
            }
            File[] files = dirToZip.listFiles();
            if (files != null) {
                for (File file : files) {
                    addToZip("", file, zipOut);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 基于commons-compress-1.27.1.jar实现的解压zip
    public static void unzip2(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        try {
            try (FileInputStream fis = new FileInputStream(zipFilePath);
                 ZipArchiveInputStream zis = new ZipArchiveInputStream(fis)) {
                ZipArchiveEntry entry;
                while ((entry = zis.getNextZipEntry()) != null) {
                    if (!entry.isDirectory()) {
                        File outputFile = new File(destDir, entry.getName());
                        File parent = outputFile.getParentFile();
                        if (!parent.exists()) {
                            parent.mkdirs();
                        }
                        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                            IOUtils.copy(zis, fos);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addToZip(String path, File file, ZipArchiveOutputStream zipOut) throws IOException {
        String entryPath = path + file.getName();
        if (file.isDirectory()) {
            // 如果是目录，递归处理
            File[] children = file.listFiles();
            if (children != null) {
                for (File childFile : children) {
                    addToZip(entryPath + "/", childFile, zipOut);
                }
            }
        } else {
            ZipArchiveEntry zipEntry = new ZipArchiveEntry(file, entryPath);
            zipOut.putArchiveEntry(zipEntry);
            try (FileInputStream fis = new FileInputStream(file)) {
                IOUtils.copy(fis, zipOut);
            }
            zipOut.closeArchiveEntry();
        }
    }
}
