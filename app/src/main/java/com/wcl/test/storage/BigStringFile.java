package com.wcl.test.storage;

import android.os.Build;
import android.text.TextUtils;

import com.wcl.test.base.BaseApp;

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
    private BigStringFile() {
    }

    private static final class InstanceHolder {
        static final BigStringFile INSTANCE = new BigStringFile();
    }

    public static BigStringFile get() {
        return BigStringFile.InstanceHolder.INSTANCE;
    }

    @Override
    public List<String> getAllKeys() {
        String dir = getExternalPath() + "/string";
        File[] files = new File(dir).listFiles();
        List<String> keys = new ArrayList<>();

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

        String file_path = getKeyFilePath(key);
        File file = new File(file_path);
        try {
            file.delete();
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        writeFile(file_path, value);
        return true;
    }

    @Override
    public String get(String key) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }

        String file_path = getKeyFilePath(key);
        return readFileString(file_path);
    }

    @Override
    public boolean putValues(List<String> keys, List<String> values) {
        if (keys == null || keys.size() == 0) {
            return false;
        } else if (values == null || values.size() == 0) {
            return false;
        } else if (keys.size() != values.size()) {
            throw new IllegalArgumentException("keys.size() != values.size()");
        }

        for (int i = 0; i < keys.size(); i++) {
            put(keys.get(i), values.get(i));
        }
        return true;
    }

    @Override
    public List<String> getValues(List<String> keys) {
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

    @Override
    public boolean remove(String key) {
        if (TextUtils.isEmpty(key)) {
            return false;
        }

        String file_path = getKeyFilePath(key);
        return new File(file_path).delete();
    }

    @Override
    public boolean remove(List<String> keys) {
        if (keys == null || keys.size() == 0) {
            return false;
        }
        for (String key : keys) {
            remove(key);
        }
        return true;
    }


    private static String getKeyFilePath(String key) {
        String dir = getExternalPath() + "/string";
        new File(dir).mkdirs();
        return dir + "/" + key;
    }

    private static String readFileString(String file_path) {
        if (TextUtils.isEmpty(file_path) || !new File(file_path).exists()) {
            return null;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Path path = Paths.get(file_path);
                return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            } else {
                StringBuilder line = new StringBuilder();
                FileReader reader = new FileReader(file_path);
                int character;
                while ((character = reader.read()) != -1) {
                    line.append((char) character);
                }
                reader.close();
                return line.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getExternalPath() {
        File file = BaseApp.getApp().getExternalFilesDir("");
        if (file == null) {
            file = BaseApp.getApp().getFilesDir();
        }
        return file.getAbsolutePath();
    }

    private static void writeFile(String file_path, String text) {
        if (TextUtils.isEmpty(file_path) || !new File(file_path).exists()) {
            return;
        }

        try {
            FileWriter fileWriter = new FileWriter(file_path, false);
            fileWriter.write(text);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
