package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class CmdTask {
    private final static String TYPE_INPUT = "input";
    private final static String TYPE_ERROR = "error";
    private final String mCommand;
    private final String mWorkDir;


    public CmdTask(String command) {
        this(command, null);
    }

    public CmdTask(String command, String workDir) {
        this.mCommand = FileUtils.replacePath(command);
        this.mWorkDir = FileUtils.replacePath(workDir);
    }

    public Outs run(boolean is_log) {
        Outs outs = new Outs();
        Process process = null;
        int exitVal = 0;
        String error = null;

        try {
            File work_dirs = null;
            if (mWorkDir != null && mWorkDir.length() > 0) {
                work_dirs = new File(mWorkDir);
            }
            process = Runtime.getRuntime().exec(mCommand, null, work_dirs);
            // Runtime.exec()创建的子进程公用父进程的流，不同平台上，父进程的stream buffer可能被打满导致子进程阻塞，从而永远无法返回。
            // 针对这种情况，我们只需要将子进程的stream重定向出来即可。
            new RedirCmdStreamThread(is_log, outs, process, process.getInputStream(), TYPE_INPUT).start();
            new RedirCmdStreamThread(is_log, outs, process, process.getErrorStream(), TYPE_ERROR).start();

            exitVal = process.waitFor();
            outs.exit_value = exitVal;
            process.destroy();
            process.exitValue();
        } catch (Exception e) {
            e.printStackTrace();
            error = e.toString();
            if (process != null) {
                process.destroy();
                process.exitValue();
            }
        }

        if (error == null || exitVal != 0) {
            error = "cmd = " + mCommand + "; exec failed:" + error + " exitVal= " + exitVal;
            PackTools.Printer.print(error);
            return outs;
        }

        return outs;
    }

    public static final class Outs {
        public int exit_value = -100;
        private final List<String> inputList = new ArrayList<>();
        private final List<String> errorList = new ArrayList<>();

        public List<String> getInputList() {
            return inputList;
        }

        public List<String> getErrorList() {
            return errorList;
        }

        public void addInputList(String line) {
            inputList.add(line);
        }

        public void addErrorList(String line) {
            errorList.add(line);
        }
    }

    private static class RedirCmdStreamThread extends Thread {
        InputStream is;
        Process process;
        String type;
        boolean isLog;
        Outs mOuts;

        RedirCmdStreamThread(boolean is_log, Outs outs, Process process, InputStream is, String type) {
            this.is = is;
            this.process = process;
            this.type = type;
            this.isLog = is_log;
            this.mOuts = outs;
        }

        public void run() {
            InputStreamReader isr = null;
            BufferedReader br = null;
            try {
                isr = new InputStreamReader(is);
                br = new BufferedReader(isr);

                String line = "";
                while ((line = br.readLine()) != null) {
                    if (type.equals(TYPE_INPUT)) {
                        mOuts.addInputList(line);
                    } else if (type.equals(TYPE_ERROR)) {
                        mOuts.addErrorList(line);
                    }
                }
            } catch (Exception ioe) {
                mOuts.inputList.add(ioe.toString());
                mOuts.errorList.add(ioe.toString());
                ioe.printStackTrace();
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                    if (isr != null) {
                        isr.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
