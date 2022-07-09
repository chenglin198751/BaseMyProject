package main;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainPack {
	private static String eclipse_dir;
	private static String android_studio_dir;
	private static String dian_aar = ".aar";
	private static int zip_count = 0;
	private static String SDK_OUT_PATH;
	private static String QBUILD_MASTER_PATH;
	private static String QBUILD_OUT;
	private static long start_time;
	private static boolean isLocalPkg = false;

	
//	public static void main(String[] args) {
//		
//		boolean isRule = rule2("C:\\Users\\weichenglin1\\Desktop\\aaa\\360SDK\\com\\unicom\\xiaowo\\account\\shield\\e\\c$b.class");
//		System.out.println("isRule = " + isRule);
//	}
	
	public static void main(String[] args) {
		String new_360SDK = "C:\\Users\\weichenglin1\\Desktop\\aaa\\new_360SDK";
		String old_360SDK = "C:\\Users\\weichenglin1\\Desktop\\aaa\\old_360SDK";

		
		ArrayList<String> list_new_360SDK = new ArrayList<>();
		ArrayList<String> list_old_360SDK = new ArrayList<>();
		
		list.clear();
		filesDirs(new File(new_360SDK));
		list_new_360SDK.addAll(list);
		
		list.clear();
		filesDirs(new File(old_360SDK));
		list_old_360SDK.addAll(list);
		
		List<String> subList = subList(list_new_360SDK,list_old_360SDK);
		for (int i = 0; i < subList.size(); i++) {
			String path2 = subList.get(i);
			if (!isProguard1(path2) && !isProguard2(path2)){
				System.out.println(subList.get(i));	
			}
		}
	}
	
	private static ArrayList<String> list = new ArrayList<>();
	
	public static void filesDirs(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File flies2 : files) {
				filesDirs(flies2);
			}
		} else {
			String path2 = file.getAbsolutePath().replace("new_360SDK", "360SDK").replace("old_360SDK", "360SDK");
			list.add(path2);
		}
	}

	public static List<String> subList(List<String> list1, List<String> list2) {
	    list1.removeAll(list2);
	    return list1;
	}
	
	public static boolean isProguard1(String path) {
		if (!path.endsWith(".class")) {
			return true;
		}
		
		String[] maping = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
		for (int i = 0; i < maping.length; i++) {
			String str = File.separator + maping[i] + ".class";	
			if (path.endsWith(str) ) {
				return true;
			}
		}
		return false;
	}

	public static boolean isProguard2(String path) {
		File file = new File(path);
		String lastStr =file.getName().replace(".class", "");
		boolean isLength1 = true;
		if (lastStr.contains("$")) {
			String[] arrays2 = lastStr.replace("$","#").split("#");
			for (int i = 0; i < arrays2.length; i++) {
				if (arrays2[i].length() > 1) {
					isLength1 = false;
				}
			}
		}else {
			isLength1 = false;
		}
		return isLength1;
	}
	
	public static void main2(String[] args) {
		PackTools.Printer.print("----args[0]=" + args[0]);
		PackTools.Printer.print("----args[1]=" + args[1]);
		// SDK输出路径：../360SDK_Android_OnlineGame/
		SDK_OUT_PATH = args[0];
		// master代码所在路径：$QBUILD_MASTER_PATH
		QBUILD_MASTER_PATH = args[1];

		// ---java打包开始---
		PackTools.Printer.print("----java package start----");
		start_time = System.currentTimeMillis();

		pkgBefore();
		exe_build_variants_sdk_common_aar_shell();
		exe_build_plugin_apk_shell();
		exe_modules_aar_shell();
		createDemoSrc();
		exe_build_demo_apks_shell();
		packageSdk();
		pkgAfter();

		// ---java打包结束---
		float minutes = (System.currentTimeMillis() - start_time) / 1000f / 60f;
		PackTools.Printer.print("----java package finished,it takes " + PackTools.formatFloat(minutes, 2) + " minutes----");
	}
	
	// 打包SDK开始前的准备工作
	private static void pkgBefore() {
		// 本地执行打包：
		String sh_dir = new File("").getAbsolutePath();
		String master_src_dir = new File(sh_dir).getParent();

		if (SDK_OUT_PATH.equals("360SDK_Android_OnlineGame")//
				&& QBUILD_MASTER_PATH.equals("360_android_mobile_game_sdk")) {
			isLocalPkg = true;
			if (SDK_OUT_PATH.equals("360SDK_Android_OnlineGame")) {
				SDK_OUT_PATH = new File(master_src_dir).getParent() + "/360SDK_Android_OnlineGame";
				PackTools.Printer.print("ocal_package_sdk.sh--args[0]=" + SDK_OUT_PATH);
			}
			if (QBUILD_MASTER_PATH.equals("360_android_mobile_game_sdk")) {
				QBUILD_MASTER_PATH = master_src_dir;
				PackTools.Printer.print("ocal_package_sdk.sh--args[1]=" + QBUILD_MASTER_PATH);
			}
		}

		QBUILD_OUT = (new File(SDK_OUT_PATH).getParent());
		eclipse_dir = SDK_OUT_PATH + "/Eclipse";
		android_studio_dir = SDK_OUT_PATH + "/AndroidStudio";

		if (SDK_OUT_PATH.contains("360SDK_Android_OnlineGame")) {
			FileUtils.delete(SDK_OUT_PATH);
		}
		new File(eclipse_dir).mkdirs();
		new File(android_studio_dir).mkdirs();
		new File(SDK_OUT_PATH + "/demo-apk").mkdirs();
		new File(QBUILD_OUT + "/360SDK_mapping").mkdirs();

		// 没什么特别的用处，因为后续打包会修改sdk_common下的QhSdkToggle.jar文件，所以这里备份，等打包后还原
		if (isLocalPkg) {
			String from = QBUILD_MASTER_PATH + "/sdk_common/libs/QhSdkToggle.jar";
			String to = QBUILD_OUT + "/bk_QhSdkToggle.jar";
			FileUtils.copy(from, to);
		}
	}

	// 打包SDK结束后的善后工作
	private static void pkgAfter() {
		if (isLocalPkg) {
			String from = QBUILD_OUT + "/bk_QhSdkToggle.jar";
			String to = QBUILD_MASTER_PATH + "/sdk_common/libs/QhSdkToggle.jar";
			FileUtils.copy(from, to);
			FileUtils.delete(from);
		}
	}

	private static void createDemoSrc() {
		PackTools.Printer.print("----java createDemoSrc start----");

		// 复制demo代码到SDK输出目录
		PackTools.Printer.print("copy demo src start");
		final String to_demo_path = (android_studio_dir + "/DemoProject");
		FileUtils.copyDirectory(QBUILD_MASTER_PATH, to_demo_path);
		deleteModuleDrawables(to_demo_path);

		// 复制/tools/exclude_libs/下的jar，到demo/libs目录
		final String exclude_libs = (QBUILD_MASTER_PATH + "/tools/exclude_libs");
		final String demo_libs = (to_demo_path + "/sdk_social_pay_demo_online/libs");
		FileUtils.copyDirectory(exclude_libs, demo_libs);

		// 复制编译出的各个module下的aar，到demo/libs目录(不复制sdk_common.aar)
		final String aar_dirs[] = { "sdk_in", "sdk_login", "sdk_pay", "sdk_social", "sdk_support" };
		for (int i = 0; i < aar_dirs.length; i++) {
			final String path = QBUILD_MASTER_PATH + "/" + aar_dirs[i] + "/build/outputs/aar/" + aar_dirs[i] + ".aar";
			FileUtils.copy(path, to_demo_path + "/sdk_social_pay_demo_online/libs/" + aar_dirs[i] + ".aar");
		}

		// 修改build.gradle以便CP可以直接打开demo工程
		PackTools.Printer.print("modify demo src build.gradle start");
		PackTools.modifyModeBuildGradle(to_demo_path + "/sdk_social_pay_demo_online/build.gradle");

		// 删除demo代码的相关文件夹
		PackTools.Printer.print("delete demo useless dir start");
		FileUtils.delete(to_demo_path + "/sdk_social_pay_demo_online/build");
		FileUtils.delete(to_demo_path + "/.gradle");
		FileUtils.delete(to_demo_path + "/.idea");
		final String temp_dirs[] = { "sdk_common", "sdk_in", "sdk_login", "sdk_pay", "sdk_plugin_apk", "sdk_plugin_apk_gmctr", "sdk_social", "sdk_support", "sdk_plugin_apk_gift" };
		for (int i = 0; i < temp_dirs.length; i++) {
			final String path = to_demo_path + File.separator + temp_dirs[i];
			PackTools.Printer.print("delete demo useless dir 2222:" + path);
			FileUtils.delete(path);
		}

		PackTools.Printer.print("delete demo useless dir 444:tools dir");
		FileUtils.delete(to_demo_path + "/tools");
		PackTools.Printer.print("----java createDemoSrc finished----");
	}

	// 删除sdk_common,sdk_login,sdk_pay,sdk_social,sdk_support下的res/下的drawable开头的文件夹
	// 因为使用的是dat，所以drawable文件在打包时都不需要了
	private static void deleteModuleDrawables(final String srcPath) {
		PackTools.Printer.print("----java deleteModuleDrawables start----");
		final String module_dirs[] = { "sdk_common", "sdk_login", "sdk_pay", "sdk_social", "sdk_support" };
		for (int i = 0; i < module_dirs.length; i++) {
			final String res_path = srcPath + "/" + module_dirs[i] + "/src/main/res";
			File resFiles[] = new File(res_path).listFiles();
			if (resFiles == null || resFiles.length <= 0) {
				return;
			}
			for (File drawable_dir : resFiles) {
				if (drawable_dir.getName().startsWith("drawable-")) {
					FileUtils.delete(drawable_dir.getAbsolutePath());
				}
			}
		}
		PackTools.Printer.print("----java deleteModuleDrawables finished----");
	}

	// 编译4个变种sdk_common_aar，分别是：'dudai_off-danji_off','dudai_off-danji_on','dudai_on-danji_off','dudai_on-danji_on'
	private static void exe_build_variants_sdk_common_aar_shell() {
		final String build_demo_apks_sh = QBUILD_MASTER_PATH + "/tools/sh/build_variants_sdk_common_aar.sh";
		final String bashCommand = "sh " + build_demo_apks_sh + " " + QBUILD_MASTER_PATH + " " + QBUILD_OUT;
		CmdTask cmdTask = new CmdTask(bashCommand, null);
		cmdTask.run(true);
	}

	// 编译sdk_in,sdk_pay等aar。因为需要先编译aar，再编译demo apk
	private static void exe_modules_aar_shell() {
		final String build_demo_apks_sh = QBUILD_MASTER_PATH + "/tools/sh/build_modules_aars.sh";
		final String bashCommand = "sh " + build_demo_apks_sh + " " + QBUILD_MASTER_PATH;
		CmdTask cmdTask = new CmdTask(bashCommand, null);
		cmdTask.run(true);
	}

	// 编译SDK主插件360sdk_1_2344.apk，编译完成后其gradle会自动将其复制到sdk_in/assets下
	private static void exe_build_plugin_apk_shell() {
		final String build_demo_apks_sh = QBUILD_MASTER_PATH + "/tools/sh/build_plugin_apk.sh";
		final String bashCommand = "sh " + build_demo_apks_sh + " " + QBUILD_MASTER_PATH + " " + QBUILD_OUT;
		CmdTask cmdTask = new CmdTask(bashCommand, null);
		cmdTask.run(true);
	}

	// 编译demo apk：是把上面编译出的sdk_in.aar,sdk_pay.aar等复制到demo工程下再执行编译
	private static void exe_build_demo_apks_shell() {
		final String build_demo_apks_sh = QBUILD_MASTER_PATH + "/tools/sh/build_demo_apks.sh";
		final String bashCommand = "sh " + build_demo_apks_sh + " " + QBUILD_MASTER_PATH + " " + QBUILD_OUT;
		CmdTask cmdTask = new CmdTask(bashCommand, null);
		cmdTask.run(true);
	}

	//打包SDK
	private static void packageSdk() {
		final String eclipse_libs_dir = eclipse_dir + "/libs";
		final String sdk_out_asset_dir = eclipse_dir + "/assets";
		final String sdk_out_child_res_dir = eclipse_dir + "/res";
		final String as_out_libs_path = (android_studio_dir + "/libs");
		final String tools_doc = QBUILD_MASTER_PATH + "/tools/doc";

		// 复制所有的aar到AndroidStudio/libs
		final String aar_dirs[] = { "sdk_common", "sdk_in", "sdk_login", "sdk_pay", "sdk_social", "sdk_support" };
		new File(as_out_libs_path).mkdirs();
		for (int i = 0; i < aar_dirs.length; i++) {
			final String path = QBUILD_MASTER_PATH + "/" + aar_dirs[i] + "/build/outputs/aar/" + aar_dirs[i] + ".aar";
			FileUtils.copy(path, as_out_libs_path + File.separator + aar_dirs[i] + ".aar");
		}

		ArrayList<File> aars = new ArrayList<>();
		File aarDir = new File(as_out_libs_path);
		File aarFiles[] = aarDir.listFiles();

		// 解压所有aar，以便下面遍历aar取出jar文件
		for (File f : aarFiles) {
			if (f.getAbsolutePath().toLowerCase().endsWith(dian_aar)) {
				File targetFile = new File(f.getAbsolutePath().replace(dian_aar, ""));
				if (targetFile.exists() && targetFile.isDirectory()) {
					FileUtils.delete(targetFile.getAbsolutePath());
				}

				String destDirPath = "";
				if (f.getAbsolutePath().toLowerCase().endsWith(dian_aar)) {
					destDirPath = f.getAbsolutePath().substring(0, f.getAbsolutePath().length() - dian_aar.length());
				}

				ZipUtils.unZip(f.getAbsolutePath(), destDirPath);
				aars.add(new File(destDirPath));
				PackTools.Printer.print("unzip aar path=" + destDirPath);
			} else {
				FileUtils.delete(f.getAbsolutePath());
			}
		}

		// 遍历解压后的aar文件夹，把需要的jar文件复制到eclipse/libs目录
		for (File _dir : aars) {
			File aar2[] = _dir.listFiles();
			final String eclipse_assets_dir = eclipse_dir + "/assets";
			final File libsDir = new File(eclipse_libs_dir);
			if (!libsDir.exists()) {
				libsDir.mkdirs();
			}

			for (File childF : aar2) {
				if (childF.isDirectory()) {
					if (childF.getName().equals("libs")) {
						FileUtils.copyDirectory(childF.getAbsolutePath(), eclipse_libs_dir);
					} else if (childF.getName().equals("assets")) {
						FileUtils.copyDirectory(childF.getAbsolutePath(), eclipse_assets_dir);
					}
				} else {
					if (childF.getName().equals("classes.jar")) {
						File childDestF = new File(eclipse_libs_dir + File.separator + _dir.getName() + ".jar");
						childF.renameTo(childDestF);
					}
				}
			}
		}

		// 复制exclude_libs下的jar到AndroidStudio/libs,Eclipse/libs
		final String exclude_libs = (QBUILD_MASTER_PATH + "/tools/exclude_libs");
		FileUtils.copyDirectory(exclude_libs, as_out_libs_path);
		FileUtils.copyDirectory(exclude_libs, eclipse_libs_dir);

		// 复制sdk_in/assets的如下几个文件夹到SDK assets输出目录
		final String from_assets_path = (QBUILD_MASTER_PATH + "/sdk_in/src/main/assets");
		FileUtils.copyDirectory(from_assets_path, sdk_out_asset_dir);
		PackTools.Printer.print("copy assets finish=" + sdk_out_asset_dir);

		// 复制sdk_in/res的如下几个文件夹到SDK res输出目录
		final String from_res_path = (QBUILD_MASTER_PATH + "/sdk_in/src/main/res");
		FileUtils.copyDirectory(from_res_path, sdk_out_child_res_dir);
		PackTools.Printer.print("copy res finish=" + sdk_out_child_res_dir);

		// 复制sdk_in/libs的如下几个文件夹到SDK libs输出目录
		PackTools.Printer.print("copy demo jar of libs to sdk libs start");
		final String from_so_path = (QBUILD_MASTER_PATH + "/sdk_in/src/main/jniLibs");
		FileUtils.copyDirectory(from_so_path, eclipse_dir + "/libs");

		// 复制sdk_in,sdk_social_pay_demo_online下的AndroidManifest.xml到eclipse SDK输出目录
		FileUtils.copy(QBUILD_MASTER_PATH + "/sdk_in/src/main/AndroidManifest.xml", eclipse_dir + "/AndroidManifest.xml");
		FileUtils.copy(QBUILD_MASTER_PATH + "/sdk_social_pay_demo_online/src/main/AndroidManifest.xml", eclipse_dir + "/AndroidManifest2.xml");

		// 复制接入文档等到SDK输出目录
		PackTools.Printer.print("copy SdkDocuments to sdk dir start");
		File dosFile = new File(tools_doc);
		FileUtils.copy(dosFile.getAbsolutePath() + "/AndroidManifest_README.txt", eclipse_dir + "/AndroidManifest_README.txt");
		File sdkDocFile = new File(dosFile.getAbsolutePath() + "/SDK_DOC.zip");
		if (sdkDocFile.exists()) {
			FileUtils.copy(sdkDocFile.getAbsolutePath(), SDK_OUT_PATH + "/SDK_DOC.zip");
		} else {
			FileUtils.delete(SDK_OUT_PATH);
			PackTools.Printer.print("java package failure:" + sdkDocFile.getAbsolutePath() + "is not exists");
			throw new NullPointerException(sdkDocFile.getAbsolutePath() + "is not exists");
		}

		// 删除解压后的aar文件夹
		PackTools.Printer.print("delete aar dir start");
		for (File _dir : aars) {
			FileUtils.delete(_dir.getAbsolutePath());
		}

		// 删除demo编译后产生的不用文件
		final String to_demo_path = (android_studio_dir + "/DemoProject");
		FileUtils.delete(to_demo_path + "/sdk_social_pay_demo_online/build");
		FileUtils.delete(to_demo_path + "/.gradle");
		FileUtils.delete(to_demo_path + "/.idea");

		// 以下代码的操作分3步，主要是处理sdk_common.aar里面的不同的QhSdkToggle.jar：
		PackTools.Printer.print("copy QhSdkToggle.jar start");
		final String sdk_common_aars[] = { "dudai_off-danji_off-sdk_common.aar", "dudai_off-danji_on-sdk_common.aar", "dudai_on-danji_off-sdk_common.aar", "dudai_on-danji_on-sdk_common.aar" };
		final String sdk_types[] = { "dudai_off-danji_off", "dudai_off-danji_on", "dudai_on-danji_off", "dudai_on-danji_on" };
		final String sdk_isDanji_types[] = { "false", "true", "false", "true" };
		final String sdk_isDudai_types[] = { "false", "false", "true", "true" };

		// 复制360SDK_Android_OnlineGame为四个sdk_types[]名字文件夹，然后各自复制QhSdkToggle.jar后打包成zip
		zip_count = 0;
		for (int i = 0; i < sdk_types.length; i++) {
			final File exclude_jars[] = (new File(exclude_libs)).listFiles();
			final ArrayList<String> array_merge_jars = new ArrayList<String>();
			for (File jar : exclude_jars) {
				array_merge_jars.add(jar.getName());
			}

			final int index = i;
			new Thread() {
				public void run() {
					boolean isDanji = Boolean.parseBoolean(sdk_isDanji_types[index]);
					boolean is_dudai = Boolean.parseBoolean(sdk_isDudai_types[index]);
					final String zip_dir = PackTools.getZipName(SDK_OUT_PATH, QBUILD_MASTER_PATH, isDanji, is_dudai);

					// 1.复制主360SDK_Android_OnlineGame文件夹，复制4份
					PackTools.Printer.print("copy dir for zip xx start...".replace("xx", sdk_types[index]));
					FileUtils.copyDirectory(SDK_OUT_PATH, zip_dir);
					PackTools.Printer.print("copy dir for zip xx end".replace("xx", sdk_types[index]));

					// 2.复制编译变种的sdk_common.aar到对应的AndroidStudio/libs/aars下
					final String build_variants_sdk_common_aar = QBUILD_OUT + File.separator + sdk_common_aars[index];
					final String as_libs_aar = (zip_dir + "/AndroidStudio/libs/sdk_common.aar");
					FileUtils.copy(build_variants_sdk_common_aar, as_libs_aar);

					// 2.1 解压复制编译变种的sdk_common.aar的classes.jar重命名为sdk_common.jar后复制到/Eclipse/libs下
					final String eclipse_libs = zip_dir + "/Eclipse/libs";
					String destDirPath = build_variants_sdk_common_aar.replace(dian_aar, "");
					ZipUtils.unZip(build_variants_sdk_common_aar, destDirPath);
					FileUtils.copy(destDirPath + "/classes.jar", eclipse_libs + "/sdk_common.jar");
					FileUtils.delete(destDirPath);
					FileUtils.delete(build_variants_sdk_common_aar);

					// 2.2 把AndroidStudio/libs下的所有aar和jar复制对应的demo工程libs下
					final String demo_sdk_common_aar_path = zip_dir + "/AndroidStudio/DemoProject/sdk_social_pay_demo_online/libs/sdk_common.aar";
					FileUtils.copy(as_libs_aar, demo_sdk_common_aar_path);
					
					// 2.3 复制online,offline接入文档和版本升级日志，并重命名(添加版本号等)
					String version_name_code = PackTools.getSdkVersionNameCode(QBUILD_MASTER_PATH);
					if (isDanji) {
						version_name_code = "100" + version_name_code;
					}
					final String doc_name = "/360SDK_Android_xxxGame_v" + version_name_code + "_Eclipse_doc.docx";
					final String doc_name1 = "/360SDK_Android_xxxGame_v" + version_name_code + "_AndroidStudio_doc.docx";
					final String doc_name2 = "/360xxxSdk_version_upgrade_log.txt";
					if (isDanji) {
						FileUtils.copy(dosFile.getAbsolutePath() + "/social_pay_OfflineGame.docx", zip_dir + "/Eclipse/" + doc_name.replace("xxx", "Offline"));
						FileUtils.copy(dosFile.getAbsolutePath() + "/social_pay_androidStudio.docx", zip_dir + "/AndroidStudio/" + doc_name1.replace("xxx", "Offline"));
						FileUtils.copy(dosFile.getAbsolutePath() + "/version_text_OfflineGame.txt", zip_dir + "/Eclipse/" + doc_name2.replace("xxx", "offline"));
						FileUtils.copy(dosFile.getAbsolutePath() + "/version_text_OfflineGame.txt", zip_dir + "/AndroidStudio/" + doc_name2.replace("xxx", "offline"));
					} else {
						FileUtils.copy(dosFile.getAbsolutePath() + "/social_pay_OnlineGame.docx", zip_dir + "/Eclipse/" + doc_name.replace("xxx", "Online"));
						FileUtils.copy(dosFile.getAbsolutePath() + "/social_pay_androidStudio.docx", zip_dir + "/AndroidStudio/" + doc_name1.replace("xxx", "Online"));
						FileUtils.copy(dosFile.getAbsolutePath() + "/version_text_OnlineGame.txt", zip_dir + "/Eclipse/" + doc_name2.replace("xxx", "online"));
						FileUtils.copy(dosFile.getAbsolutePath() + "/version_text_OnlineGame.txt", zip_dir + "/AndroidStudio/" + doc_name2.replace("xxx", "online"));
					}

					// 2.4 demo-apk文件夹下只保留当前SDK对应的apk
					if (isDanji) {
						FileUtils.delete(zip_dir + "/demo-apk/360SDK_Android_OnlineGame.apk");
					} else {
						FileUtils.delete(zip_dir + "/demo-apk/360SDK_Android_offlineGame.apk");
						FileUtils.delete(zip_dir + "/Eclipse/libs/holmes1.3.4.0.jar");
						FileUtils.delete(zip_dir + "/AndroidStudio/libs/holmes1.3.4.0.jar");
					}

					// 3.合并/Eclipse/libs下的jar为360SDK.jar
					PackTools.Printer.print("merge xx jars start".replace("xx", sdk_types[index]));
					final String temp_libs = QBUILD_OUT + File.separator + sdk_types[index] + "/temp_libs";
					new File(temp_libs).mkdirs();
					final String merge_jars_sh = QBUILD_MASTER_PATH + "/tools/sh/merge_jars.sh";
					final File libsDirs[] = new File(eclipse_libs).listFiles();
					for (File jar : libsDirs) {
						if (jar.getName().endsWith(".jar") && !array_merge_jars.contains(jar.getName())) {
							FileUtils.copy(jar.getAbsolutePath(), temp_libs + File.separator + jar.getName());
							jar.delete();
						}
					}
					PackTools.Printer.print("execute java for xx merge_jars.sh shell".replace("xx", sdk_types[index]));
					final String bashCommand = "sh " + merge_jars_sh + " " + temp_libs;
					mergeJarsTo360SDKjar(bashCommand);
					PackTools.Printer.print("copy 360SDK.jar to xx eclipse dir".replace("xx", sdk_types[index]));
					FileUtils.copy(temp_libs + "/360SDK.jar", eclipse_libs + "/360SDK.jar");
					PackTools.Printer.print("deleting xx dir ...".replace("xx", sdk_types[index]));
					FileUtils.delete(QBUILD_OUT + File.separator + sdk_types[index]);
					PackTools.Printer.print("merge xx jars end".replace("xx", sdk_types[index]));

					// 4.把目录打包成zip
					zipSdkDir(zip_dir, zip_dir + ".zip");
					zip_count++;
				};
			}.start();

		}

		while (true) {
			try {
				Thread.sleep(15 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (zip_count == sdk_types.length) {
				FileUtils.delete(SDK_OUT_PATH);
				break;
			}
		}
	}

	private static void zipSdkDir(String zip_from_dir, String zip_to_dir) {
		PackTools.Printer.print(zip_to_dir + " to zip start");
		ZipUtils.zip(zip_from_dir, zip_to_dir);
		FileUtils.delete(zip_from_dir);
		PackTools.Printer.print(zip_to_dir + " to zip end");
	}

	private static void mergeJarsTo360SDKjar(String bashCommand) {
		CmdTask cmdTask = new CmdTask(bashCommand, null);
		cmdTask.run(false);
	}

}
