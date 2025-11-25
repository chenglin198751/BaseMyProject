package com.qihoo360.common.ad;


import android.text.TextUtils;

import androidx.annotation.Keep;

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
    public static final String play_apk = "play_apk";
    public static final String play_h5 = "play_h5";

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
        LogUtils.i(TAG, "getAdShownTotalCount()=" + totalCount + ",daily_max_ad_show_count=" + XmAdStrategyUtils.getAdConfig().daily_max_ad_show_count);

        if (totalCount < XmAdStrategyUtils.getAdConfig().daily_max_ad_show_count) {
            int singleGameOpenOrCloseCount = XmAdStrategyUtils.getSingleGameOpenOrCloseCount(game_id, open_type) + 1;

            // 1、打开和关闭游戏：
            if (open_apk.equals(open_type) || close_apk.equals(open_type) //
                    || open_h5.equals(open_type) || close_h5.equals(open_type)) {
                AdChildConfig adChildConfig = XmAdStrategyUtils.getAdChildConfig(open_type);
                if (adChildConfig != null && adChildConfig.isOpen()) {
                    if (isCounting(game_id, open_type)) {
                        // 每次打开游戏，如果对应广告开关是开，则递增记录打开或关闭总次数
                        XmAdStrategyUtils.saveSingleGameOpenOrCloseCount(game_id, open_type, singleGameOpenOrCloseCount);
                    }
                    ArrayList<Integer> ad_show_trigger_times = adChildConfig.ad_show_trigger_times;
                    LogUtils.i(TAG, "game_id=" + game_id + ",open_type=" + open_type + //
                            ",ad_show_trigger_times=" + ad_show_trigger_times + ",singleGameOpenOrCloseCount=" + singleGameOpenOrCloseCount);
                    if (ad_show_trigger_times.contains(singleGameOpenOrCloseCount)) {
                        // 展示广告后，则递增记录广告总count
                        if (isCounting(game_id, open_type)) {
                            LogUtils.v(TAG, "game_id=" + game_id + ",shouldShowAd=true" + ",getAdShownTotalCount()=" + (totalCount + 1));
                            XmAdStrategyUtils.saveAdShownTotalCount(totalCount + 1);
                        }
                        return true;
                    }
                }
            }

            // 2、玩游戏中：
            if (play_apk.equals(open_type) || play_h5.equals(open_type)) {
                // 先检查玩游戏的特定游戏列表（优先级最高），再检查普通玩游戏中
                boolean isPlayDefineGame = XmAdStrategyUtils.isPlayDefineGame(game_id);
                String tag2 = isPlayDefineGame ? "玩游戏中命中特定游戏列表:" : "普通玩游戏中:";
                int playAdMaxTimes = XmAdStrategyUtils.getPlayAdMaxTimes(game_id, open_type);
                AdChildPlayConfig playConfig = XmAdStrategyUtils.getAdChildPlayConfig(open_type);
                if (isPlayDefineGame || (playConfig != null && playConfig.isOpen())) {
                    XmAdStrategyUtils.saveSingleGameOpenOrCloseCount(game_id, open_type, singleGameOpenOrCloseCount);
                    LogUtils.i(TAG, tag2 + "game_id=" + game_id + ",open_type=" + open_type + ",singleGameOpenOrCloseCount=" + singleGameOpenOrCloseCount + ",define_game=" + playConfig.define_game);
                    LogUtils.i(TAG, tag2 + "playAdMaxTimes=" + playAdMaxTimes + ",play_ad_max_times=" + playConfig.play_ad_max_times);
                    if (playAdMaxTimes < playConfig.play_ad_max_times) {
                        ArrayList<Integer> ad_show_trigger_minutes = playConfig.ad_show_trigger_minutes;
                        LogUtils.i(TAG, tag2 + "ad_show_trigger_times=" + ad_show_trigger_minutes);
                        if (ad_show_trigger_minutes.contains(singleGameOpenOrCloseCount)) {
                            XmAdStrategyUtils.savePlayAdMaxTimes(game_id, open_type, playAdMaxTimes + 1);
                            XmAdStrategyUtils.saveAdShownTotalCount(XmAdStrategyUtils.getAdShownTotalCount() + 1);
                            LogUtils.v(TAG, tag2 + "game_id=" + game_id + ",open_type=" + open_type + ",shouldShowAd=true");
                            return true;
                        }
                    }
                }
            }
        }
        LogUtils.i(TAG, "game_id=" + game_id + ",open_type=" + open_type + ",shouldShowAd=false");
        return false;
    }

    // 2025-11-19:默认不开始计数且返回播放广告，目的是兼容news插件bug(没加载到广告不返回任何回调)，
    // 插件是打开apk和H5（必须计数，不接受外部设置），
    // 主程是关闭apk和H5（默认不计数，外部设置是否计数）
    // 此方法被调用时一定是展示广告了
    public static void startCounting(String game_id, String open_type) {
        LogUtils.d(TAG, "IntersAd,startCounting(),game_id:" + game_id + ",open_type:" + open_type);
        if (close_apk.equals(open_type) || close_h5.equals(open_type)) {
            String key = game_id + "_" + open_type;
            if (!countingList.contains(key)) {
                countingList.add(key);
                int singleGameAdShownCount = XmAdStrategyUtils.getSingleGameOpenOrCloseCount(game_id, open_type);
                if (singleGameAdShownCount == 0) {
                    // 当第一次执行时，本地存储需要更新打开次数
                    XmAdStrategyUtils.saveSingleGameOpenOrCloseCount(game_id, open_type, 1);
                    XmAdStrategyUtils.saveAdShownTotalCount(1);
                }
            }
        }
    }

    // 退出游戏时判断玩家玩了多少分钟，才会出现插屏广告
    public static int getExitDuration(String open_type) {
        if (close_apk.equals(open_type) || close_h5.equals(open_type)) {
            AdChildConfig child = XmAdStrategyUtils.getAdChildConfig(open_type);
            if (child != null && child.exit_duration > 0) {
                return child.exit_duration;
            }
        }
        return -1;
    }

    // 判断是否需要计数
    private static boolean isCounting(String game_id, String open_type) {
        if (close_apk.equals(open_type) || close_h5.equals(open_type)) {
            String key = game_id + "_" + open_type;
            return countingList.contains(key);
        }
        return true;
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




