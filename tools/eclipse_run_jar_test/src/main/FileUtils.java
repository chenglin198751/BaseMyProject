package main;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

		if (toDir2.contains(fromDir2 + File.separator)) {
			PackTools.Error_Msg = "copy dir has entered an endless loop:fromDir2=" + fromDir2 + ", toDir2=" + toDir2;
			throw new RuntimeException("package sdk failed:" + PackTools.Error_Msg);
		}

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
					copyFile(strFrom, strTo);
				}
			}
		} catch (Exception e) {
			PackTools.Printer.print("copy Directory error:" + e.toString());
			PackTools.Error_Msg = e.toString();
			throw new RuntimeException("copy Directory error:" + e.toString());
		}

	}

	public static void copyFile(String source2, String dest2) {
		source2 = replacePath(source2);
		dest2 = replacePath(dest2);
		try {
			File source = new File(source2);
			File dest = new File(dest2);

			if (dest.exists()) {
				dest.delete();
			}
			Files.copy(source.toPath(), dest.toPath());
		} catch (Exception e) {
			PackTools.Printer.print("copy file error:" + e.toString());
			PackTools.Error_Msg = e.toString();
			throw new RuntimeException("copy file error:" + e.toString());
		}
	}

	public static void copyFileToDir(File source, String destDir) {
		destDir = replacePath(destDir);
		try {
			File dest = new File(destDir + File.separator + source.getName());
			if (dest.exists()) {
				dest.delete();
			}
			Files.copy(source.toPath(), dest.toPath());
		} catch (Exception e) {
			PackTools.Printer.print("copy file error:" + e.toString());
			PackTools.Error_Msg = e.toString();
			throw new RuntimeException("copy file error:" + e.toString());
		}
	}

	/**
	 * 读取文件
	 */
	public static String readFile(File file) {
		if (!file.exists()) {
			return null;
		}

		try {
			Path path = Paths.get(file.getAbsolutePath());
			Files.readAllBytes(path);
			String readString = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
			return readString;
		} catch (Throwable e) {
			PackTools.Error_Msg = e.toString();
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 把字符串写入文件
	 */
	public static void writeFile(File file, String value) {
		if (!file.exists()) {
			return;
		}

		// 写入文件
		try {
			Path path = Paths.get(file.getAbsolutePath());
			Files.write(path, value.getBytes(StandardCharsets.UTF_8));
		} catch (Throwable e) {
			PackTools.Error_Msg = e.toString();
			e.printStackTrace();
		}
	}

	public static String replacePath(String path) {
		if (path == null || path.length() <= 0) {
			return path;
		} else {
			return path.replace("/", File.separator);
		}
	}

	public static void rename(String from, String to) {
		File fromFile = new File(from);
		File toFile = new File(to);
		if (!fromFile.exists()) {
			PackTools.Error_Msg = "file not exists:" + from;
			throw new NullPointerException("file not exists:" + from);
		}
		fromFile.renameTo(toFile);
	}
}
