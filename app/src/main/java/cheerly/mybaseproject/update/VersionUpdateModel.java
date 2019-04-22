package cheerly.mybaseproject.update;


import android.support.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by chenglin on 2017-11-3.
 */

public class VersionUpdateModel {
    public static transient final int UPDATE_NORMAL = 0;//正常更新
    public static transient final int UPDATE_FORCE = 1;//强制更新

    public String versionName; //最新版本号
    private int updateType = UPDATE_NORMAL; //是否强制升级：0-普通升级；1-强制升级
    public String title; //升级标题
    public String content; //升级描述
    public String url; //下载链接


    @IntDef({
            UPDATE_NORMAL,
            UPDATE_FORCE,
    })

    //表示注解作用范围，参数注解，成员注解，方法注解
    @Target({
            ElementType.PARAMETER,
            ElementType.FIELD,
            ElementType.METHOD,
    })


    //表示注解所存活的时间,在运行时,而不会存在 .class 文件中
    @Retention(RetentionPolicy.SOURCE)

    //接口，定义新的注解类型
    public @interface UpdateType {
    }

    public void setUpdateType(@UpdateType int updateType) {
        this.updateType = updateType;
    }

    public int getUpdateType() {
        return this.updateType;
    }
}
