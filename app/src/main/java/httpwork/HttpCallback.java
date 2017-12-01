package httpwork;

/**
 * Created by chenglin on 2017-6-27.
 */
public interface HttpCallback {
    void onFailure(Exception e);

    void onSuccess(String result);
}
