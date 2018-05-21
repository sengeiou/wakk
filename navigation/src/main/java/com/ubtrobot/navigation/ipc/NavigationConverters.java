package com.ubtrobot.navigation.ipc;

import android.util.Pair;

import com.ubtrobot.navigation.GroundOverlay;
import com.ubtrobot.navigation.LocateOption;
import com.ubtrobot.navigation.Location;
import com.ubtrobot.navigation.Marker;
import com.ubtrobot.navigation.NavMap;
import com.ubtrobot.navigation.NavigateOption;
import com.ubtrobot.navigation.Navigator;
import com.ubtrobot.navigation.Position;

import java.util.LinkedList;
import java.util.List;

public class NavigationConverters {

    private NavigationConverters() {
    }

    public static NavigationProto.NavMap toNavMapProto(NavMap map) {
        NavigationProto.NavMap.Builder builder = NavigationProto.NavMap.newBuilder().
                setId(map.getId()).setName(map.getName());
        for (GroundOverlay groundOverlay : map.getGroundOverlayList()) {
            builder.addGroundOverlay(NavigationProto.GroundOverlay.newBuilder().
                    setImage(groundOverlay.getImage()).
                    setRemoteImage(groundOverlay.getRemoteImage()).build());
        }

        for (Marker marker : map.getMarkerList()) {
            builder.addMarker(NavigationProto.Marker.newBuilder().
                    setTitle(marker.getTitle()).
                    setPosition(NavigationProto.Position.newBuilder().
                            setX(marker.getPosition().getX()).
                            setY(marker.getPosition().getY()).build()
                    ).
                    setZ(marker.getZ()).
                    setRotation(marker.getRotation()).
                    build()
            );
        }

        return builder.build();
    }

    public static NavMap toNavMapPojo(NavigationProto.NavMap mapProto) {
        NavMap.Builder builder = new NavMap.Builder(mapProto.getId()).setName(mapProto.getName());
        for (NavigationProto.GroundOverlay groundOverlay : mapProto.getGroundOverlayList()) {
            builder.addGroundOverlay(new GroundOverlay(groundOverlay.getImage(),
                    groundOverlay.getRemoteImage()));
        }

        for (NavigationProto.Marker marker : mapProto.getMarkerList()) {
            builder.addMarker(new Marker.Builder(
                    marker.getTitle(),
                    new Position(
                            marker.getPosition().getX(),
                            marker.getPosition().getY())
            ).setZ(marker.getZ()).setRotation(marker.getRotation()).build());
        }

        return builder.build();
    }

    public static NavigationProto.NavMapList toNavMapListProto(Pair<List<NavMap>, String> maps) {
        NavigationProto.NavMapList.Builder builder = NavigationProto.NavMapList.newBuilder();
        for (NavMap map : maps.first) {
            builder.addMap(toNavMapProto(map));
        }

        return builder.setIdSelected(maps.second).build();
    }

    public static Pair<List<NavMap>, String> toNavMapListPojo(NavigationProto.NavMapList mapListProto) {
        List<NavMap> maps = new LinkedList<>();
        for (NavigationProto.NavMap mapProto : mapListProto.getMapList()) {
            maps.add(toNavMapPojo(mapProto));
        }

        return new Pair<>(maps, mapListProto.getIdSelected());
    }

    public static Location toLocationPojo(NavigationProto.Location location) {
        return new Location.Builder(new Position(
                location.getPosition().getX(),
                location.getPosition().getY()
        )).setZ(location.getZ()).setRotation(location.getRotation()).build();
    }

    public static NavigationProto.Location toLocationProto(Location location) {
        return NavigationProto.Location.newBuilder().
                setPosition(NavigationProto.Position.newBuilder().
                        setX(location.getPosition().getX()).
                        setY(location.getPosition().getY()).build()).
                setRotation(location.getRotation()).
                setZ(location.getZ()).
                build();
    }

    public static NavigationProto.LocateOption toLocateOptionProto(LocateOption option) {
        return NavigationProto.LocateOption.newBuilder().setUseNearby(option.useNearby()).
                setNearby(NavigationProto.Location.newBuilder()
                        .setPosition(NavigationProto.Position.newBuilder()
                                .setX(option.getNearby().getPosition().getX())
                                .setY(option.getNearby().getPosition().getY())
                                .build()
                        ).setRotation(option.getNearby().getRotation())
                        .setZ(option.getNearby().getZ())
                        .build()).build();
    }

    public static LocateOption toLocateOptionPojo(NavigationProto.LocateOption option) {
        if (option.getUseNearby()) {
            return new LocateOption.Builder().setNearby(toLocationPojo(option.getNearby())).build();
        } else {
            return new LocateOption.Builder().build();
        }
    }

    public static NavigationProto.NavigateOption
    toNavigateOptionProto(Location destination, NavigateOption option) {
        return NavigationProto.NavigateOption.newBuilder().
                setDestination(toLocationProto(destination)).
                setMaxSpeed(option.getMaxSpeed()).
                setRetryCount(option.getRetryCount()).
                build();
    }

    public static NavigateOption toNavigateOptionPojo(NavigationProto.NavigateOption option) {
        return new NavigateOption.Builder().setMaxSpeed(option.getMaxSpeed()).
                setRetryCount(option.getRetryCount()).build();
    }

    public static NavigationProto.NavigatingProgress
    toNavigatingProgressProto(Navigator.NavigatingProgress progress) {
        return NavigationProto.NavigatingProgress.newBuilder().setState(progress.getState()).
                setLocation(toLocationProto(progress.getLocation())).build();
    }

    public static Navigator.NavigatingProgress
    toNavigatingProgressPojo(NavigationProto.NavigatingProgress progress) {
        return new Navigator.NavigatingProgress.Builder(
                progress.getState()).setLocation(toLocationPojo(progress.getLocation())).build();
    }
}