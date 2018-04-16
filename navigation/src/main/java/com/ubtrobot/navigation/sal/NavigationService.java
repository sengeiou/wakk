package com.ubtrobot.navigation.sal;

import android.util.Pair;

import com.ubtrobot.async.Promise;
import com.ubtrobot.navigation.LocateException;
import com.ubtrobot.navigation.LocateOption;
import com.ubtrobot.navigation.Location;
import com.ubtrobot.navigation.NavMap;
import com.ubtrobot.navigation.NavMapException;
import com.ubtrobot.navigation.NavigateException;
import com.ubtrobot.navigation.NavigateOption;
import com.ubtrobot.navigation.Navigator;

import java.util.List;

/**
 * 导航服务
 */
public interface NavigationService {

    Promise<Pair<List<NavMap>, String>, NavMapException, Void> getNavMapList();

    Promise<NavMap, NavMapException, Void> addNavMap(NavMap navMap);

    Promise<NavMap, NavMapException, Void> selectNavMap(String navMapId);

    Promise<NavMap, NavMapException, Void> modifyNavMap(NavMap navMap);

    Promise<NavMap, NavMapException, Void> removeNavMap(String navMapId);

    Promise<Location, LocateException, Void> locateSelf(LocateOption option);

    Promise<Void, NavigateException, Navigator.NavigatingProgress>
    navigate(Location destination, NavigateOption option);
}