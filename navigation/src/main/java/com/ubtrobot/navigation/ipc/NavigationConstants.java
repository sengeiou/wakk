package com.ubtrobot.navigation.ipc;

public class NavigationConstants {

    private NavigationConstants() {
    }

    public static final String SERVICE_NAME = "navigation";

    public static final String COMPETING_ITEM_NAVIGATOR = "navigator";

    public static final String CALL_PATH_GET_NAV_MAP_LIST = "/navigation/map/list";
    public static final String CALL_PATH_GET_NAV_MAP = "/navigation/map";
    public static final String CALL_PATH_GET_SELECTED_NAV_MAP = "/navigation/map/selected";
    public static final String CALL_PATH_ADD_NAV_MAP = "/navigation/map/add";
    public static final String CALL_PATH_SELECT_NAV_MAP = "/navigation/map/select";
    public static final String CALL_PATH_MODIFY_NAV_MAP = "/navigation/map/modify";
    public static final String CALL_PATH_REMOVE_NAV_MAP = "/navigation/map/remove";

    public static final String CALL_PATH_GET_CURRENT_LOCATION = "/navigation/location/current";
    public static final String CALL_PATH_LOCATE_SELF = "/navigation/locate/self";
    public static final String CALL_PATH_QUERY_LOCATING = "/navigation/locating";
    public static final String CALL_PATH_NAVIGATE = "/navigation/navigate";
    public static final String CALL_PATH_QUERY_NAVIGATING = "/navigation/navigating";
}