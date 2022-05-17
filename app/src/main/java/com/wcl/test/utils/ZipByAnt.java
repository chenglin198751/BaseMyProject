package com.wcl.test.utils;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;

import java.io.File;

public class ZipByAnt {

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

    public static void zip(String inputFile, String outputZipFile) {
        File srcDir = new File(inputFile);
        if (!srcDir.exists()) {
            throw new RuntimeException(srcDir.getAbsolutePath() + "not exists");
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
}
