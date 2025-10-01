package com.wcl.test.storage;

import android.os.Build;
import android.text.TextUtils;

import com.wcl.test.base.BaseApp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class BigStringFile implements BigStringBase {
    private String mCachedPath = null;

    private static final class InstanceHolder {
        private static final BigStringFile INSTANCE = new BigStringFile("big_files");
    }

    public static BigStringFile getInstance() {
        return BigStringFile.InstanceHolder.INSTANCE;
    }

    private BigStringFile() {
    }

    private BigStringFile(String dir_name) {
        if (TextUtils.isEmpty(dir_name)) {
            throw new NullPointerException("BigStringFile() dir_name is null");
        }
        String externalPath = getExternalPath();
        mCachedPath = new File(externalPath, dir_name).getAbsolutePath();

        File folder = new File(mCachedPath);
        if (!folder.exists() && !folder.mkdirs()) {
            throw new IllegalStateException("Failed to create directory: " + mCachedPath);
        }
    }

    @Override
    public List<String> getAllKeys() {
        List<String> keys = new ArrayList<>();
        File folder = new File(mCachedPath);
        if (!folder.exists() || !folder.isDirectory()) {
            return keys;
        }
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                keys.add(file.getName());
            }
        }
        return keys;
    }

    @Override
    public boolean put(String key, String value) {
        if (TextUtils.isEmpty(key)) {
            return false;
        }

        File folder = new File(mCachedPath);
        if (!folder.exists() && !folder.mkdirs()) {
            return false;
        }

        String filePath = new File(mCachedPath, key).getAbsolutePath();
        writeFile(filePath, value);
        return true;
    }

    @Override
    public String get(String key) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }

        String file_path = new File(mCachedPath, key).getAbsolutePath();
        return readFileString(file_path);
    }

    @Override
    public boolean putValues(List<String> keys, List<String> values) {
        if (keys == null || keys.isEmpty()) {
            return false;
        } else if (values == null || values.isEmpty()) {
            return false;
        } else if (keys.size() != values.size()) {
            throw new IllegalArgumentException("putValues() method keys.size() != values.size()");
        }

        for (int i = 0; i < keys.size(); i++) {
            put(keys.get(i), values.get(i));
        }
        return true;
    }

    @Override
    public List<String> getValues(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return null;
        }

        List<String> contents = new ArrayList<>();
        for (String key : keys) {
            String content = get(key);
            contents.add(content);
        }

        return contents;
    }

    @Override
    public boolean remove(String key) {
        if (TextUtils.isEmpty(key)) {
            return false;
        }

        String file_path = new File(mCachedPath, key).getAbsolutePath();
        return new File(file_path).delete();
    }

    @Override
    public boolean remove(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return false;
        }
        for (String key : keys) {
            remove(key);
        }
        return true;
    }

    private static String readFileString(String file_path) {
        File file = new File(file_path);
        if (TextUtils.isEmpty(file_path) || !file.exists()) {
            return null;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Path path = Paths.get(file_path);
                byte[] bytes = Files.readAllBytes(path);
                return new String(bytes, StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            long length = file.length();
            StringBuilder contentBuilder = new StringBuilder((int) length);
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    contentBuilder.append(line).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return contentBuilder.toString();
        }
        return null;
    }

    private static String getExternalPath() {
        BaseApp app = BaseApp.getApp();
        if (app == null) {
            throw new IllegalStateException("Application context is null");
        }

        File file = app.getExternalFilesDir(null);
        if (file == null) {
            file = app.getFilesDir();
        }
        return file.getAbsolutePath();
    }

    private static void writeFile(String file_path, String text) {
        File file = new File(file_path);
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            return;
        }

        try (FileWriter fileWriter = new FileWriter(file, false)) {
            fileWriter.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
