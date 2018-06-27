package com.ubtrobot.diagnosis.ipc;

public class DiagnosisConstants {

    private DiagnosisConstants() {
    }

    public static final String SERVICE_NAME = "diagnosis";

    public static final String CALL_PATH_GET_PART_LIST = "/diagnosis/part/list";
    public static final String CALL_PATH_GET_DIAGNOSIS_LIST = "/diagnosis/list";

    public static final String ACTION_DIAGNOSE_PREFIX = "event.action.DIAGNOSE";
}