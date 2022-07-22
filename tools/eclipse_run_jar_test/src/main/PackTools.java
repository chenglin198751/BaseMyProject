package main;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class PackTools {
	private final static String replace_str = "dependencies {\r\n" //
			+ "    api fileTree(dir: 'libs', include: ['*.jar'])\r\n" //
			+ "    implementation(name: 'sdk_login', ext: 'aar')\r\n" //
			+ "    implementation(name: 'sdk_pay', ext: 'aar')\r\n" //
			+ "    implementation(name: 'sdk_social', ext: 'aar')\r\n" //
			+ "    implementation(name: 'sdk_in', ext: 'aar')\r\n" //
			+ "    implementation(name: 'sdk_common', ext: 'aar')\r\n" //
			+ "    implementation(name: 'sdk_support', ext: 'aar')\r\n"//
			+ "}";

	public static void modifyModeBuildGradle(final String build_gradle_path2) {
		final String build_gradle_path = FileUtils.replacePath(build_gradle_path2);
		File file = new File(build_gradle_path);
		String text = FileUtils.readFile(file);

		int index1 = text.indexOf("dependencies");
		int index2 = index1 + 1;
		int count = 0;
		while (true && count < 2000) {
			index2++;
			count++;
			if (text.charAt(index2) == '}') {
				break;
			}
		}
		String sub_str = text.substring(index1, index2 + 1);
		text = text.replace(sub_str, replace_str);

		FileUtils.writeFile(file, text);
	}

//	// 写入SDK的版本号，比如网游是2.3.4，单机是1002.3.4
//	public static void writeSdkVersionNameCode(boolean isDanji, final String file_path2) {
//		final String file_path = FileUtils.replacePath(file_path2);
//		final String version_name = getSdkVersionNameCode(file_path);
//		final File file = new File(file_path);
//		final String java_text = FileUtils.readFile(file);
//
//		if (isDanji) {
//			if (!version_name.startsWith("100")) {
//				FileUtils.writeFile(file, java_text.replace(version_name, "100" + version_name));
//			}
//		} else {
//			if (version_name.startsWith("100")) {
//				String target_v = version_name.replace("100", "");
//				FileUtils.writeFile(file, java_text.replace(version_name, target_v));
//			}
//		}
//	}
	
	// 获取SDK的版本号，以便打包时给zip命名
	public static String getSdkVersionNameCode(final String QBUILD_MASTER_PATH) {
		String version_path = QBUILD_MASTER_PATH + "/sdk_common/src/main/java/com/qihoo/gamecenter/sdk/common/SDKVersion.java";
		version_path = version_path.replace("/", File.separator);
		final String file_path = FileUtils.replacePath(version_path);
		
		File file = new File(file_path);
		String text = FileUtils.readFile(file);
		text = text.replace("\n", "").replace("\r", "").replace(" ", "");
		String version_name = null;
		String version_code = null;

		String arrs[] = text.split(";");
		for (int i = 0; i < arrs.length; i++) {
			if (arrs[i].contains("ONLINE_V") || arrs[i].contains("SDK_VERSION_CODE")) {
				String arrs2[] = arrs[i].split("=");
				for (int j = 0; j < arrs2.length; j++) {
					if (arrs2[j].contains("ONLINE_V")) {
						if (arrs2[0].contains("ONLINE_V")) {
							version_name = arrs2[1].replace("\"", "");
						}
					}
					if (arrs2[j].contains("SDK_VERSION_CODE")) {
						if (arrs2[0].contains("SDK_VERSION_CODE")) {
							version_code = arrs2[1];
						}
					}
				}
			}
		}

		return version_name + "_" + version_code;
	}

	public static float formatFloat(float f, int scale) {
		BigDecimal b = new BigDecimal(f);
		return b.setScale(scale, RoundingMode.HALF_UP).floatValue();
	}

	public static String getZipName(String SDK_OUT_PATH, String QBUILD_MASTER_PATH, boolean isDanji, boolean is_dudai) {
		SDK_OUT_PATH = FileUtils.replacePath(SDK_OUT_PATH);
		QBUILD_MASTER_PATH = FileUtils.replacePath(QBUILD_MASTER_PATH);

		File outputFile = new File(SDK_OUT_PATH);
		File parentF = outputFile.getParentFile();
		String version_name_code = PackTools.getSdkVersionNameCode(QBUILD_MASTER_PATH);
		if (isDanji) {
			version_name_code = "100" + version_name_code;
		}
		String zip_name = parentF.getAbsolutePath() + File.separator + outputFile.getName() + "(SDK_v" + version_name_code + ")_(xxx)";
		if (isDanji) {
			zip_name = zip_name.replace("Online", "Offline");
		}
		if (is_dudai) {
			zip_name = zip_name.replace("xxx", "dudai_on");
		} else {
			zip_name = zip_name.replace("xxx", "dudai_off");
		}
		return zip_name;
	}

	public static ArrayList<String> getDemoConfig(String content, String key) {
		ArrayList<String> list = new ArrayList<>();
		String kvs[] = content.split("\n");
		for (int i = 0; i < kvs.length; i++) {
			String kvs2[] = kvs[i].split(":");
			if (key.equals(kvs2[0])) {
				Collections.addAll(list, kvs2[1].split(","));
				return list;
			}
		}
		return null;
	}
	
	public static final class Printer {
		private static final DateFormat format1 = new SimpleDateFormat("hh:mm:ss");

		public static void print(String message) {
			String dateStr = format1.format(new Date());
			System.out.println("[" + dateStr + "] " + message);
		}
	}
}
