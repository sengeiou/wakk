package com.ubtrobot.power.ipc;

public class PowerConstants {

    private PowerConstants() {
    }

    public static final String SERVICE_NAME = "power";

    public static final String COMPETING_ITEM_CHARGING_STATTION_CONNECTION = "charging-station-connection";

    public static final String CALL_PATH_SLEEP = "/power/sleep";
    public static final String CALL_PATH_QUERY_IS_SLEEPING = "/power/query-is-sleeping";
    public static final String CALL_PATH_WAKE_UP = "/power/wake-up";
    public static final String CALL_PATH_SHUTDOWN = "/power/shutdown";
    public static final String CALL_PATH_GET_BATTERY_PROPERTIES = "/power/battery-properties";
    public static final String CALL_PATH_CONNECT_TO_CHARGING_STATION = "/power/charging-station/connect";
    public static final String CALL_PATH_DISCONNECT_FROM_CHARGING_STATION = "/power/charging-station/disconnect";
    public static final String CALL_PATH_QUERY_CONNECTED_TO_CHARGING_STATTION = "/power/charging-station/query-is-connected";

    public static final String ACTION_BATTERY_CHANGE = "event.action.BATTERY_CHANGE";
}
