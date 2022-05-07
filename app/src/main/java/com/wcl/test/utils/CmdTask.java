package com.wcl.test.utils;

import com.wcl.test.listener.OnFinishedListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class CmdTask {

	public static void exe(String command, OnFinishedListener<Boolean, String> listener) {
		CmdTask cmdTask = new CmdTask(command, listener);
		cmdTask.run();
	}

	private String mCommand;
	private OnFinishedListener<Boolean, String> mListener;

	public CmdTask(String command) {
		this(command, null);
	}

	public CmdTask(String command, OnFinishedListener<Boolean, String> listener) {
		this.mCommand = command;
		this.mListener = listener;
	}

	public void run() {
		Process process = null;
		int exitVal = 0;
		String error = "";

		try {
			process = Runtime.getRuntime().exec(mCommand);
			// Runtime.exec()创建的子进程公用父进程的流，不同平台上，父进程的stream buffer可能被打满导致子进程阻塞，从而永远无法返回。
			// 针对这种情况，我们只需要将子进程的stream重定向出来即可。
			new RedirCmdStreamThread(process, process.getInputStream()).start();
			new RedirCmdStreamThread(process, process.getErrorStream()).start();

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
			throw new RuntimeException(error);
		}
	}

	class RedirCmdStreamThread extends Thread {
		InputStream is;
		Process process;

		RedirCmdStreamThread(Process process, InputStream is) {
			this.is = is;
			this.process = process;
		}

		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is, Charset.forName("GBK"));
				BufferedReader br = new BufferedReader(isr);

				String line = "";
				String line2 = "";
				while ((line = br.readLine()) != null) {
					if (line != null || line.length() > 0) {
						line2 = line2 + line + "\n";
					}
				}
				if (mListener != null) {
					mListener.onFinished(true, line2);
				}
			} catch (IOException ioe) {
				if (mListener != null) {
					mListener.onFinished(false, ioe.toString());
				}
				ioe.printStackTrace();
			}
		}
	}
}
