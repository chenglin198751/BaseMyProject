package com.qihoo360.common.ad;

import java.util.ArrayList;

class AdChildPlayConfig {
    //比如，第一次，第三次，第五次，打开或者关闭游戏时弹出插屏广告
    public ArrayList<Integer> ad_show_trigger_minutes;

    //0关，1开
    public int status;

    //只针对游戏退出场景（close_apk,close_h5）：退出游戏时判断玩家玩了多少分钟，才会出现插屏广告
    public int exit_duration;

    //只针对玩游戏中3个场景（play_apk,play_h5,play_define_game）：单用户单游戏，当天最多出现插屏广告的次数。
    public int play_ad_max_times;

    //只针对玩游戏中场景（play_apk,play_h5）：特定游戏id列表
    public ArrayList<String> define_game;

    public boolean isOpen() {
        return status == 1;
    }
}
