package com.wcl.test.storage;

import java.util.List;


public interface BigStringBase {
    List<String> getAllKeys();

    boolean put(String key, String value);

    String get(String key);

    boolean putValues(List<String> keys, List<String> values);

    List<String> getValues(List<String> keys);

    boolean remove(String key);

    boolean remove(List<String> keys);
}
