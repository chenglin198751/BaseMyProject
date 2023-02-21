package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;


public class CmdTask {
	private final static String TYPE_INPUT = "input";
	private final static String TYPE_error = "error";
	private boolean isLog = false;

	public static void exe(String command, String workDir, OnFinishedListener<Boolean, String> listener) {
		CmdTask cmdTask = new CmdTask(command, workDir, listener);
		cmdTask.run(false);
	}

	private String mCommand;
	private String mWorkDir;
	private OnFinishedListener<Boolean, String> mListener;

	public CmdTask(String command, String workDir) {
		this(command, workDir, null);
	}

	public CmdTask(String command, String workDir, OnFinishedListener<Boolean, String> listener) {
		this.mCommand = FileUtils.replacePath(command);
		this.mWorkDir = FileUtils.replacePath(workDir);
		this.mListener = listener;
	}

	public void run(boolean is_log) {
		isLog = is_log;
		Process process = null;
		int exitVal = 0;
		String error = "";

		try {
			File work_dirs = null;
			if (mWorkDir != null && mWorkDir.length() > 0) {
				work_dirs = new File(mWorkDir);
			}
			process = Runtime.getRuntime().exec(mCommand, null, work_dirs);
			// Runtime.exec()创建的子进程公用父进程的流，不同平台上，父进程的stream buffer可能被打满导致子进程阻塞，从而永远无法返回。
			// 针对这种情况，我们只需要将子进程的stream重定向出来即可。
			new RedirCmdStreamThread(process, process.getInputStream(), TYPE_INPUT).start();
			new RedirCmdStreamThread(process, process.getErrorStream(), TYPE_error).start();

			exitVal = process.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			error = e.toString();
		}

		if (error.length() > 0 || exitVal != 0) {
			error = "cmd = " + mCommand + "; exec failed:" + error;
			if (mListener != null) {
				mListener.onFinished(false, error);
			}
			PackTools.Error_Msg = error;
			throw new RuntimeException(error);
		}
	}

	class RedirCmdStreamThread extends Thread {
		InputStream is;
		Process process;
		String type;

		RedirCmdStreamThread(Process process, InputStream is, String type) {
			this.is = is;
			this.process = process;
			this.type = type;
		}

		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is, Charset.forName("GBK"));
				BufferedReader br = new BufferedReader(isr);

				String line = "";
				String line2 = "";
				while ((line = br.readLine()) != null) {
					if (line != null || line.length() > 0) {
						if (isLog) {
							PackTools.Printer.print(line);	
						}
						line2 = line2 + line + "\n";
					}
				}
				if (mListener != null) {
					if (type.equals(TYPE_INPUT)) {
						mListener.onFinished(true, line2);
					}
				}
			} catch (IOException ioe) {
				if (mListener != null) {
					if (type.equals(TYPE_INPUT)) {
						mListener.onFinished(false, ioe.toString());
					}
				}
				ioe.printStackTrace();
			}
		}
	}
}
