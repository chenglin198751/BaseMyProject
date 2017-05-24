package listener;

/**
 * Created by chenglin on 2017-5-24.
 */
public interface MyCallback {
    void onPrepare();

    void onSucceed(Object object);

    void onError();
}