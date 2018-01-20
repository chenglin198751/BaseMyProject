package httpwork;

import java.io.IOException;

import okhttp3.Call;

/**
 * Created by chenglin on 2017-6-27.
 */
public interface HttpDownloadCallback {
    void onSuccess(String filePath);

    /**
     * @param call            A call is a request that has been prepared for execution. A call can be canceled
     * @param fileTotalSize   文件总大小
     * @param fileDowningSize 文件已经下载的大小
     * @param percent         文件下载的进度百分比
     */
    void onProgress(Call call, long fileTotalSize, long fileDowningSize, int percent);

    void onFailure(IOException e);
}
