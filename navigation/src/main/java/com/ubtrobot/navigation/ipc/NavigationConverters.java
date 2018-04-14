package com.ubtrobot.navigation.ipc;

import android.util.Pair;

import com.ubtrobot.navigation.GroundOverlay;
import com.ubtrobot.navigation.LatLng;
import com.ubtrobot.navigation.Marker;
import com.ubtrobot.navigation.NavMap;

import java.util.LinkedList;
import java.util.List;

public class NavigationConverters {

    private NavigationConverters() {
    }

    public static NavigationProto.NavMap toNavMapProto(NavMap map) {
        NavigationProto.NavMap.Builder builder = NavigationProto.NavMap.newBuilder();
        for (GroundOverlay groundOverlay : map.getGroundOverlayList()) {
            builder.addGroundOverlay(NavigationProto.GroundOverlay.newBuilder().
                    setImage(groundOverlay.getImage()).
                    setRemoteImage(groundOverlay.getRemoteImage()).build());
        }

        for (Marker marker : map.getMarkerList()) {
            builder.addMarker(NavigationProto.Marker.newBuilder().
                    setTitle(marker.getTitle()).
                    setPosition(NavigationProto.LatLng.newBuilder().
                            setLatitude(marker.getPosition().getLatitude()).
                            setLongitude(marker.getPosition().getLongitude()).build()
                    ).
                    setElevation(marker.getElevation()).
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
                    new LatLng(
                            marker.getPosition().getLatitude(),
                            marker.getPosition().getLongitude())
            ).setElevation(marker.getElevation()).setRotation(marker.getRotation()).build());
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
}