package httpwork;

/**
 * Created by chenglin on 2017-6-27.
 */
public interface HttpCallback {
    void onSuccess(String result);

    void onFailure(Exception e);
}
