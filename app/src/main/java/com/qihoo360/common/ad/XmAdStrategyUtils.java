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
    static String adConfJsonStr;

    public static ArrayList<Integer> convertJSONArrayToArrayList(JSONArray jsonArray) {
        ArrayList<Integer> arrayList = new ArrayList<>();
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

    // 保存当前设备、每日、广告展示的总count
    public static void saveAdShownTotalCount(int count) {
        XmAdStrategyStorage.get().saveInt(KEY_AD_SHOWN_TOTAL_COUNT, count);
    }

    // 获取当前设备、每日、广告展示的总count
    public static int getAdShownTotalCount() {
        return XmAdStrategyStorage.get().getInt(KEY_AD_SHOWN_TOTAL_COUNT, 0);
    }

    // 保存当前设备、每日、单个游戏、其open_type广告展示的总count
    public static void saveSingleGameAdShownCount(String game_id, String open_type, int count) {
        String key = game_id + "_" + open_type;
        XmAdStrategyStorage.get().saveInt(key, count);
    }

    // 获取当前设备、每日、单个游戏、其open_type广告展示的总count
    public static int getSingleGameAdShownCount(String game_id, String open_type) {
        String key = game_id + "_" + open_type;
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
            String[] array = {XmAdStrategy.close_apk, XmAdStrategy.close_h5, XmAdStrategy.open_apk, XmAdStrategy.open_h5};
            mAdConfig = new AdConfig();
            for (String str : array) {
                if (jsonData.has(str)) {
                    AdChildConfig adChildConfig = new AdChildConfig();
                    JSONObject jsonChild = jsonData.getJSONObject(str);
                    JSONArray jsonChildArray = jsonChild.optJSONArray("ad_show_trigger_times");
                    if (jsonChildArray != null) {
                        adChildConfig.ad_show_trigger_times = XmAdStrategyUtils.convertJSONArrayToArrayList(jsonChildArray);
                    }
                    adChildConfig.status = Integer.parseInt(jsonChild.optString("status"));
                    switch (str) {
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

}