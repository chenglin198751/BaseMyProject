package update;


/**
 * Created by chenglin on 2017-11-3.
 */

public class VersionUpdateModel {
    public static transient final int UPDATE_NORMAL = 0;//正常更新
    public static transient final int UPDATE_FORCE = 1;//强制更新

    public String versionName; //最新版本号
    public int updateType = -1; //是否强制升级：0-普通升级；1-强制升级
    public String title; //升级标题
    public String content; //升级描述
    public String url; //下载链接
}
