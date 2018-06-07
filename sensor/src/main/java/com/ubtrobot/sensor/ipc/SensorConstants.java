package com.ubtrobot.sensor.ipc;

public class SensorConstants {

    private SensorConstants() {
    }

    public static final String SERVICE_NAME = "sensor";

    public static final String CALL_PATH_GET_SENSOR_LIST = "/sensor/device/list";

    public static final String ACTION_SENSOR_CHANGE = "event.action.SENSOR_CHANGE";
}
