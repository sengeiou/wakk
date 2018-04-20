package com.ubtrobot.light.ipc;

public class LightConstants {

    private LightConstants() {
    }

    public static final String SERVICE_NAME = "light";

    public static final String COMPETING_ITEM_PREFIX_LIGHT = "light-";

    public static final String CALL_PATH_GET_LIGHT_LIST = "/light/device/list";
    public static final String CALL_PATH_TURN_ON = "/light/turn-on";
    public static final String CALL_PATH_TURN_OFF = "/light/turn-off";
    public static final String CALL_PATH_GET_IS_TURN_ON = "/light/query-is-turn-on";
    public static final String CALL_PATH_GET_COLOR = "/light/color";
    public static final String CALL_PATH_CHANGE_COLOR = "/light/color/change";
    public static final String CALL_PATH_GET_LIGHTING_EFFECT_LIST = "/light/effect/list";
    public static final String CALL_PATH_DISPLAY_LIGHTING_EFFECT = "/light/effect/display";
}