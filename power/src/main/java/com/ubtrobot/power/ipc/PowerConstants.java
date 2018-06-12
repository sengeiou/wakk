package com.ubtrobot.power.ipc;

public class PowerConstants {

    private PowerConstants() {
    }

    public static final String SERVICE_NAME = "power";

    public static final String CALL_PATH_SLEEP = "/power/sleep";
    public static final String CALL_PATH_QUERY_IS_SLEEPING = "/power/query-is-sleeping";
    public static final String CALL_PATH_WAKE_UP = "/power/wake-up";
    public static final String CALL_PATH_SHUTDOWN = "/power/shutdown";
    public static final String CALL_PATH_GET_BATTERY_PROPERTIES = "/power/battery-properties";

    public static final String ACTION_BATTERY_CHANGE = "event.action.BATTERY_CHANGE";
}
