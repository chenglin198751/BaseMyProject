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

    /**
     * 执行命令并返回结果。
     *
     * @param printOutput 是否实时打印命令输出
     */
    public Result execute(boolean printOutput) {
        Result result = new Result();
        Process process = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            // 把 stderr 合并进 stdout，单流顺序读取，避免死锁，也无需另起线程
            pb.redirectErrorStream(true);
            if (workDir != null && !workDir.isEmpty()) {
                pb.directory(new File(workDir));
            }
            process = pb.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.output.add(line);
                    if (printOutput) {
                        PackTools.Printer.print(line);
                    }
                }
                result.exitValue = process.waitFor();
            }
        } catch (Exception e) {
            result.error = e.toString();
            PackTools.Printer.print("exec exception: " + e);
            if (process != null) {
                // 中断或异常时销毁子进程，避免残留孤儿进程
                process.destroy();
            }
        }

        if (!result.isSuccess()) {
            String msg = "cmd=" + Arrays.toString(command) + ";exec failed:" + result.failReason();
            PackTools.Printer.print(msg);
            PackTools.Error_Msg = msg;
        }
        return result;
    }

    /**
     * 命令执行结果
     */
    public static final class Result {
        private int exitValue = -1;
        private final List<String> output = new ArrayList<>();
        private String error;

        /** 是否执行成功：无异常且退出码为 0 */
        public boolean isSuccess() {
            return error == null && exitValue == 0;
        }

        public int getExitValue() {
            return exitValue;
        }

        /** 命令的合并输出（stdout + stderr），失败时也包含命令自身打印的错误 */
        public List<String> getOutput() {
            return output;
        }

        /** 执行过程抛出的异常信息；命令正常结束（即使退出码非 0）时为空 */
        public String getError() {
            return error;
        }

        private String failReason() {
            return error != null ? error : "exitValue=" + exitValue;
        }
    }
}
