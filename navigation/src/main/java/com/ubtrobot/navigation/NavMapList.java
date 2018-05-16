package com.ubtrobot.navigation;

import android.util.Pair;

import com.google.protobuf.StringValue;
import com.ubtrobot.async.Promise;
import com.ubtrobot.cache.CachedField;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.navigation.ipc.NavigationConstants;
import com.ubtrobot.navigation.ipc.NavigationConverters;
import com.ubtrobot.navigation.ipc.NavigationProto;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.util.Collections;
import java.util.List;

public class NavMapList {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("NavMapList");

    private final ProtoCallAdapter mNavigationService;

    private final CachedField<Pair<List<NavMap>, String>> mNavMapList;

    public NavMapList(ProtoCallAdapter navigationService) {
        mNavigationService = navigationService;
        mNavMapList = new CachedField<>(new CachedField.FieldGetter<Pair<List<NavMap>, String>>() {
            @Override
            public Pair<List<NavMap>, String> get() {
                try {
                    NavigationProto.NavMapList navMapList = mNavigationService.syncCall(
                            NavigationConstants.CALL_PATH_GET_NAV_MAP_LIST,
                            NavigationProto.NavMapList.class
                    );

                    Pair<List<NavMap>, String> pair = NavigationConverters.
                            toNavMapListPojo(navMapList);
                    return new Pair<>(Collections.unmodifiableList(pair.first), pair.second);
                } catch (CallException e) {
                    LOGGER.e(e, "Framework error when getting the navigation map list.");
                    return new Pair<>(Collections.<NavMap>emptyList(), "");
                }
            }
        });
    }

    public List<NavMap> all() {
        //noinspection ConstantConditions
        return mNavMapList.get().first;
    }

    public NavMap get(String mapId) {
        for (NavMap navMap : all()) {
            if (navMap.getId().equals(mapId)) {
                return navMap;
            }
        }

        throw new NavMapNotFoundException();
    }

    public Promise<NavMap, NavMapException> add(NavMap map) {
        return mNavigationService.call(
                NavigationConstants.CALL_PATH_ADD_NAV_MAP,
                NavigationConverters.toNavMapProto(map),
                new NavMapConverter()
        );
    }

    public NavMap getSelected() {
        //noinspection ConstantConditions
        return get(mNavMapList.get().second);
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

    public static class NavMapNotFoundException extends RuntimeException {
    }

    private static class NavMapConverter
            implements ProtoCallAdapter.DFProtoConverter<
            NavMap,NavigationProto.NavMap,NavMapException> {

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