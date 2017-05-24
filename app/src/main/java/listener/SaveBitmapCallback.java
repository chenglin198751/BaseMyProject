package listener;

/**
 * Created by chenglin on 2017-5-24.
 */

public interface SaveBitmapCallback {
    void onPrepare();

    void onSucceed(String path);

    void onError();
}