package com.wcl.test.main.sothos;

import com.wcl.test.base.BaseApp;

import org.json.JSONObject;

import java.util.Map;

public class SothosEventManager {
    private SothosRepository mSothosRepository = null;

    private SothosEventManager() {
        mSothosRepository = new SothosRepository(BaseApp.getApp());
    }

    private static final class InstanceHolder {
        private static final SothosEventManager INSTANCE = new SothosEventManager();
    }

    public static SothosEventManager getInstance() {
        return InstanceHolder.INSTANCE;
    }


    public void onEvent(String eventId, Map<String, String> params) {
        try {
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
