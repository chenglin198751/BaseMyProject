package preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesData {

	private static SharedPreferences getPreferences(final Context context) {
		return context.getSharedPreferences("preference_1", Context.MODE_PRIVATE);
	}

	/** 最后的登录人ID */
	private static final String lastLoginId = "lastLoginId";

	public static void setLastLoginId(final Context context, String lastLogin) {
		SharedPreferences prefs = getPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(lastLoginId, lastLogin);
		editor.commit();
	}

	public static String getLastLoginId(final Context context) {
		return getPreferences(context).getString(lastLoginId, "");
	}

}
