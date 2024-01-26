package com.wcl.test.base;

import android.content.Context;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonToken;
import com.hjq.gson.factory.GsonFactory;
import com.hjq.gson.factory.ParseExceptionCallback;
import com.wcl.test.utils.AppLogUtils;

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
        GsonFactory.setParseExceptionCallback(new ParseExceptionCallback() {
            @Override
            public void onParseObjectException(TypeToken<?> typeToken, String fieldName, JsonToken jsonToken) {
                AppLogUtils.e("GsonFactory", "onParseObjectException:类型解析异常：" + typeToken + "#" + fieldName + "，后台返回的类型为：" + jsonToken);
            }

            @Override
            public void onParseListItemException(TypeToken<?> typeToken, String fieldName, JsonToken jsonToken) {
                AppLogUtils.e("GsonFactory", "onParseListItemException:类型解析异常：" + typeToken + "#" + fieldName + "，后台返回的类型为：" + jsonToken);
            }

            @Override
            public void onParseMapItemException(TypeToken<?> typeToken, String fieldName, String mapItemKey, JsonToken jsonToken) {
                AppLogUtils.e("GsonFactory", "onParseMapItemException:类型解析异常：" + typeToken + "#" + fieldName + "，后台返回的类型为：" + jsonToken);
            }
        });
    }
}
