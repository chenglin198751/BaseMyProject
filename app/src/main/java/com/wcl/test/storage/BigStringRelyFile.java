package com.wcl.test.storage;

import android.text.TextUtils;

import com.wcl.test.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class BigStringRelyFile {
    private static final String TAG = "BigStringRelyFile";

    private static String getKeyFilePath(String key) {
        String dir = FileUtils.getExternalPath() + "/string";
        new File(dir).mkdirs();
        return dir + "/" + key;
    }

    public static List<String> getAllKeys() {
        String dir = FileUtils.getExternalPath() + "/string";
        File[] files = new File(dir).listFiles();
        List<String> keys = new ArrayList<>();

        if (files != null && files.length > 0) {
            for (File file : files) {
                keys.add(file.getName());
            }
        }
        return keys;
    }

    public static void put(String key, String value) {
        if (TextUtils.isEmpty(key)) {
            return;
        }

        String file_path = getKeyFilePath(key);
        File file = new File(file_path);
        try {
            file.delete();
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileUtils.writeFile(file_path, value);
    }

    public static String get(String key) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }

        String file_path = getKeyFilePath(key);
        return FileUtils.readFile(file_path);
    }

    public static void putValues(List<String> keys, List<String> values) {
        if (keys == null || keys.size() == 0) {
            return;
        } else if (values == null || values.size() == 0) {
            return;
        } else if (keys.size() != values.size()) {
            throw new IllegalArgumentException("keys.size() != values.size()");
        }

        for (int i = 0; i < keys.size(); i++) {
            put(keys.get(i), values.get(i));
        }
    }

    public static List<String> getValues(List<String> keys) {
        if (keys == null || keys.size() == 0) {
            return null;
        }

        List<String> contents = new ArrayList<>();
        for (int i = 0; i < keys.size(); i++) {
            String content = get(keys.get(i));
            contents.add(content);
        }

        return contents;
    }

    public static void remove(String key) {
        if (TextUtils.isEmpty(key)) {
            return;
        }

        String file_path = getKeyFilePath(key);
        new File(file_path).delete();
    }

    public static void remove(List<String> keys) {
        if (keys == null || keys.size() == 0) {
            return;
        }

        for (String key : keys) {
            remove(key);
        }
    }
}
