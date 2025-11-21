package com.qihoo360.common.ad;

import android.support.annotation.Keep;
import android.text.TextUtils;

import com.qihoo.utils.LogUtils;
import com.qihoo360.common.HttpURLConnectionUtils;
import com.qihoo360.common.helper.UrlUtils;

import java.util.ArrayList;

/**
 * 小猫快玩插屏广告策略实现
 */
@Keep
public class XmAdStrategy {
    private static final String TAG = "XmAdStrategy";
    private static final ArrayList<String> countingList = new ArrayList<>();

    public static final String open_apk = "open_apk";
    public static final String close_apk = "close_apk";
    public static final String open_h5 = "open_h5";
    public static final String close_h5 = "close_h5";
    public static final String specific_game = "specific_game";
    public static final String playing_apk = "playing_apk";
    public static final String playing_h5 = "playing_h5";

    // 2025-11-19:默认不开始计数且返回播放广告，目的是兼容news插件bug(没加载到广告不返回任何回调)，
    // 插件是打开apk和H5（必须计数，不接受外部设置），
    // 主程是关闭apk和H5（默认不计数，外部设置是否计数）
    // 此方法被调用时一定是展示广告了
    public static void startCounting(String game_id, String open_type) {
        LogUtils.d("IntersAd", "startCounting:" + game_id + "   open_type::" + open_type);
        if (close_apk.equals(open_type) || close_h5.equals(open_type)) {
            String key = game_id + "_" + open_type;
            if (!countingList.contains(key)) {
                countingList.add(key);
                int singleGameAdShownCount = XmAdStrategyUtils.getSingleGameAdShownCount(game_id, open_type);
                if (singleGameAdShownCount == 0) {
                    // 当第一次执行时，本地存储需要更新打开次数
                    XmAdStrategyUtils.saveSingleGameAdShownCount(game_id, open_type, 1);
                    XmAdStrategyUtils.saveAdShownTotalCount(1);
                }
            }
        }
    }

    // 判断是否需要计数
    private static boolean isCounting(String game_id, String open_type) {
        if (close_apk.equals(open_type) || close_h5.equals(open_type)) {
            String key = game_id + "_" + open_type;
            return countingList.contains(key);
        }
        return true;
    }

    // 是否需要展示插屏广告
    @Keep
    public static boolean shouldShowAd(String game_id, String open_type) {
        // 如果不是当日，则重置用户广告count等数据
        LogUtils.i(TAG, "--------------------------------------------");
        if (!XmAdStrategyUtils.isToday()) {
            LogUtils.i(TAG, "It's not today. All data needs to be reset");
            XmAdStrategyStorage.get().clear();
            XmAdStrategyUtils.refreshSavedDateWithToday();
        }

        int totalCount = XmAdStrategyUtils.getAdShownTotalCount();
        LogUtils.i(TAG, "getAdShownTotalCount()=" + totalCount + //
                ",daily_max_ad_show_count=" + XmAdStrategyUtils.getAdConfig().daily_max_ad_show_count);

        if (totalCount < XmAdStrategyUtils.getAdConfig().daily_max_ad_show_count) {
            AdChildConfig adChildConfig = XmAdStrategyUtils.getAdChildConfig(open_type);
            if (adChildConfig != null && adChildConfig.isAdOpen()) {
                // 每次打开游戏，如果对应广告开关是开，则递增记录打开次数
                int singleGameAdShownCount = XmAdStrategyUtils.getSingleGameAdShownCount(game_id, open_type) + 1;
                if (isCounting(game_id, open_type)) {
                    XmAdStrategyUtils.saveSingleGameAdShownCount(game_id, open_type, singleGameAdShownCount);
                }

                ArrayList<Integer> ad_show_trigger_times = adChildConfig.ad_show_trigger_times;
                LogUtils.i(TAG, "game_id=" + game_id + //
                        ",ad_show_trigger_times=" + ad_show_trigger_times + //
                        ",adShownCountForSingleGame=" + singleGameAdShownCount);
                if (ad_show_trigger_times.contains(singleGameAdShownCount)) {

                    // 展示广告后，则递增记录广告总count
                    if (isCounting(game_id, open_type)) {
                        LogUtils.i(TAG, "game_id=" + game_id + ",shouldShowAd=true" + //
                                ",getAdShownTotalCount()=" + (totalCount + 1));
                        XmAdStrategyUtils.saveAdShownTotalCount(totalCount + 1);
                    }
                    return true;
                }
            }
        }

        LogUtils.i(TAG, "game_id=" + game_id + ",shouldShowAd=false");
        return false;
    }

    public static void initAdStrategyConfig() {
        try {
            if (!TextUtils.isEmpty(XmAdStrategyUtils.adConfJsonStr)) {
                return;
            }

            HttpURLConnectionUtils.get(UrlUtils.getAdConfUrl(), new HttpURLConnectionUtils.HttpCallback() {
                @Override
                public void onSuccess(String response) {
                    LogUtils.i(TAG, "getAdStrategyConfig()=" + response);
                    XmAdStrategyUtils.adConfJsonStr = response;
                }

                @Override
                public void onError(Exception e) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.v(TAG, "initAdStrategyConfig() error:" + e);
        }
    }
}




