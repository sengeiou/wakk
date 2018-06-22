package com.ubtrobot.navigation.sal;

import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.navigation.GetLocationException;
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

    Promise<List<NavMap>, NavMapException> getNavMapList();

    Promise<NavMap, NavMapException> getNavMap(String mapId);

    Promise<NavMap, NavMapException> getSelectedNavMap();

    Promise<NavMap, NavMapException> addNavMap(NavMap navMap);

    Promise<NavMap, NavMapException> selectNavMap(String navMapId);

    Promise<NavMap, NavMapException> modifyNavMap(NavMap navMap);

    Promise<NavMap, NavMapException> removeNavMap(String navMapId);

    Promise<Location, GetLocationException> getCurrentLocation();

    Promise<Location, LocateException> locateSelf(LocateOption option);

    Promise<Boolean, AccessServiceException> isLocating();

    ProgressivePromise<Void, NavigateException, Navigator.NavigatingProgress>
    navigate(Location destination, NavigateOption option);

    Promise<Boolean, AccessServiceException> isNavigating();
}