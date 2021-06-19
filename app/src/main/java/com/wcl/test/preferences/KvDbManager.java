//package com.wcl.test.preferences;
//
//import android.text.TextUtils;
//
//import com.qihoo.gamecenter.weareyoung.db.SettingModel;
//import com.qihoo.gamecenter.weareyoung.db.SettingModel_Table;
//import com.raizlabs.android.dbflow.sql.language.SQLite;
//
///**
// * 此类用来存储当前应用的与当前登录用户强相关的缓存数据：
// * 比如切换用户登录了，那么对应的肯定就是新用户的缓存数据；
// * 再比如切换环境了，那么直接清空这个表。
// * Created by weichenglin  on 2018/4/11
// */
//public class KvDbManager {
//
//    @Deprecated
//    public static String wrapperKey(final String key) {
//        String uid = UserManager.getInstance().getUid();
//        if (!TextUtils.isEmpty(uid)) {
//            return key + "_" + uid;
//        } else {
//            return key;
//        }
//    }
//
//    public static void put(String key, String value) {
//        SettingModel model = new SettingModel();
//        model.key = key;
//        model.value = value;
//        model.save();
//    }
//
//    public static String get(String key) {
//        String value = "";
//
//        SettingModel settingModel = SQLite.select()
//                .from(SettingModel.class)
//                .where(SettingModel_Table.key.eq(key))
//                .querySingle();
//
//        if (settingModel != null) {
//            value = settingModel.value;
//        }
//        return value;
//    }
//
//    /**
//     * 以key 作为删除条件，删除一行
//     *
//     * @param key
//     * @return 是否删除成功
//     */
//    public static boolean remove(String key) {
//        SettingModel settingModel = SQLite.select()
//                .from(SettingModel.class)
//                .where(SettingModel_Table.key.eq(key))
//                .querySingle();
//
//        if (settingModel != null) {
//            return settingModel.delete();
//        }
//        return false;
//    }
//
//    /**
//     * 慎用慎用慎用慎用慎用，清除当前整个设置表的数据
//     * 我故意把此API 标记为弃用，就是为了使用错时提醒
//     */
//    @Deprecated
//    public static void clear() {
//        SQLite.delete(SettingModel.class).execute();
//    }
//
//}
