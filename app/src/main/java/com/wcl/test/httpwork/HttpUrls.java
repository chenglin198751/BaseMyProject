package com.wcl.test.httpwork;

import com.wcl.test.EnvToggle;

/**
 * Created by chenglin on 2018-1-25.
 */

public class HttpUrls {
    public static final String BASE_URL = EnvToggle.isDebug() ? "https://debug" : "https://release";
    public static final String check_update = BASE_URL + "/check_update";
}
