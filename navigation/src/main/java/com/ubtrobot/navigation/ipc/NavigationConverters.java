package com.ubtrobot.navigation.ipc;

import android.util.Pair;

import com.ubtrobot.navigation.GroundOverlay;
import com.ubtrobot.navigation.LocateOption;
import com.ubtrobot.navigation.Location;
import com.ubtrobot.navigation.Marker;
import com.ubtrobot.navigation.NavMap;
import com.ubtrobot.navigation.NavigateOption;
import com.ubtrobot.navigation.Navigator;
import com.ubtrobot.navigation.Point;

import java.util.LinkedList;
import java.util.List;

public class NavigationConverters {

    private NavigationConverters() {
    }

    public static NavigationProto.NavMap toNavMapProto(NavMap map) {
        NavigationProto.NavMap.Builder builder = NavigationProto.NavMap.newBuilder().
                setId(map.getId()).setName(map.getName()).
                setTag(map.getTag()).setScale(map.getScale());
        for (GroundOverlay groundOverlay : map.getGroundOverlayList()) {
            builder.addGroundOverlay(toGroundOverlayProto(groundOverlay));
        }

        for (Marker marker : map.getMarkerList()) {
            builder.addMarker(toMarkerProto(marker));
        }

        return builder.build();
    }

    private static NavigationProto.GroundOverlay toGroundOverlayProto(GroundOverlay groundOverlay) {
        return NavigationProto.GroundOverlay.newBuilder().
                setName(groundOverlay.getName()).
                setTag(groundOverlay.getTag()).
                setWidth(groundOverlay.getWidth()).
                setHeight(groundOverlay.getHeight()).
                setOriginInImage(toPointProto(groundOverlay.getOriginInImage())).
                setImageUri(groundOverlay.getImageUri()).
                setRemoteImageUri(groundOverlay.getRemoteImageUri()).
                build();
    }

    private static NavigationProto.Point toPointProto(Point point) {
        return NavigationProto.Point.newBuilder().
                setX(point.getX()).
                setY(point.getY()).build();
    }

    private static NavigationProto.Marker toMarkerProto(Marker marker) {
        return NavigationProto.Marker.newBuilder().
                setId(marker.getId()).
                setTitle(marker.getTitle()).
                setTag(marker.getTag()).
                setDescription(marker.getDescription()).
                setPosition(toPointProto(marker.getPosition())).
                setZ(marker.getZ()).
                setRotation(marker.getRotation()).
                build();
    }

    public static NavMap toNavMapPojo(NavigationProto.NavMap mapProto) {
        NavMap.Builder builder = new NavMap.Builder(mapProto.getId(), mapProto.getScale()).
                setName(mapProto.getName()).setTag(mapProto.getTag());
        for (NavigationProto.GroundOverlay groundOverlay : mapProto.getGroundOverlayList()) {
            builder.addGroundOverlay(toGroundOverlayPojo(groundOverlay));
        }

        for (NavigationProto.Marker marker : mapProto.getMarkerList()) {
            builder.addMarker(toMarkerPojo(marker));
        }

        return builder.build();
    }

    private static GroundOverlay toGroundOverlayPojo(NavigationProto.GroundOverlay groundOverlay) {
        return new GroundOverlay.Builder(groundOverlay.getWidth(), groundOverlay.getHeight()).
                setName(groundOverlay.getName()).
                setTag(groundOverlay.getTag()).
                setOriginInImage(toPointPojo(groundOverlay.getOriginInImage())).
                setImageUri(groundOverlay.getImageUri()).
                setRemoteImageUri(groundOverlay.getRemoteImageUri()).
                build();
    }

    private static Point toPointPojo(NavigationProto.Point point) {
        return new Point(point.getX(), point.getY());
    }

    private static Marker toMarkerPojo(NavigationProto.Marker marker) {
        return new Marker.Builder(marker.getId(), toPointPojo(marker.getPosition())).
                setTitle(marker.getTitle()).
                setTag(marker.getTag()).
                setDescription(marker.getDescription()).
                setZ(marker.getZ()).
                setRotation(marker.getRotation()).
                build();
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
        return new Location.Builder(toPointPojo(location.getPosition())).
                setZ(location.getZ()).setRotation(location.getRotation()).build();
    }

    public static NavigationProto.Location toLocationProto(Location location) {
        return NavigationProto.Location.newBuilder().
                setPosition(toPointProto(location.getPosition())).
                setRotation(location.getRotation()).
                setZ(location.getZ()).
                build();
    }

    public static NavigationProto.LocateOption toLocateOptionProto(LocateOption option) {
        return NavigationProto.LocateOption.newBuilder().setUseNearby(option.useNearby()).
                setNearby(NavigationProto.Location.newBuilder()
                        .setPosition(NavigationProto.Point.newBuilder()
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