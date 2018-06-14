package com.ubtrobot.navigation.ipc;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.ubtrobot.io.IoConverters;
import com.ubtrobot.io.ipc.IoProto;
import com.ubtrobot.navigation.GroundOverlay;
import com.ubtrobot.navigation.LocateOption;
import com.ubtrobot.navigation.Location;
import com.ubtrobot.navigation.Marker;
import com.ubtrobot.navigation.NavMap;
import com.ubtrobot.navigation.NavigateOption;
import com.ubtrobot.navigation.Navigator;
import com.ubtrobot.navigation.Point;
import com.ubtrobot.navigation.Polyline;

import java.util.LinkedList;
import java.util.List;

public class NavigationConverters {

    private NavigationConverters() {
    }

    public static NavigationProto.NavMap toNavMapProto(NavMap map) {
        NavigationProto.NavMap.Builder builder = NavigationProto.NavMap.newBuilder().
                setId(map.getId()).setName(map.getName()).
                setExtension(map.getExtension()).setScale(map.getScale()).
                setNavFile(Any.pack(IoConverters.toFileInfoProto(map.getNavFile())));
        for (GroundOverlay groundOverlay : map.getGroundOverlayList()) {
            builder.addGroundOverlay(toGroundOverlayProto(groundOverlay));
        }

        for (Marker marker : map.getMarkerList()) {
            builder.addMarker(toMarkerProto(marker));
        }

        for (Polyline polyline : map.getPolylineList()) {
            builder.addPolyline(toPolylineProto(polyline));
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
                setImage(Any.pack(IoConverters.toFileInfoProto(groundOverlay.getImage()))).
                build();
    }

    private static NavigationProto.Point toPointProto(Point point) {
        return NavigationProto.Point.newBuilder().
                setX(point.getX()).
                setY(point.getY()).build();
    }

    private static NavigationProto.Marker toMarkerProto(Marker marker) {
        NavigationProto.Marker.Builder builder = NavigationProto.Marker.newBuilder().
                setId(marker.getId()).
                setTitle(marker.getTitle()).
                setDescription(marker.getDescription()).
                setPosition(toPointProto(marker.getPosition())).
                setZ(marker.getZ()).
                setRotation(marker.getRotation());
        for (String tag : marker.getTagList()) {
            builder.addTag(tag);
        }
        return builder.build();
    }

    private static NavigationProto.Polyline toPolylineProto(Polyline polyline) {
        NavigationProto.Polyline.Builder builder = NavigationProto.Polyline.newBuilder()
                .setId(polyline.getId()).setName(polyline.getName())
                .setDescription(polyline.getDescription()).setExtension(polyline.getExtension());

        for (Location location : polyline.getLocationList()) {
            builder.addLocation(toLocationProto(location));
        }

        return builder.build();
    }

    private static Polyline toPolylinePojo(NavigationProto.Polyline polyline) {
        Polyline.Builder builder = new Polyline.Builder(polyline.getId())
                .setName(polyline.getName()).setDescription(polyline.getDescription())
                .setExtension(polyline.getExtension());
        for (NavigationProto.Location location : polyline.getLocationList()) {
            builder.addLocation(toLocationPojo(location));
        }

        return builder.build();
    }

    public static NavMap toNavMapPojo(NavigationProto.NavMap mapProto)
            throws InvalidProtocolBufferException {
        NavMap.Builder builder = new NavMap.Builder(mapProto.getId(), mapProto.getScale()).
                setName(mapProto.getName()).setExtension(mapProto.getExtension()).
                setNavFile(IoConverters.toFileInfoPojo(mapProto.getNavFile().
                        unpack(IoProto.FileInfo.class)));
        for (NavigationProto.GroundOverlay groundOverlay : mapProto.getGroundOverlayList()) {
            builder.addGroundOverlay(toGroundOverlayPojo(groundOverlay));
        }

        for (NavigationProto.Marker marker : mapProto.getMarkerList()) {
            builder.addMarker(toMarkerPojo(marker));
        }

        for (NavigationProto.Polyline polyline : mapProto.getPolylineList()) {
            builder.addPolyline(toPolylinePojo(polyline));
        }

        return builder.build();
    }

    private static GroundOverlay toGroundOverlayPojo(NavigationProto.GroundOverlay groundOverlay)
            throws InvalidProtocolBufferException {
        return new GroundOverlay.Builder().
                setWidth(groundOverlay.getWidth()).
                setHeight(groundOverlay.getHeight()).
                setName(groundOverlay.getName()).
                setTag(groundOverlay.getTag()).
                setOriginInImage(toPointPojo(groundOverlay.getOriginInImage())).
                setImage(IoConverters.toFileInfoPojo(groundOverlay.getImage().unpack(IoProto.FileInfo.class))).
                build();
    }

    private static Point toPointPojo(NavigationProto.Point point) {
        return new Point(point.getX(), point.getY());
    }

    private static Marker toMarkerPojo(NavigationProto.Marker marker) {
        Marker.Builder builder = new Marker.Builder(marker.getId(), toPointPojo(marker.getPosition())).
                setTitle(marker.getTitle()).
                setDescription(marker.getDescription()).
                setZ(marker.getZ()).
                setRotation(marker.getRotation());
        for (String tag : marker.getTagList()) {
            builder.addTag(tag);
        }
        return builder.build();
    }

    public static NavigationProto.NavMapList toNavMapListProto(List<NavMap> maps) {
        NavigationProto.NavMapList.Builder builder = NavigationProto.NavMapList.newBuilder();
        for (NavMap map : maps) {
            builder.addMap(toNavMapProto(map));
        }

        return builder.build();
    }

    public static List<NavMap> toNavMapListPojo(NavigationProto.NavMapList mapListProto)
            throws InvalidProtocolBufferException {
        List<NavMap> maps = new LinkedList<>();
        for (NavigationProto.NavMap mapProto : mapListProto.getMapList()) {
            maps.add(toNavMapPojo(mapProto));
        }

        return maps;
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
        return NavigationProto.LocateOption.newBuilder().setUseNearby(option.useNearby())
                .setTimeout(option.getTimeout()).setNearby(toLocationProto(option.getNearby()))
                .build();
    }

    public static LocateOption toLocateOptionPojo(NavigationProto.LocateOption option) {
        if (option.getUseNearby()) {
            return new LocateOption.Builder().setNearby(toLocationPojo(option.getNearby()))
                    .setTimeout(option.getTimeout()).build();
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