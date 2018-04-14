package com.ubtrobot.navigation;

import android.os.Handler;
import android.os.Looper;

import com.ubtrobot.async.Promise;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.navigation.ipc.NavigationConstants;

import java.util.List;

public class NavigationManager {

    private final MasterContext mMasterContext;

    private final NavMapList mNavMapList;

    public NavigationManager(MasterContext masterContext) {
        mMasterContext = masterContext;

        ProtoCallAdapter navigationService = new ProtoCallAdapter(
                mMasterContext.createSystemServiceProxy(NavigationConstants.SERVICE_NAME),
                new Handler(Looper.getMainLooper())
        );
        mNavMapList = new NavMapList(navigationService);
    }

    public List<NavMap> getNavMapList() {
        return mNavMapList.all();
    }

    public NavMap getNavMap(String navMapId) {
        return mNavMapList.get(navMapId);
    }

    public Promise<NavMap, NavMapException, Void> addNavMap(NavMap navMap) {
        return mNavMapList.add(navMap);
    }

    public NavMap getSelectedNavMap() {
        return mNavMapList.getSelected();
    }

    public Promise<NavMap, NavMapException, Void> selectNavMap(String navMapId) {
        return mNavMapList.select(navMapId);
    }

    public Promise<NavMap, NavMapException, Void> modifyNavMap(NavMap navMap) {
        return mNavMapList.modify(navMap);
    }

    public Promise<NavMap, NavMapException, Void> removeNavMap(String navMapId) {
        return mNavMapList.remove(navMapId);
    }
}