package cheerly.mybaseproject.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.Context;
import android.text.TextUtils;

public class NotchUtils {
	/**
	 * 是否是刘海屏
	 */
	public static boolean isNotch(Context context) {
		String brand = android.os.Build.BRAND;

		if (TextUtils.isEmpty(brand)) {
			return false;
		}

		if (brand.equalsIgnoreCase("xiaomi")) {
			return isXiaomiNotch(context);
		} else if (brand.equalsIgnoreCase("HONOR") || brand.equalsIgnoreCase("HUAWEI")) {
			return isHuaWeiNotch(context);
		} else if (brand.equalsIgnoreCase("OPPO")) {
			return isOppoNotch(context);
		} else if (brand.equalsIgnoreCase("vivo")) {
			return isVivoNotch(context);
		} else if (brand.equalsIgnoreCase("Meizu")) {
			return isMeizuNotch(context);
		}

		return false;
	}

	/**
	 * 获取刘海屏区域的宽度
	 */
	public static int getNotchWidth(Context context) {
		String brand = android.os.Build.BRAND;

		if (brand.equalsIgnoreCase("xiaomi")) {
			int resourceId = context.getResources().getIdentifier("notch_width", "dimen", "android");
			if (resourceId > 0) {
				return context.getResources().getDimensionPixelSize(resourceId);
			}
		} else if (brand.equalsIgnoreCase("HONOR") || brand.equalsIgnoreCase("HUAWEI")) {
			int[] getNotchSize = getHuaweiNotchSize(context);
			if (getNotchSize[0] > 0) {
				return getNotchSize[0];
			}
		} else if (brand.equalsIgnoreCase("OPPO")) {
			String property = SystemProperties.getString("ro.oppo.screen.heteromorphism");
			// 378,0:702,80
			String[] array = property.split(":");
			String[] array_1 = array[0].split(",");
			String[] array_2 = array[1].split(",");
			int width = Integer.parseInt(array_2[0]) - Integer.parseInt(array_1[0]);
			return width;
		} else if (brand.equalsIgnoreCase("vivo")) {

		}

		return -1;
	}

	/**
	 * 获取刘海屏区域的高度
	 */
	public static int getNotchHeight(Context context) {
		String brand = android.os.Build.BRAND;

		if (brand.equalsIgnoreCase("xiaomi")) {
			int resourceId = context.getResources().getIdentifier("notch_height", "dimen", "android");
			if (resourceId > 0) {
				return context.getResources().getDimensionPixelSize(resourceId);
			}
		} else if (brand.equalsIgnoreCase("HONOR") || brand.equalsIgnoreCase("HUAWEI")) {
			int[] getNotchSize = getHuaweiNotchSize(context);
			if (getNotchSize[1] > 0) {
				return getNotchSize[1];
			}
		} else if (brand.equalsIgnoreCase("OPPO")) {
			String property = SystemProperties.getString("ro.oppo.screen.heteromorphism");
			// 378,0:702,80
			String[] array = property.split(":");
			String[] array_1 = array[0].split(",");
			String[] array_2 = array[1].split(",");
			int height = Integer.parseInt(array_2[1]) - Integer.parseInt(array_1[1]);
			return height;
		}

		return -1;
	}

	/**
	 * 获取华为刘海屏的宽高
	 */
	private static int[] getHuaweiNotchSize(Context context) {
		int[] ret = new int[] { 0, 0 };
		try {
			ClassLoader cl = context.getClassLoader();
			Class HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
			Method get = HwNotchSizeUtil.getMethod("getNotchSize");
			ret = (int[]) get.invoke(HwNotchSizeUtil);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return ret;
		}
	}

	/**
	 * 是否为华为的刘海屏
	 */
	private static boolean isHuaWeiNotch(Context context) {
		boolean ret = false;
		try {
			ClassLoader cl = context.getClassLoader();
			Class HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
			Method get = HwNotchSizeUtil.getMethod("hasNotchInScreen");
			ret = (Boolean) get.invoke(HwNotchSizeUtil);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return ret;
		}
	}

	/**
	 * 是否为vivo的刘海屏
	 */
	private static boolean isVivoNotch(Context context) {
		boolean ret = false;
		try {
			ClassLoader cl = context.getClassLoader();
			Class ftFeature = cl.loadClass("android.util.FtFeature");
			Method get = ftFeature.getMethod("isFeatureSupport", int.class);
			ret = (Boolean) get.invoke(ftFeature, 0x00000020);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return ret;
		}
	}

	/**
	 * 是否为oppo的刘海屏
	 */
	private static boolean isOppoNotch(Context context) {
		try {
			return context.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 是否为小米的刘海屏
	 */
	private static boolean isXiaomiNotch(Context context) {
		try {
			return (SystemProperties.getInt("ro.miui.notch") == 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 是否为魅族的刘海屏
	 */
	private static boolean isMeizuNotch(Context context) {
		boolean fringeDevice = false;
		try {
			Class clazz = Class.forName("flyme.config.FlymeFeature");
			Field field = clazz.getDeclaredField("IS_FRINGE_DEVICE");
			fringeDevice = (Boolean) field.get(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fringeDevice;
	}
}
