package com.wcl.test.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.wcl.test.base.BaseApp;

public class UserManager {

	private static SharedPreferences getPreferences(final Context context) {
		return context.getSharedPreferences("user_manager", Context.MODE_PRIVATE);
	}

	public static void clear(){
		getPreferences(BaseApp.getApp()).edit().clear().apply();
	}

	/** 登录ID */
	private static final String uid = "uid";

	public static void setUid(String lastLogin) {
		SharedPreferences prefs = getPreferences(BaseApp.getApp());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(uid, lastLogin);
		editor.commit();
	}

	public static String getUid() {
		return getPreferences(BaseApp.getApp()).getString(uid, "");
	}

}
