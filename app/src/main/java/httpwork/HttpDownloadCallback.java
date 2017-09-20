package httpwork;

import java.io.IOException;

/**
 * Created by chenglin on 2017-6-27.
 */
public interface HttpDownloadCallback {
    void onFailure(IOException e);

    void onSuccess();

    void onStart();

    /**
     * @param fileTotalSize   文件总大小
     * @param fileDowningSize 文件已经下载的大小
     * @param percent         文件下载的进度百分比
     */
    void onProgress(long fileTotalSize, long fileDowningSize, int percent);
}
