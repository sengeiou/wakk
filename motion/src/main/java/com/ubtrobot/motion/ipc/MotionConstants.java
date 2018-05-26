package com.ubtrobot.motion.ipc;

public class MotionConstants {

    private MotionConstants() {
    }

    public static final String SERVICE_NAME = "motion";

    public static final String COMPETING_ITEM_PREFIX_JOINT = "joint-";
    public static final String COMPETING_ITEM_SCRIPT_EXECUTOR = "motion-script-executor";

    public static final String CALL_PATH_GET_JOINT_LIST = "/joint/device/list";
    public static final String CALL_PATH_QUERY_JOINT_IS_ROTATING = "/joint/query-is-rotating";
    public static final String CALL_PATH_GET_JOINT_ANGLE = "/joint/angle";
    public static final String CALL_PATH_JOINT_ROTATE = "/joint/rotate";
    public static final String CALL_PATH_EXECUTE_MOTION_SCRIPT = "/motion/script/execute";

    public static final String CALL_PATH_GET_LOCOMOTOR = "/locomotor/device";
}