package com.ubtrobot.upgrade.ipc;

public class UpgradeConstants {

    private UpgradeConstants() {
    }

    public static final String SERVICE_NAME = "upgrade";

    public static final String CALL_PATH_GET_FIRMWARE_LIST = "/firmware/list";
    public static final String CALL_PATH_DETECT_UPGRADE = "/firmware/upgrade/detect";
}