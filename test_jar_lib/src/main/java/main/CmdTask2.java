package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 新版命令行执行工具，使用这个
 */
public class CmdTask2 {
    private final String[] mCommand;
    private final String mWorkDir;


    public CmdTask2(String[] commands) {
        this(commands, null);
    }

    public CmdTask2(String[] commands, String workDir) {
        this.mCommand = commands;
        this.mWorkDir = workDir;
    }

    public Outs run(boolean is_log) {
        Outs outs = new Outs();
        String error = null;

        try {
            ProcessBuilder pb = new ProcessBuilder(mCommand);
            pb.redirectErrorStream(true);
            if (mWorkDir != null && !mWorkDir.isEmpty()) {
                pb.directory(new File(mWorkDir));
            }
            Process process = pb.start();
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                outs.addInputList(line);
                if (is_log) {
                    PackTools.Printer.print(line);
                }
            }
            outs.exit_value = process.waitFor();
        } catch (Exception e) {
            error = e.toString();
        }

        if (error != null || outs.exit_value != 0) {
            error = "cmd=" + String.join(" ", mCommand) + ";exec failed:" + error + ";exitValue=" + outs.exit_value;
            PackTools.Error_Msg = error;
            return outs;
        }
        return outs;
    }

    public static final class Outs {
        public int exit_value = -1;
        private final List<String> inputList = new ArrayList<>();

        public List<String> getInputList() {
            return inputList;
        }

        public void addInputList(String line) {
            inputList.add(line);
        }
    }
}
