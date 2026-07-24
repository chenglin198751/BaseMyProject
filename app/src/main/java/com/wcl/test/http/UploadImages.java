package com.wcl.test.http;

import com.wcl.test.utils.AppLogUtils;
import com.wcl.test.utils.AppThreadPoolExecutor;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class UploadImages {
    private static String TAG = "UploadImages";

    static void exe(String url,
                    Map<String, Object> params,
                    String fileKey,
                    List<File> files,
                    OkHttpExecutor.UploadCallback callback) {
        if (LiteHelper.invalidUrl(url) || files == null || files.isEmpty()) {
            LiteHelper.notifyUploadFailure(callback, "无效的 URL 或文件列表为空");
            return;
        }
        try {
            AppThreadPoolExecutor.getExecutor().execute(() -> {
            Map<String, Object> finalParams = HttpRequestHelper.withCommonParams(params);
            int totalCount = files.size();
            for (int i = 0; i < totalCount; i++) {
                File file = files.get(i);
                if (file == null || !file.exists()) {
                    LiteHelper.postToUi(() -> {
                        if (callback != null) {
                            callback.onFinished(false, "文件不存在: " + file);
                        }
                    });
                    return;
                }
                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                for (Map.Entry<String, Object> entry : finalParams.entrySet()) {
                    builder.addFormDataPart(entry.getKey(), String.valueOf(entry.getValue()));
                }
                builder.addFormDataPart(fileKey, file.getName(),
                        RequestBody.create(LiteHelper.guessMediaType(file), file));
                Request request;
                try {
                    request = new Request.Builder().url(url).post(builder.build()).build();
                } catch (IllegalArgumentException e) {
                    String error = e.toString();
                    LiteHelper.postToUi(() -> {
                        if (callback != null) {
                            callback.onFinished(false, error);
                        }
                    });
                    return;
                }
                try (Response response = HttpRequestHelper.CLIENT.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        String error = "上传失败: " + response.code();
                        LiteHelper.postToUi(() -> {
                            if (callback != null) {
                                callback.onFinished(false, error);
                            }
                        });
                        return;
                    }
                    final int index = i;
                    LiteHelper.postToUi(() -> {
                        if (callback != null) {
                            callback.onProgress(index, totalCount);
                        }
                    });
                } catch (IOException e) {
                    AppLogUtils.w(TAG, "uploadImages error: " + e);
                    String error = e.toString();
                    LiteHelper.postToUi(() -> {
                        if (callback != null) {
                            callback.onFinished(false, error);
                        }
                    });
                    return;
                }
            }
            LiteHelper.postToUi(() -> {
                if (callback != null) {
                    callback.onFinished(true, null);
                }
            });
            });
        } catch (java.util.concurrent.RejectedExecutionException e) {
            LiteHelper.notifyUploadFailure(callback, e.toString());
        }
    }
}
