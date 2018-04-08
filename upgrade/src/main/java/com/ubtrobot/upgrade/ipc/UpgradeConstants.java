package com.ubtrobot.upgrade.ipc;

public class UpgradeConstants {

    private UpgradeConstants() {
    }

    public static final String SERVICE_NAME = "upgrade";

    public static final String CALL_PATH_GET_FIRMWARE_LIST = "/firmware/list";
    public static final String CALL_PATH_DETECT_UPGRADE = "/firmware/upgrade/detect";
    public static final String CALL_PATH_GET_DOWNLOADING_FIRMWARE_PACKAGE_GROUP =
            "/firmware/upgrade/group/downloading";
    public static final String CALL_PATH_GET_FIRMWARE_PACKAGE_DOWNLOAD_STATE =
            "/firmware/upgrade/group/download/state";
    public static final String CALL_PATH_GET_FIRMWARE_PACKAGE_DOWNLOAD_PROGRESS =
            "/firmware/upgrade/group/download/progress";

    public static final String ACTION_FIRMWARE_DOWNLOAD_STATE_CHANGE =
            "event.action.FIRMWARE_DOWNLOAD_STATE_CHANGE";
    public static final String ACTION_FIRMWARE_DOWNLOAD_PROGRESS_CHANGE =
            "event.action.FIRMWARE_DOWNLOAD_PROGRESS_CHANGE";
}