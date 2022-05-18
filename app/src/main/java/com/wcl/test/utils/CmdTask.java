package com.wcl.test.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CmdTask {
    private final static String TYPE_INPUT = "input";
    private final static String TYPE_error = "error";

    public static void exe(String command, String workDir) {
        CmdTask cmdTask = new CmdTask(command, workDir);
        cmdTask.run();
    }

    private String mCommand;
    private String mWorkDir;


    public CmdTask(String command, String workDir) {
        this.mCommand = replacePath(command);
        this.mWorkDir = replacePath(workDir);
    }

    public void run() {
        Process process = null;
        int exitVal = 0;
        String error = "";

        try {
            File work_dirs = null;
            if (mWorkDir != null && mWorkDir.length() > 0) {
                work_dirs = new File(mWorkDir);
            }
            process = Runtime.getRuntime().exec(mCommand, null, work_dirs);
            StreamPumper inputPumper = new StreamPumper(process.getInputStream(), TYPE_INPUT);
            StreamPumper errorPumper = new StreamPumper(process.getErrorStream(), TYPE_error);
            inputPumper.start();
            errorPumper.start();
            process.waitFor();
            inputPumper.join();
            errorPumper.join();
            process.destroy();
            exitVal = process.exitValue();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            error = e.toString();
        }

        if (error.length() > 0 || exitVal != 0) {
            error = "cmd = " + mCommand + "; exec failed:" + error;
            System.out.println("command_error:" + error);
            throw new RuntimeException(error);
        }
    }


    private static class StreamPumper extends Thread {
        private BufferedReader din;
        private String messageLevel;
        private boolean endOfStream = false;
        private static final int SLEEP_TIME = 5;

        public StreamPumper(InputStream is, String messageLevel) {
            this.din = new BufferedReader(new InputStreamReader(is));
            this.messageLevel = messageLevel;
        }

        public void pumpStream() throws IOException {
            if (!this.endOfStream) {
                String line = this.din.readLine();
                if (line != null) {
                    System.out.println("command_" + messageLevel + ":" + line);
                } else {
                    this.endOfStream = true;
                }
            }
        }

        @Override
        public void run() {
            while (!this.endOfStream) {
                try {
                    try {
                        pumpStream();
                        sleep(5L);
                    } catch (IOException e) {
                        return;
                    }
                } catch (InterruptedException e2) {
                }
            }
            try {
                this.din.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String replacePath(String path) {
        if (path == null || path.length() <= 0) {
            return path;
        } else {
            return path.replace("/", File.separator);
        }
    }
}
