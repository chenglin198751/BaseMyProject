package com.wcl.test.update;


/**
 * Created by chenglin on 2017-11-3.
 */

public class HcxVersionModel {
    public int id; //唯一ID
    public String version; //最新版本号
    public String fromVersion; //开始版本号
    public int source; //系统类型：1-iOS 2-Android 3-其他
    public int forceUpgrade = VersionUpdateModel.UPDATE_NORMAL; //是否强制升级：0-普通升级；1-强制升级
    public String title; //升级标题
    public String content; //升级描述
    public String url; //下载链接
    public String updateTime;//更新时间
}
