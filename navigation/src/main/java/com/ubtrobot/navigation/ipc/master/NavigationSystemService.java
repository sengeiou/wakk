package com.ubtrobot.navigation.ipc.master;

import android.app.Application;
import android.os.Handler;
import android.text.TextUtils;

import com.google.protobuf.Message;
import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.master.adapter.CallProcessAdapter;
import com.ubtrobot.master.adapter.ProtoCallProcessAdapter;
import com.ubtrobot.master.adapter.ProtoParamParser;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.competition.CompetingCallDelegate;
import com.ubtrobot.master.competition.CompetingItemDetail;
import com.ubtrobot.master.competition.CompetitionSessionInfo;
import com.ubtrobot.master.competition.ProtoCompetingCallDelegate;
import com.ubtrobot.master.service.MasterSystemService;
import com.ubtrobot.navigation.LocateException;
import com.ubtrobot.navigation.Location;
import com.ubtrobot.navigation.NavMap;
import com.ubtrobot.navigation.NavMapException;
import com.ubtrobot.navigation.NavigateException;
import com.ubtrobot.navigation.Navigator;
import com.ubtrobot.navigation.ipc.NavigationConstants;
import com.ubtrobot.navigation.ipc.NavigationConverters;
import com.ubtrobot.navigation.ipc.NavigationProto;
import com.ubtrobot.navigation.sal.AbstractNavigationService;
import com.ubtrobot.navigation.sal.NavigationFactory;
import com.ubtrobot.navigation.sal.NavigationService;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;

import java.util.Collections;
import java.util.List;

public class NavigationSystemService extends MasterSystemService {

    private NavigationService mService;
    private ProtoCallProcessAdapter mCallProcessor;
    private ProtoCompetingCallDelegate mCompetingCallDelegate;

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

        Handler handler = new Handler(getMainLooper());
        mCallProcessor = new ProtoCallProcessAdapter(handler);
        mCompetingCallDelegate = new ProtoCompetingCallDelegate(this, handler);
    }

    @Override
    protected List<CompetingItemDetail> getCompetingItems() {
        return Collections.singletonList(new CompetingItemDetail.Builder(
                getName(), NavigationConstants.COMPETING_ITEM_NAVIGATOR).
                setDescription("Navigator competing item.").
                addCallPath(NavigationConstants.CALL_PATH_LOCATE_SELF).
                addCallPath(NavigationConstants.CALL_PATH_NAVIGATE).
                build()
        );
    }

    @Call(path = NavigationConstants.CALL_PATH_GET_NAV_MAP_LIST)
    public void onGetNavMapList(Request request, final Responder responder) {
        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<List<NavMap>, NavMapException>() {
                    @Override
                    public Promise<List<NavMap>, NavMapException>
                    call() throws CallException {
                        return mService.getNavMapList();
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<List<NavMap>, NavMapException>() {
                    @Override
                    public Message convertDone(List<NavMap> mapList) {
                        return NavigationConverters.toNavMapListProto(mapList);
                    }

                    @Override
                    public CallException convertFail(NavMapException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Call(path = NavigationConstants.CALL_PATH_GET_NAV_MAP)
    public void onGetNavMap(Request request, final Responder responder) {
        final String navMapId = ProtoParamParser.parseStringParam(request, responder);
        if (TextUtils.isEmpty(navMapId)) {
            return;
        }

        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<NavMap, NavMapException>() {
                    @Override
                    public Promise<NavMap, NavMapException> call() throws CallException {
                        return mService.getNavMap(navMapId);
                    }
                },
                new NavMapConverter()
        );
    }

    @Call(path = NavigationConstants.CALL_PATH_GET_SELECTED_NAV_MAP)
    public void onGetSelectedNavMap(Request request, final Responder responder) {
        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<NavMap, NavMapException>() {
                    @Override
                    public Promise<NavMap, NavMapException> call() throws CallException {
                        return mService.getSelectedNavMap();
                    }
                },
                new NavMapConverter()
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
                new CallProcessAdapter.Callable<NavMap, NavMapException>() {
                    @Override
                    public Promise<NavMap, NavMapException> call() throws CallException {
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
                new CallProcessAdapter.Callable<NavMap, NavMapException>() {
                    @Override
                    public Promise<NavMap, NavMapException> call() throws CallException {
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
                new CallProcessAdapter.Callable<NavMap, NavMapException>() {
                    @Override
                    public Promise<NavMap, NavMapException> call() throws CallException {
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
                new CallProcessAdapter.Callable<NavMap, NavMapException>() {
                    @Override
                    public Promise<NavMap, NavMapException> call() throws CallException {
                        return mService.removeNavMap(navMapId);
                    }
                },
                new NavMapConverter()
        );
    }

    @Call(path = NavigationConstants.CALL_PATH_LOCATE_SELF)
    public void onLocateSelf(Request request, Responder responder) {
        final NavigationProto.LocateOption locateOption = ProtoParamParser.parseParam(request,
                NavigationProto.LocateOption.class, responder);
        if (locateOption == null) {
            return;
        }

        mCompetingCallDelegate.onCall(
                request,
                NavigationConstants.COMPETING_ITEM_NAVIGATOR,
                responder,
                new CompetingCallDelegate.SessionCallable<Location, LocateException>() {
                    @Override
                    public Promise<Location, LocateException> call() throws CallException {
                        return mService.locateSelf(NavigationConverters.
                                toLocateOptionPojo(locateOption));
                    }
                },
                new ProtoCompetingCallDelegate.DFConverter<Location, LocateException>() {
                    @Override
                    public Message convertDone(Location location) {
                        return NavigationConverters.toLocationProto(location);
                    }

                    @Override
                    public CallException convertFail(LocateException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Call(path = NavigationConstants.CALL_PATH_NAVIGATE)
    public void onNavigate(Request request, Responder responder) {
        final NavigationProto.NavigateOption navigateOption = ProtoParamParser.parseParam(request,
                NavigationProto.NavigateOption.class, responder);
        if (navigateOption == null) {
            return;
        }

        mCompetingCallDelegate.onCall(
                request,
                NavigationConstants.COMPETING_ITEM_NAVIGATOR,
                responder,
                new CompetingCallDelegate.SessionProgressiveCallable<
                        Void, NavigateException, Navigator.NavigatingProgress>() {
                    @Override
                    public ProgressivePromise<Void, NavigateException, Navigator.NavigatingProgress>
                    call() throws CallException {
                        return mService.navigate(
                                NavigationConverters.toLocationPojo(navigateOption.getDestination()),
                                NavigationConverters.toNavigateOptionPojo(navigateOption)
                        );
                    }
                },
                new ProtoCompetingCallDelegate.DFPConverter<
                        Void, NavigateException, Navigator.NavigatingProgress>() {
                    @Override
                    public Message convertDone(Void done) {
                        return null;
                    }

                    @Override
                    public CallException convertFail(NavigateException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }

                    @Override
                    public Message convertProgress(Navigator.NavigatingProgress progress) {
                        return NavigationConverters.toNavigatingProgressProto(progress);
                    }
                }
        );
    }

    @Override
    protected void onCompetitionSessionInactive(CompetitionSessionInfo sessionInfo) {
        mCompetingCallDelegate.onCompetitionSessionInactive(sessionInfo);
    }

    private static class NavMapConverter implements ProtoCallProcessAdapter.
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