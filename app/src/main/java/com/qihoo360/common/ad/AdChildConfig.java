package com.qihoo360.common.ad;

import java.util.ArrayList;

class AdChildConfig {
    //比如，第一次，第三次，第五次，打开或者关闭游戏时弹出插屏广告
    public ArrayList<Integer> ad_show_trigger_times;

    //0关，1开
    public int status;

    //单用户单游戏，当天最多出现插屏广告的次数。只针对游戏中插屏广告场景
    public int maxTimes;

    //只针对玩游戏中场景：特定游戏game_id列表，不受play_apk和play_h5开关控制
    public ArrayList<String> define_game;

    public boolean isAdOpen() {
        return status == 1;
    }
}
