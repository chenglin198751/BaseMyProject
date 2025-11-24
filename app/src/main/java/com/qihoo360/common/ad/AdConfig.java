package com.qihoo360.common.ad;

class AdConfig {
    //单用户当天展示的广告总次数
    public int daily_max_ad_show_count;

    public AdChildConfig close_apk;
    public AdChildConfig close_h5;
    public AdChildConfig open_apk;
    public AdChildConfig open_h5;

    public AdChildPlayConfig play_define_game;
    public AdChildPlayConfig play_apk;
    public AdChildPlayConfig play_h5;
}
