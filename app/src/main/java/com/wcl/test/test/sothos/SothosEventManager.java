package com.wcl.test.test.sothos;

import com.wcl.test.base.BaseApp;

import org.json.JSONObject;

import java.util.Map;

public class SothosEventManager {
    private SothosRepository mSothosRepository = null;
    private SothosTimer mSothosTimer = null;

    private final Runnable uploadDataTask = () -> {
//        SothosHttpUploader.doGet()
    };

    private SothosEventManager() {
        mSothosRepository = new SothosRepository(BaseApp.getApp());
        mSothosTimer = new SothosTimer();
    }

    private static final class InstanceHolder {
        private static final SothosEventManager INSTANCE = new SothosEventManager();
    }

    public static SothosEventManager getInstance() {
        return InstanceHolder.INSTANCE;
    }


    public void onEvent(String eventId, Map<String, String> params) {
        try {
            mSothosTimer.start(uploadDataTask);
            String json = toJson(eventId, params);
            mSothosRepository.insertEvent(json);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    private String toJson(String eventId, Map<String, String> params) {
        String json = "";
        try {
            JSONObject jo = new JSONObject();
            JSONObject joMap = new JSONObject();
            if (params != null) {
                for (String key : params.keySet()) {
                    joMap.put(key, params.get(key));
                }
            }
            jo.put(eventId, joMap);
            json = jo.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }
}
