package main;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Main {


    public static void main(String[] args) {
        initTools();

        String[] print_certs = {"java", "-jar", EnvUtils.getApksigner(), "verify", "--print-certs", args[0]};
        CmdTask2 cmdTask2 = new CmdTask2(print_certs);
        CmdTask2.Outs outs = cmdTask2.run(false);

        List<String> list =  outs.getInputList();
        PackTools.Printer.print(list.toString());
    }

    private static void initTools() {
        File work_dir = new File(EnvUtils.getWorkPath());
        work_dir.mkdirs();

        String java_name = new File(EnvUtils.JAR_PATH).getName();
        String target_jar = EnvUtils.getWorkPath() + "/" + java_name;

        FileUtils.copyFile(EnvUtils.JAR_PATH, target_jar);
        ZipUtils.unZip(target_jar, EnvUtils.getWorkPath());
    }
}
