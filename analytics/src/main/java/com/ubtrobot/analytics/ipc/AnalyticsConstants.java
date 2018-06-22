package com.ubtrobot.analytics.ipc;

public class AnalyticsConstants {

    public static final String PROVIDER_URI = "content://com.ubtrobot.analytics.ipc.provider.AnalyticsProvider";

    public static final String KEY_PROVIDER_PONG = "pong";
    public static final String KEY_STRATEGY = "strategy";
    public static final String KEY_ENABLE = "enable";
    public static final String KEY_RECORD_EVENT = "recordEvent";

    public static final String CALL_METHOD_PING = "ping";
    public static final String CALL_METHOD_SET_STRATEGY = "setStrategy";
    public static final String CALL_METHOD_GET_STRATEGY = "getStrategy";
    public static final String CALL_METHOD_ENABLE = "enable";
    public static final String CALL_METHOD_RECORD_EVENT = "recordEvent";
    public static final String CALL_METHOD_SHUTDOWN = "shutdown";

    public static final int ID_MAX_LENGTH = 64;
    public static final String CUSTOM_EVENT = "custom_event";
    public static final String SHUTDOWN_EVENT_ID = "shutdown_event_id";
    public static final String SHUTDOWN_EVENT = "shutdown_event";

    public static final String EVENT_TYPE_ACTIVITY = "activity";                // 页面的类型：activity
    public static final String EVENT_TYPE_FRAGMENT = "fragment";                // 页面类型：fragment

    public static final String EVENT_ID_DEF_ACTIVITY_START = "activityStart";   // activity启动时，默认的eventId
    public static final String EVENT_ID_DEF_ACTIVITY_STOP = "activityStop";     // activity退出时，默认的eventId
    public static final String EVENT_ID_DEF_FRAGMENT_START = "fragmentStart";   // fragment展示时，默认的eventId
    public static final String EVENT_ID_DEF_FRAGMENT_STOP = "fragmentStop";     // fragment退出前台时，默认的eventId

}
