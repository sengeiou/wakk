package com.ubtrobot.navigation;

import com.google.protobuf.StringValue;
import com.ubtrobot.async.Promise;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.navigation.ipc.NavigationConstants;
import com.ubtrobot.navigation.ipc.NavigationConverters;
import com.ubtrobot.navigation.ipc.NavigationProto;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.util.List;

public class NavMapList {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("NavMapList");

    private final ProtoCallAdapter mNavigationService;

    public NavMapList(ProtoCallAdapter navigationService) {
        mNavigationService = navigationService;
    }

    public Promise<List<NavMap>, NavMapException> all() {
        return mNavigationService.call(
                NavigationConstants.CALL_PATH_GET_NAV_MAP_LIST,
                new ProtoCallAdapter.DFProtoConverter<
                        List<NavMap>, NavigationProto.NavMapList, NavMapException>() {
                    @Override
                    public Class<NavigationProto.NavMapList> doneProtoClass() {
                        return NavigationProto.NavMapList.class;
                    }

                    @Override
                    public List<NavMap> convertDone(NavigationProto.NavMapList navMapList) {
                        return NavigationConverters.toNavMapListPojo(navMapList);
                    }

                    @Override
                    public NavMapException convertFail(CallException e) {
                        return new NavMapException.Factory().from(e);
                    }
                }
        );
    }

    public Promise<NavMap, NavMapException> get(String mapId) {
        return mNavigationService.call(
                NavigationConstants.CALL_PATH_GET_NAV_MAP,
                StringValue.newBuilder().setValue(mapId).build(),
                new NavMapConverter()
        );
    }

    public Promise<NavMap, NavMapException> add(NavMap map) {
        return mNavigationService.call(
                NavigationConstants.CALL_PATH_ADD_NAV_MAP,
                NavigationConverters.toNavMapProto(map),
                new NavMapConverter()
        );
    }

    public Promise<NavMap, NavMapException> getSelected() {
        return mNavigationService.call(
                NavigationConstants.CALL_PATH_GET_SELECTED_NAV_MAP,
                new NavMapConverter()
        );
    }

    public Promise<NavMap, NavMapException> select(String mapId) {
        return mNavigationService.call(
                NavigationConstants.CALL_PATH_SELECT_NAV_MAP,
                StringValue.newBuilder().setValue(mapId).build(),
                new NavMapConverter()
        );
    }

    public Promise<NavMap, NavMapException> modify(NavMap map) {
        return mNavigationService.call(
                NavigationConstants.CALL_PATH_MODIFY_NAV_MAP,
                NavigationConverters.toNavMapProto(map),
                new NavMapConverter()
        );
    }

    public Promise<NavMap, NavMapException> remove(String mapId) {
        return mNavigationService.call(
                NavigationConstants.CALL_PATH_REMOVE_NAV_MAP,
                StringValue.newBuilder().setValue(mapId).build(),
                new NavMapConverter()
        );
    }

    private static class NavMapConverter
            implements ProtoCallAdapter.DFProtoConverter<
            NavMap, NavigationProto.NavMap, NavMapException> {

        @Override
        public Class<NavigationProto.NavMap> doneProtoClass() {
            return NavigationProto.NavMap.class;
        }

        @Override
        public NavMap convertDone(NavigationProto.NavMap NavMap) {
            return NavigationConverters.toNavMapPojo(NavMap);
        }

        @Override
        public NavMapException convertFail(CallException e) {
            return new NavMapException.Factory().from(e);
        }
    }
}