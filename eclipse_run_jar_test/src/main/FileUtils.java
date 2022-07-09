package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class FileUtils {

	public static void delete(String file2) {
		file2 = replacePath(file2);
		File file = new File(file2);
		if (file.exists()) {
			if (file.isFile()) {
				file.delete();
			} else if (file.isDirectory()) {
				File files[] = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					delete(files[i].getAbsolutePath());
				}
			}
			file.delete();
		}
	}

	public static void copyDirectory(String fromDir2, String toDir2) {
		fromDir2 = replacePath(fromDir2);
		toDir2 = replacePath(toDir2);
		try {
			File fromDir = new File(fromDir2);
			File toDir = new File(toDir2);

			if (!fromDir.isDirectory()) {
				return;
			}

			if (!toDir.exists()) {
				toDir.mkdirs();
			}

			File[] files = fromDir.listFiles();
			for (File file : files) {
				String strFrom = fromDir + File.separator + file.getName();
				String strTo = toDir + File.separator + file.getName();
				if (file.isDirectory()) {
					copyDirectory(strFrom, strTo);
				}
				if (file.isFile()) {
					copy(strFrom, strTo);
				}
			}
		} catch (Exception e) {
			PackTools.Printer.print("copy Directory error:" + e.toString());
			throw new RuntimeException("copy Directory error:" + e.toString());
		}

	}

	public static void copy(String source2, String dest2) {
		source2 = replacePath(source2);
		dest2 = replacePath(dest2);
		try {
			File source = new File(source2);
			File dest = new File(dest2);

			if (dest.exists()) {
//				PackTools.Printer.print("duplicate file name is " + source.getAbsolutePath());
				dest.delete();
			}
			Files.copy(source.toPath(), dest.toPath());
		} catch (Exception e) {
			PackTools.Printer.print("copy file error:" + e.toString());
			throw new RuntimeException("copy file error:" + e.toString());
		}
	}

	/**
	 * 把字符串写入文件
	 */
	public static void writeFile(File file, String value) {
		if (!file.exists()) {
			return;
		}
		
		// 清空内容
		try {
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write("");
			fileWriter.flush();
			fileWriter.close();
		} catch (Exception e) {
			PackTools.Printer.print(e.toString());
		}

		// 写入文件
		try {
			FileWriter fileWriter = new FileWriter(file, true);
			fileWriter.write(value);
			fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 读取文件
	 */
	public static String readFile(File file) {
		if (!file.exists()) {
			return null;
		}

		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(file));
			String readString = "";
			String currentLine;
			while ((currentLine = in.readLine()) != null) {
				currentLine += '\n';
				readString += currentLine;
			}
			return readString;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String replacePath(String path) {
		if (path == null || path.length() <= 0) {
			return path;
		} else {
			return path.replace("/", File.separator);
		}
	}
}
