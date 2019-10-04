package cheerly.mybaseproject.listener;

/**
 * Created by chenglin on 2017-5-24.
 */
public interface OnCompressBitmapListener {
    void onPrepare();

    void onSucceed(Object object);

    void onError();
}