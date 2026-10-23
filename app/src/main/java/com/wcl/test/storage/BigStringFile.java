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

public class BigStringFile implements IBigString {
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
        if ((!folder.exists() && !folder.mkdirs()) || !folder.isDirectory()) {
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
                if (file.isFile()) {
                    keys.add(file.getName());
                }
            }
        }
        return keys;
    }

    @Override
    public boolean put(String key, String value) {
        File file = getFileForKey(key);
        if (file == null || value == null) {
            return false;
        }

        File folder = new File(mCachedPath);
        if (!folder.exists() && !folder.mkdirs()) {
            return false;
        }

        return writeFile(file, value);
    }

    @Override
    public String get(String key) {
        File file = getFileForKey(key);
        if (file == null) {
            return null;
        }

        return readFileString(file.getAbsolutePath());
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
            if (getFileForKey(keys.get(i)) == null || values.get(i) == null) {
                return false;
            }
        }

        boolean success = true;
        for (int i = 0; i < keys.size(); i++) {
            if (!put(keys.get(i), values.get(i))) {
                success = false;
            }
        }
        return success;
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
        File file = getFileForKey(key);
        if (file == null) {
            return false;
        }

        return file.delete();
    }

    @Override
    public boolean remove(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return false;
        }

        for (String key : keys) {
            if (getFileForKey(key) == null) {
                return false;
            }
        }

        boolean success = true;
        for (String key : keys) {
            if (!remove(key)) {
                success = false;
            }
        }
        return success;
    }

    private File getFileForKey(String key) {
        if (TextUtils.isEmpty(key)
                || ".".equals(key)
                || "..".equals(key)
                || key.indexOf('/') >= 0
                || key.indexOf('\\') >= 0) {
            return null;
        }
        return new File(mCachedPath, key);
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

    private static boolean writeFile(File file, String text) {
        File parent = file.getParentFile();
        if (parent == null || (!parent.exists() && !parent.mkdirs()) || !parent.isDirectory()) {
            return false;
        }

        try (FileWriter fileWriter = new FileWriter(file, false)) {
            fileWriter.write(text);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
