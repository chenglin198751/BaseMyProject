package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

    public Outs run(boolean isLog) {
        Outs outs = new Outs();
        try {
            File workDirs = null;
            if (mWorkDir != null && !mWorkDir.isEmpty()) {
                workDirs = new File(mWorkDir);
            }
            Process process = Runtime.getRuntime().exec(mCommand, null, workDirs);
            CompletableFuture<Void> inputFuture = CompletableFuture.runAsync(() -> handleStream(process.getInputStream(), outs, TYPE_INPUT, isLog));
            CompletableFuture<Void> errorFuture = CompletableFuture.runAsync(() -> handleStream(process.getErrorStream(), outs, TYPE_ERROR, isLog));
            int exitValue = process.waitFor();
            try {
                CompletableFuture.allOf(inputFuture, errorFuture).get();
            } catch (Exception e) {
                PackTools.Printer.print("Exception11:" + e);
            }
            outs.exit_value = exitValue;
        } catch (Exception e) {
            outs.errorList.add(e.toString());
            PackTools.Printer.print("Exception22:" + e);
        }

        if (outs.exit_value != 0) {
            String error = "cmd=" + Arrays.toString(mCommand) + ";exec failed:" + outs.errorList + ";exitValue=" + outs.exit_value;
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

    private static void handleStream(InputStream is, Outs outs, String type, boolean isLog) {
        try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (type.equals(TYPE_INPUT)) {
                    outs.addInputList(line);
                } else if (type.equals(TYPE_ERROR)) {
                    outs.addErrorList(line);
                }
                if (isLog) {
                    PackTools.Printer.print(line);
                }
            }
        } catch (Exception e) {
            outs.errorList.add(e.toString());
            PackTools.Printer.print(e.toString());
        }
    }
}