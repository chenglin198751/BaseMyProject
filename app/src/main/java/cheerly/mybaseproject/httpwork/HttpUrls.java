package cheerly.mybaseproject.httpwork;

import cheerly.mybaseproject.BuildConfig;

/**
 * Created by chenglin on 2018-1-25.
 */

public class HttpUrls {
    public static final String BASE_URL = BuildConfig.ENV_DEBUG ? "https://debug" : "https://release";
    public static final String check_update = BASE_URL + "/check_update";
}
