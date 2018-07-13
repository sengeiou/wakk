package com.ubtrobot.navigation;

import android.os.Handler;
import android.os.Looper;

import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.competition.ActivateException;
import com.ubtrobot.master.competition.CompetitionSession;
import com.ubtrobot.master.competition.CompetitionSessionExt;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.navigation.ipc.NavigationConstants;

import java.util.List;

public class NavigationManager {

    private final MasterContext mMasterContext;

    private final NavMapList mNavMapList;

    private final Navigator mNavigator;
    private volatile CompetitionSessionExt<Navigator> mSession;

    public NavigationManager(MasterContext masterContext) {
        mMasterContext = masterContext;
        Handler handler = new Handler(Looper.getMainLooper());

        ProtoCallAdapter navigationService = new ProtoCallAdapter(
                mMasterContext.createSystemServiceProxy(NavigationConstants.SERVICE_NAME),
                handler
        );
        mNavMapList = new NavMapList(navigationService);
        mNavigator = new Navigator(masterContext, navigationService, handler);
    }

    public Promise<List<NavMap>, NavMapException> getNavMapList() {
        return mNavMapList.all();
    }

    public Promise<NavMap, NavMapException> getNavMap(String navMapId) {
        return mNavMapList.get(navMapId);
    }

    public Promise<NavMap, NavMapException> addNavMap(NavMap navMap) {
        return mNavMapList.add(navMap);
    }

    public Promise<NavMap, NavMapException> getSelectedNavMap() {
        return mNavMapList.getSelected();
    }

    public Promise<NavMap, NavMapException> selectNavMap(String navMapId) {
        return mNavMapList.select(navMapId);
    }

    public Promise<NavMap, NavMapException> modifyNavMap(NavMap navMap) {
        return mNavMapList.modify(navMap);
    }

    public Promise<NavMap, NavMapException> removeNavMap(String navMapId) {
        return mNavMapList.remove(navMapId);
    }

    private CompetitionSessionExt<Navigator> navigatorSession() {
        if (mSession != null) {
            return mSession;
        }

        synchronized (this) {
            if (mSession != null) {
                return mSession;
            }

            mSession = new CompetitionSessionExt<>(mMasterContext.openCompetitionSession().
                    addCompeting(navigator()));
            return mSession;
        }
    }

    public Promise<Location, GetLocationException> getCurrentLocation() {
        return mNavigator.getCurrentLocation();
    }

    public Promise<Location, LocateException> locateSelf() {
        return locateSelf(LocateOption.DEFAULT);
    }

    public Promise<Location, LocateException>
    locateSelf(final LocateOption option) {
        return navigatorSession().execute(
                mNavigator,
                new CompetitionSessionExt.SessionCallable<
                        Location, LocateException, Navigator>() {
                    @Override
                    public Promise<Location, LocateException>
                    call(CompetitionSession session, Navigator navigator) {
                        return navigator.locateSelf(session, option);
                    }
                },
                new CompetitionSessionExt.Converter<LocateException>() {
                    @Override
                    public LocateException convert(ActivateException e) {
                        return new LocateException.Factory().occupied(e);
                    }
                }
        );
    }

    public Promise<Boolean, AccessServiceException> isLocating() {
        return mNavigator.isLocating();
    }

    public ProgressivePromise<Void, NavigateException, Navigator.NavigatingProgress>
    navigate(Location destination) {
        return navigate(destination, NavigateOption.DEFAULT);
    }

    public ProgressivePromise<Void, NavigateException, Navigator.NavigatingProgress>
    navigate(final Location destination, final NavigateOption option) {
        return navigatorSession().execute(
                mNavigator,
                new CompetitionSessionExt.SessionProgressiveCallable<
                        Void, NavigateException, Navigator.NavigatingProgress, Navigator>() {
                    @Override
                    public ProgressivePromise<Void, NavigateException, Navigator.NavigatingProgress>
                    call(CompetitionSession session, Navigator navigator) {
                        return navigator.navigate(session, destination, option);
                    }
                },
                new CompetitionSessionExt.Converter<NavigateException>() {
                    @Override
                    public NavigateException convert(ActivateException e) {
                        return new NavigateException.Factory().occupied(e);
                    }
                }
        );
    }

    public Promise<Boolean, AccessServiceException> isNavigating() {
        return mNavigator.isNavigating();
    }

    public Navigator navigator() {
        return mNavigator;
    }

    public void registerLocationChangeListener(LocationChangeListener locationChangeListener) {
        mNavigator.registerLocationChangeListener(locationChangeListener);
    }

    public void unregisterLocationChangeListener(LocationChangeListener locationChangeListener) {
        mNavigator.unregisterLocationChangeListener(locationChangeListener);
    }
}