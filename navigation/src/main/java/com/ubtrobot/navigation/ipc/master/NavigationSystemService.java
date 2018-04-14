package com.ubtrobot.navigation.ipc.master;

import android.app.Application;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Pair;

import com.google.protobuf.Message;
import com.ubtrobot.async.Promise;
import com.ubtrobot.master.adapter.CallProcessAdapter;
import com.ubtrobot.master.adapter.ProtoCallProcessAdapter;
import com.ubtrobot.master.adapter.ProtoParamParser;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.service.MasterSystemService;
import com.ubtrobot.navigation.NavMap;
import com.ubtrobot.navigation.NavMapException;
import com.ubtrobot.navigation.ipc.NavigationConstants;
import com.ubtrobot.navigation.ipc.NavigationConverters;
import com.ubtrobot.navigation.ipc.NavigationProto;
import com.ubtrobot.navigation.sal.AbstractNavigationService;
import com.ubtrobot.navigation.sal.NavigationFactory;
import com.ubtrobot.navigation.sal.NavigationService;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;

import java.util.List;

public class NavigationSystemService extends MasterSystemService {

    private NavigationService mService;
    private ProtoCallProcessAdapter mCallProcessor;

    @Override
    protected void onServiceCreate() {
        Application application = getApplication();
        if (!(application instanceof NavigationFactory)) {
            throw new IllegalStateException(
                    "Your application should implement NavigationFactory interface.");
        }

        mService = ((NavigationFactory) application).createNavigationService();
        if (mService == null || !(mService instanceof AbstractNavigationService)) {
            throw new IllegalStateException("Your application 's createNavigationService returns " +
                    "null or does not return a instance of AbstractNavigationService.");
        }

        mCallProcessor = new ProtoCallProcessAdapter(new Handler(getMainLooper()));
    }

    @Call(path = NavigationConstants.CALL_PATH_GET_NAV_MAP_LIST)
    public void onGetNavMapList(Request request, final Responder responder) {
        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<Pair<List<NavMap>, String>, NavMapException, Void>() {
                    @Override
                    public Promise<Pair<List<NavMap>, String>, NavMapException, Void>
                    call() throws CallException {
                        return mService.getNavMapList();
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<Pair<List<NavMap>, String>, NavMapException>() {
                    @Override
                    public Message convertDone(Pair<List<NavMap>, String> mapList) {
                        return NavigationConverters.toNavMapListProto(mapList);
                    }

                    @Override
                    public CallException convertFail(NavMapException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Call(path = NavigationConstants.CALL_PATH_ADD_NAV_MAP)
    public void onAddNavMap(Request request, final Responder responder) {
        final NavigationProto.NavMap navMap = ProtoParamParser.parseParam(request,
                NavigationProto.NavMap.class, responder);
        if (navMap == null) {
            return;
        }

        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<NavMap, NavMapException, Void>() {
                    @Override
                    public Promise<NavMap, NavMapException, Void> call() throws CallException {
                        return mService.addNavMap(NavigationConverters.toNavMapPojo(navMap));
                    }
                },
                new NavMapConverter()
        );
    }

    @Call(path = NavigationConstants.CALL_PATH_SELECT_NAV_MAP)
    public void onSelectNavMap(Request request, final Responder responder) {
        final String navMapId = ProtoParamParser.parseStringParam(request, responder);
        if (TextUtils.isEmpty(navMapId)) {
            return;
        }

        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<NavMap, NavMapException, Void>() {
                    @Override
                    public Promise<NavMap, NavMapException, Void> call() throws CallException {
                        return mService.selectNavMap(navMapId);
                    }
                },
                new NavMapConverter()
        );
    }

    @Call(path = NavigationConstants.CALL_PATH_MODIFY_NAV_MAP)
    public void onModifyNavMap(Request request, final Responder responder) {
        final NavigationProto.NavMap navMap = ProtoParamParser.parseParam(request,
                NavigationProto.NavMap.class, responder);
        if (navMap == null) {
            return;
        }

        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<NavMap, NavMapException, Void>() {
                    @Override
                    public Promise<NavMap, NavMapException, Void> call() throws CallException {
                        return mService.modifyNavMap(NavigationConverters.toNavMapPojo(navMap));
                    }
                },
                new NavMapConverter()
        );
    }

    @Call(path = NavigationConstants.CALL_PATH_REMOVE_NAV_MAP)
    public void onRemoveNavMap(Request request, final Responder responder) {
        final String navMapId = ProtoParamParser.parseStringParam(request, responder);
        if (TextUtils.isEmpty(navMapId)) {
            return;
        }

        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<NavMap, NavMapException, Void>() {
                    @Override
                    public Promise<NavMap, NavMapException, Void> call() throws CallException {
                        return mService.removeNavMap(navMapId);
                    }
                },
                new NavMapConverter()
        );
    }

    private static class NavMapConverter extends ProtoCallProcessAdapter.
            DFConverter<NavMap, NavMapException> {

        @Override
        public Message convertDone(NavMap navMap) {
            return NavigationConverters.toNavMapProto(navMap);
        }

        @Override
        public CallException convertFail(NavMapException e) {
            return new CallException(e.getCode(), e.getMessage());
        }
    }
}