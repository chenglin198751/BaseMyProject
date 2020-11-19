package cheerly.mybaseproject.listener;

/**
 * Created by chenglin on 2017-5-24.
 */
public interface OnCompressBitmapListener<T> {
    void onPrepare();

    void onSucceed(T t);

    void onError();
}