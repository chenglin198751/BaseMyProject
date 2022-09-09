package com.wcl.test.base;

import android.content.Context;
import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonToken;
import com.hjq.gson.factory.GsonFactory;
import com.hjq.gson.factory.JsonCallback;
import com.wcl.test.utils.LogUtils;

/**
 * Created by weichenglin  on 2018/7/13
 */
public class MainApp extends BaseApp {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 设置 Json 解析容错监听
        GsonFactory.setJsonCallback(new JsonCallback() {

            @Override
            public void onTypeException(TypeToken<?> typeToken, String fieldName, JsonToken jsonToken) {
                LogUtils.w("GsonFactory", "类型解析异常：" + typeToken + "#" + fieldName + "，后台返回的类型为：" + jsonToken);
            }
        });
    }
}
