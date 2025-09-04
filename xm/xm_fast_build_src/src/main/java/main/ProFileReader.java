package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProFileReader {

//# 详情页
//  plugin_jar_name=recommend.jar
//  plugin_dir=D:\AndroidCode\develop_xmkw\plugins\app_info
//
//# modulation
//# plugin_jar_name=com.qihoo.plugin.modulation.jar
//# plugin_dir=D:\AndroidCode\develop_xmkw\plugins\app_info
//
//# 搜索
//# plugin_jar_name=search.jar
//# plugin_dir=D:\AndroidCode\develop_xmkw\plugins\search


    public static Map<String, String> readProFile(String filePath) {
        Map<String, String> configMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    int index = line.indexOf('=');
                    if (index != -1) {
                        String key = line.substring(0, index).trim();
                        String value = line.substring(index + 1).trim();
                        configMap.put(key, value);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return configMap;
    }
}