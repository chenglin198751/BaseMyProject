package com.wcl.test.utils;

import java.lang.reflect.Method;

public class SystemProperties {

	public static int getInt(final String key) {
		try {
			Method getIntMethod = Class.forName("android.os.SystemProperties").getMethod("getInt", String.class, int.class);
			return ((Integer) getIntMethod.invoke(null, key, -1)).intValue();
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	public static String getString(String key) {
		String value = null;
		Class<?> cls = null;
		try {
			cls = Class.forName("android.os.SystemProperties");
			Method hideMethod = cls.getMethod("get", String.class);
			Object object = cls.newInstance();
			value = (String) hideMethod.invoke(object, key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}

}
