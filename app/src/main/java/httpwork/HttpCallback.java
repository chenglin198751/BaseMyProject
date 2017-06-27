package httpwork;

import java.io.IOException;

/**
 * Created by chenglin on 2017-6-27.
 */
public interface HttpCallback {
    void onFailure(IOException e);

    void onSuccess(String result);
}
