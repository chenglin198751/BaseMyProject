package com.qihoo360.common.ad;

import android.text.TextUtils;

import com.qihoo.utils.LogUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

class XmAdStrategyUtils {
    private static final String TAG = "XmAdStrategy";
    private static AdConfig mAdConfig = null;
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String KEY_TODAY = "key_today";
    private static final String KEY_AD_SHOWN_TOTAL_COUNT = "key_ad_shown_total_count";
    private static final String PLAY_DEFINE_GAME = "play_define_game";
    static String adConfJsonStr;

    public static ArrayList<Integer> toIntList(JSONArray jsonArray) {
        ArrayList<Integer> arrayList = new ArrayList<>();
        if (jsonArray == null) {
            return arrayList;
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                Object obj = jsonArray.get(i);
                arrayList.add(Integer.parseInt((String) obj));
            } catch (Exception e) {
                arrayList.add(0);
            }
        }
        return arrayList;
    }

    public static ArrayList<String> toStringList(JSONArray jsonArray) {
        ArrayList<String> arrayList = new ArrayList<>();
        if (jsonArray == null) {
            return arrayList;
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                Object obj = jsonArray.get(i);
                arrayList.add((String) obj);
            } catch (Exception e) {
                arrayList.add("");
            }
        }
        return arrayList;
    }

    // 保存当前设备、每日、广告展示的总count
    public static void saveAdShownTotalCount(int count) {
        XmAdStrategyStorage.get().saveInt(KEY_AD_SHOWN_TOTAL_COUNT, count);
    }

    // 获取当前设备、每日、广告展示的总count
    public static int getAdShownTotalCount() {
        return XmAdStrategyStorage.get().getInt(KEY_AD_SHOWN_TOTAL_COUNT, 0);
    }

    // 保存：（计数）当前设备、每日、单个游戏、对应open_type的总次数
    public static void saveSingleGameOpenTypeCount(String game_id, String open_type, int count) {
        String key = "OpenOrCloseCount_" + game_id + "_" + open_type;
        XmAdStrategyStorage.get().saveInt(key, count);
    }

    // 获取：（计数）当前设备、每日、单个游戏、对应open_type的总次数
    public static int getSingleGameOpenTypeCount(String game_id, String open_type) {
        String key = "OpenOrCloseCount_" + game_id + "_" + open_type;
        return XmAdStrategyStorage.get().getInt(key, 0);
    }

    // 保存：只针对玩游戏中3个场景（play_apk,play_h5,play_define_game）：单用户单游戏，当天最多出现插屏广告的次数。
    public static void savePlayAdMaxTimes(String game_id, String open_type, int count) {
        String key = "PlayAdMaxTimes_" + game_id + "_" + open_type;
        XmAdStrategyStorage.get().saveInt(key, count);
    }

    // 获取：只针对玩游戏中3个场景（play_apk,play_h5,play_define_game）：单用户单游戏，当天最多出现插屏广告的次数。
    public static int getPlayAdMaxTimes(String game_id, String open_type) {
        String key = "PlayAdMaxTimes_" + game_id + "_" + open_type;
        return XmAdStrategyStorage.get().getInt(key, 0);
    }

    public static AdConfig getAdConfig() {
        if (mAdConfig != null) {
            return mAdConfig;
        }

        if (TextUtils.isEmpty(adConfJsonStr)) {
            return new AdConfig();
        }

        try {
            JSONObject joRoot = new JSONObject(adConfJsonStr);
            if (joRoot.optInt("code") != 0) {
                LogUtils.v(TAG, "getAdConfig() error:code!=0");
                return new AdConfig();
            }
            JSONObject jsonData = joRoot.getJSONObject("data");
            String[] array = {XmAdStrategy.close_apk, XmAdStrategy.close_h5, //
                    XmAdStrategy.open_apk, XmAdStrategy.open_h5,//
                    XmAdStrategy.play_apk, XmAdStrategy.play_h5,//
                    PLAY_DEFINE_GAME};
            mAdConfig = new AdConfig();
            for (String open_type : array) {
                if (jsonData.has(open_type)) {
                    JSONObject jsonChild = jsonData.getJSONObject(open_type);
                    //1、玩游戏中
                    if (XmAdStrategy.play_apk.equals(open_type) || XmAdStrategy.play_h5.equals(open_type) || PLAY_DEFINE_GAME.equals(open_type)) {
                        AdChildPlayConfig adChildPlayConfig = new AdChildPlayConfig();
                        adChildPlayConfig.ad_show_trigger_minutes = XmAdStrategyUtils.toIntList(jsonChild.optJSONArray("ad_show_trigger_minutes"));
                        adChildPlayConfig.define_game = XmAdStrategyUtils.toStringList(jsonChild.optJSONArray("define_game"));
                        adChildPlayConfig.status = Integer.parseInt(jsonChild.optString("status", "0"));
                        adChildPlayConfig.exit_duration = Integer.parseInt(jsonChild.optString("exit_duration", "0"));
                        adChildPlayConfig.play_ad_max_times = Integer.parseInt(jsonChild.optString("play_ad_max_times", "0"));
                        switch (open_type) {
                            case XmAdStrategy.play_apk:
                                mAdConfig.play_apk = adChildPlayConfig;
                                break;
                            case XmAdStrategy.play_h5:
                                mAdConfig.play_h5 = adChildPlayConfig;
                                break;
                            case PLAY_DEFINE_GAME:
                                mAdConfig.play_define_game = adChildPlayConfig;
                                break;
                        }
                    }

                    // 打开和关闭游戏：
                    if (XmAdStrategy.open_apk.equals(open_type) || XmAdStrategy.close_apk.equals(open_type) //
                            || XmAdStrategy.open_h5.equals(open_type) || XmAdStrategy.close_h5.equals(open_type)) {
                        AdChildConfig adChildConfig = new AdChildConfig();
                        adChildConfig.ad_show_trigger_times = XmAdStrategyUtils.toIntList(jsonChild.optJSONArray("ad_show_trigger_times"));
                        adChildConfig.status = Integer.parseInt(jsonChild.optString("status", "0"));
                        adChildConfig.exit_duration = Integer.parseInt(jsonChild.optString("exit_duration", "0"));
                        switch (open_type) {
                            case XmAdStrategy.close_apk:
                                mAdConfig.close_apk = adChildConfig;
                                break;
                            case XmAdStrategy.close_h5:
                                mAdConfig.close_h5 = adChildConfig;
                                break;
                            case XmAdStrategy.open_apk:
                                mAdConfig.open_apk = adChildConfig;
                                break;
                            case XmAdStrategy.open_h5:
                                mAdConfig.open_h5 = adChildConfig;
                                break;
                        }
                    }
                }
            }
            mAdConfig.daily_max_ad_show_count = jsonData.optInt("daily_max_ad_show_count", 0);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.v(TAG, "getAdConfig() error:" + e);
            mAdConfig = null;
        }

        return mAdConfig != null ? mAdConfig : new AdConfig();
    }

    public static AdChildConfig getAdChildConfig(String open_type) {
        AdChildConfig adChildConfig = null;
        switch (open_type) {
            case XmAdStrategy.close_apk:
                adChildConfig = mAdConfig.close_apk;
                break;
            case XmAdStrategy.close_h5:
                adChildConfig = mAdConfig.close_h5;
                break;
            case XmAdStrategy.open_apk:
                adChildConfig = mAdConfig.open_apk;
                break;
            case XmAdStrategy.open_h5:
                adChildConfig = mAdConfig.open_h5;
                break;
        }
        return adChildConfig;
    }

    public static AdChildPlayConfig getAdChildPlayConfig(String open_type) {
        AdChildPlayConfig adChildPlayConfig = null;
        switch (open_type) {
            case XmAdStrategy.play_apk:
                adChildPlayConfig = mAdConfig.play_apk;
                break;
            case XmAdStrategy.play_h5:
                adChildPlayConfig = mAdConfig.play_h5;
                break;
        }
        return adChildPlayConfig;
    }

    public static boolean isToday() {
        String today = getToday();
        String savedDate = XmAdStrategyStorage.get().getString(KEY_TODAY, "");
        return savedDate.equals(today);
    }

    private static String getToday() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return sdf.format(new Date());
    }

    public static void refreshSavedDateWithToday() {
        XmAdStrategyStorage.get().saveString(KEY_TODAY, getToday());
    }

    // 当前正在玩的游戏是不是在特定游戏列表中
    public static boolean isPlayDefineGame(String game_id) {
        AdChildPlayConfig play_define_game = XmAdStrategyUtils.getAdConfig().play_define_game;
        return play_define_game != null && play_define_game.isOpen() && play_define_game.define_game != null && play_define_game.define_game.contains(game_id);
    }
}