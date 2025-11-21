package com.qihoo360.common.ad;

class AdConfig {
    //单用户当天展示的广告总次数
    public int daily_max_ad_show_count;

    //玩游戏时长超过多少分钟后，退出H5或APK游戏才会出现插屏广告
    public int exit_duration;

    public AdChildConfig close_apk;
    public AdChildConfig close_h5;
    public AdChildConfig open_apk;
    public AdChildConfig open_h5;

    public AdChildConfig play_define_game;
    public AdChildConfig play_apk;
    public AdChildConfig play_h5;
}
