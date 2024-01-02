package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class CmdTask {
    private final static String TYPE_INPUT = "input";
    private final static String TYPE_ERROR = "error";
    private final String[] mCommand;
    private final String mWorkDir;


    public CmdTask(String[] command) {
        this(command, null);
    }

    public CmdTask(String[] command, String workDir) {
        this.mCommand = command;
        this.mWorkDir = workDir;
    }

    public Outs run(boolean is_log) {
        Outs outs = new Outs();
        String error = null;

        try {
            File work_dirs = null;
            if (mWorkDir != null && mWorkDir.length() > 0) {
                work_dirs = new File(mWorkDir);
            }
            Process process = Runtime.getRuntime().exec(mCommand, null, work_dirs);

            // Runtime.exec()创建的子进程公用父进程的流，不同平台上，父进程的stream buffer可能被打满导致子进程阻塞，从而永远无法返回。
            // 针对这种情况，我们只需要将子进程的stream重定向出来即可。
            Thread input_thread = new RedirCmdStreamThread(is_log, outs, process, process.getInputStream(), TYPE_INPUT);
            Thread error_thread = new RedirCmdStreamThread(is_log, outs, process, process.getErrorStream(), TYPE_ERROR);
            input_thread.start();
            error_thread.start();
            // 设置执行命令的超时时间为3分钟。特别注意，这里设置超时不适用于所有项目。
            // 因为有些命令执行确实非常耗时，比如java -jar apktool，要2-5分钟
            if (!process.waitFor(3, TimeUnit.MINUTES)) {
                error = "执行命令超时了";
                process.destroy();
            }
            outs.exit_value = process.exitValue();
            input_thread.join();
            error_thread.join();
        } catch (Exception e) {
            e.printStackTrace();
            error = e.toString();
        }

        if (error != null || outs.exit_value != 0) {
            error = "cmd is " + mCommand + ";exec failed:" + error + " exitValue is " + outs.exit_value;
            PackTools.Printer.print(error);
            return outs;
        }

        return outs;
    }

    public static final class Outs {
        public int exit_value = -1;
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
                    if (isLog) {
                        PackTools.Printer.print(line);
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
