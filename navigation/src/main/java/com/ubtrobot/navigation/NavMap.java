package com.ubtrobot.navigation;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 导航地图
 */
public class NavMap {

    private String id;
    private String name;
    private List<GroundOverlay> groundOverlayList;
    private List<Marker> markerList;

    private NavMap(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<GroundOverlay> getGroundOverlayList() {
        return groundOverlayList;
    }

    public List<Marker> getMarkerList() {
        return markerList;
    }

    public Marker getMarker(String title) {
        for (Marker marker : markerList) {
            if (marker.getTitle().equals(title)) {
                return marker;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return "NavMap{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", groundOverlayList=" + groundOverlayList +
                ", markerList=" + markerList +
                '}';
    }

    public static class Builder {

        private String id;
        private String name;
        private List<GroundOverlay> groundOverlayList = new LinkedList<>();
        private LinkedList<Marker> markerList = new LinkedList<>();

        public Builder(NavMap map) {
            if (map == null) {
                throw new IllegalArgumentException("Argument map is null.");
            }

            this.id = map.getId();
            this.name = map.getName();
            this.markerList.addAll(map.getMarkerList());
        }

        public Builder(String id) {
            if (id == null) {
                throw new IllegalArgumentException("Argument is is null.");
            }

            this.id = id;
        }

        public Builder setName(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Argument name is null.");
            }

            this.name = name;
            return this;
        }

        public Builder addMarkers(List<Marker> markers) {
            if (markers == null) {
                throw new IllegalArgumentException("Argument markers is null.");
            }

            markerList.addAll(markers);
            return this;
        }

        public Builder addMarker(Marker marker) {
            if (marker == null) {
                throw new IllegalArgumentException("Argument marker is null.");
            }

            markerList.add(marker);
            return this;
        }

        public Builder addGroundOverlays(List<GroundOverlay> overlays) {
            if (overlays == null) {
                throw new IllegalArgumentException("Argument overlays is null.");
            }

            groundOverlayList.addAll(overlays);
            return this;
        }

        public Builder addGroundOverlay(GroundOverlay overlay) {
            if (overlay == null) {
                throw new IllegalArgumentException("Argument overlay is null.");
            }

            groundOverlayList.add(overlay);
            return this;
        }

        public NavMap build() {
            NavMap navMap = new NavMap(id);
            navMap.name = name;
            navMap.groundOverlayList = Collections.unmodifiableList(groundOverlayList);
            navMap.markerList = Collections.unmodifiableList(markerList);

            return navMap;
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", groundOverlayList=" + groundOverlayList +
                    ", markerList=" + markerList +
                    '}';
        }
    }
}