package com.wcl.test.base;

public class EventAction {

    public interface System {
        /**
         * 根据开关onKeepSingleActivity()：当前Activity无论打开多少，只保留最后打开的一个
         */
        String ACTION_KEEP_SINGLE_ACTIVITY = "ACTION_SYS_KEEP_SINGLE_ACTIVITY";
        /**
         * 关闭别的Activity，只保留MainActivity不关闭
         */
        String ACTION_KEEP_MAIN_AND_CLOSE_ACTIVITY = "ACTION_SYS_KEEP_MAIN_AND_CLOSE_ACTIVITY";
    }

    public interface App {
        String action_test = "ACTION_TEST";
    }
}
