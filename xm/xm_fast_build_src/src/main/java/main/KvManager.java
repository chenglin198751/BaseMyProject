package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class KvManager {
	private static final String FILE_PATH = "E:\\360portalgamesdk\\portal-sdk-tool\\log\\xxx.pro";
	private Properties properties;
	private File file;

	public static KvManager getIns() {
		return InstanceHolder.INSTANCE;
	}

	private static final class InstanceHolder {
		static final KvManager INSTANCE = new KvManager(FILE_PATH);
	}

	private KvManager() {
	}

	public KvManager(String fileName) {
		this(new File(fileName));
	}

	public KvManager(File file) {
		this();

		try {
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!file.getName().endsWith(".pro")) {
			throw new IllegalArgumentException("无效文件!");
		}
		this.file = file;
		properties = getProperties();

	}

	private Properties getProperties() {
		try {
			Properties pro = new Properties();
			FileInputStream in = new FileInputStream(file);
			pro.load(in);
			in.close();
			return pro;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String get(String key) {
		return properties.getProperty(key);// 根据key获取内容
	}

	public void set(String key, String value) {
		try {
			FileOutputStream out = new FileOutputStream(file);
			properties.setProperty(key, value);
			properties.store(out, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}