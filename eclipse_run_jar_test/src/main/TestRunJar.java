package main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TestRunJar {
	private static ArrayList<String> classList = new ArrayList<>();

	public static void main(String[] args) {
		String new_360SDK = "C:\\Users\\weichenglin1\\Desktop\\aaa\\new_360SDK";
		String old_360SDK = "C:\\Users\\weichenglin1\\Desktop\\aaa\\old_360SDK";

		ArrayList<String> list_new_360SDK = new ArrayList<>();
		ArrayList<String> list_old_360SDK = new ArrayList<>();

		classList.clear();
		filesDirs(new File(new_360SDK));
		list_new_360SDK.addAll(classList);

		classList.clear();
		filesDirs(new File(old_360SDK));
		list_old_360SDK.addAll(classList);

		List<String> subList = subList(list_new_360SDK, list_old_360SDK);
		for (int i = 0; i < subList.size(); i++) {
			String path2 = subList.get(i);
			if (!isProguardRule1(path2) && !isProguardRule2(path2)) {
				System.out.println(subList.get(i));
			}
		}
	}

	public static void filesDirs(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File flies2 : files) {
				filesDirs(flies2);
			}
		} else {
			String path2 = file.getAbsolutePath().replace("new_360SDK", "360SDK").replace("old_360SDK", "360SDK");
			if (path2.endsWith(".class")) {
				classList.add(path2);
			}
		}
	}

	public static List<String> subList(List<String> list1, List<String> list2) {
		list1.removeAll(list2);
		return list1;
	}

	public static boolean isProguardRule1(String path) {
		if (!path.endsWith(".class")) {
			return true;
		}

		String className = new File(path).getName();

		String[] maping = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
		for (int i = 0; i < maping.length; i++) {
			String className2 = maping[i] + ".class";
			if (className.equals(className2)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isProguardRule2(String path) {
		File file = new File(path);
		String className = file.getName().replace(".class", "");
		boolean isProguard = true;
		if (className.contains("$")) {
			String[] arrays2 = className.replace("$", "#").split("#");
			for (int i = 0; i < arrays2.length; i++) {
				if (arrays2[i].length() > 1) {
					isProguard = false;
				}
			}
		} else {
			isProguard = false;
		}
		return isProguard;
	}

}
