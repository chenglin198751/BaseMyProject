package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 新版命令行执行工具，使用这个
 */
public class CmdTask2 {
    private final String[] command;
    private final String workDir;

    public CmdTask2(String[] command) {
        this(command, null);
    }

    public CmdTask2(String[] command, String workDir) {
        this.command = command;
        this.workDir = workDir;
    }

    public Outs run(boolean isLog) {
        Outs outs = new Outs();
        String error = null;

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            if (workDir != null && !workDir.isEmpty()) {
                pb.directory(new File(workDir));
            }
            Process process = pb.start();
            try (InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    outs.addInputList(line);
                    if (isLog) {
                        PackTools.Printer.print(line);
                    }
                }
                outs.setExitValue(process.waitFor());
            }
        } catch (Exception e) {
            error = "IOException: " + e.getMessage();
            PackTools.Printer.print("IOException occurred" + e);
        }

        if (error != null || outs.getExitValue() != 0) {
            error = "cmd=" + Arrays.toString(command) + ";exec failed:" + error + ";exitValue=" + outs.getExitValue();
            PackTools.Printer.print(error);
            PackTools.Error_Msg = error;
            outs.addInputList(error);
            return outs;
        }
        return outs;
    }

    public static final class Outs {
        private int exitValue = -1;
        private final List<String> inputList = new ArrayList<>();

        public int getExitValue() {
            return exitValue;
        }

        public void setExitValue(int exitValue) {
            this.exitValue = exitValue;
        }

        public List<String> getInputList() {
            return inputList;
        }

        public void addInputList(String line) {
            inputList.add(line);
        }
    }
}