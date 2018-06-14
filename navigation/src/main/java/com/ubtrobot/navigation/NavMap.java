package com.ubtrobot.navigation;

import android.text.TextUtils;

import com.ubtrobot.io.FileInfo;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 导航地图
 */
public class NavMap {

    private String id;
    private String name;
    private String tag;
    private float scale;
    private List<GroundOverlay> groundOverlayList;
    private List<Marker> markerList;
    private List<Polyline> polylineList;
    private FileInfo navFile;

    private NavMap(String id, float scale) {
        this.id = id;
        this.scale = scale;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return tag;
    }

    public float getScale() {
        return scale;
    }

    public List<GroundOverlay> getGroundOverlayList() {
        return groundOverlayList;
    }

    public List<Marker> getMarkerList() {
        return markerList;
    }

    public Marker getMarker(String id) {
        for (Marker marker : markerList) {
            if (marker.getId().equals(id)) {
                return marker;
            }
        }

        return null;
    }

    public List<Polyline> getPolylineList() {
        return polylineList;
    }

    public Polyline getPolyline(String id) {
        for (Polyline polyline : polylineList) {
            if (polyline.getId().equals(id)) {
                return polyline;
            }
        }

        return null;
    }

    public FileInfo getNavFile() {
        return navFile;
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public String toString() {
        return "NavMap{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", tag='" + tag + '\'' +
                ", scale=" + scale +
                ", groundOverlayList=" + groundOverlayList +
                ", markerList=" + markerList +
                ", polylineList=" + polylineList +
                ", navFile=" + navFile +
                '}';
    }

    public static class Builder {

        private String id;
        private String name;
        private String tag;
        private float scale;
        private FileInfo navFile;

        private List<GroundOverlay> groundOverlayList = new LinkedList<>();
        private LinkedList<Marker> markerList = new LinkedList<>();
        private LinkedList<Polyline> polylineList = new LinkedList<>();

        public Builder(NavMap map) {
            if (map == null) {
                throw new IllegalArgumentException("Argument map is null.");
            }

            id = map.getId();
            name = map.getName();
            tag = map.getTag();
            scale = map.scale;
            navFile = map.getNavFile();

            groundOverlayList.addAll(map.getGroundOverlayList());
            markerList.addAll(map.getMarkerList());
            polylineList.addAll(map.getPolylineList());
        }

        public Builder(String id, float scale) {
            if (TextUtils.isEmpty(id) || scale <= 0) {
                throw new IllegalArgumentException("Argument id is an empty string or scale <= 0.");
            }

            this.id = id;
            this.scale = scale;
        }

        public Builder setName(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Argument name is null.");
            }

            this.name = name;
            return this;
        }

        public Builder setTag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder setNavFile(FileInfo navFile) {
            if (navFile == null) {
                throw new IllegalArgumentException("Argument navFile is null.");
            }

            this.navFile = navFile;
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

        public Builder setMarker(int index, Marker marker) {
            if (index < 0 || index >= markerList.size()) {
                throw new IllegalArgumentException("Index out of bounds.");
            }
            if (marker == null) {
                throw new IllegalArgumentException("Argument marker is null.");
            }

            markerList.set(index, marker);
            return this;
        }

        public Builder removeMarker(int index) {
            if (index < 0 || index >= markerList.size()) {
                throw new IllegalArgumentException("Index out of bounds.");
            }

            markerList.remove(index);
            return this;
        }

        public Builder addPolylineList(List<Polyline> polylineList) {
            if (polylineList == null) {
                throw new IllegalArgumentException("Argument polylineList is null.");
            }

            this.polylineList.addAll(polylineList);
            return this;
        }

        public Builder setPolylineList(List<Polyline> polylineList) {
            if (polylineList == null) {
                throw new IllegalArgumentException("Argument polylineList is null.");
            }

            this.polylineList.clear();
            this.polylineList.addAll(polylineList);
            return this;
        }

        public Builder addPolyline(Polyline polyline) {
            if (polyline == null) {
                throw new IllegalArgumentException("Argument polyline is null.");
            }

            polylineList.add(polyline);
            return this;
        }

        public Builder setPolyline(int index, Polyline polyline) {
            if (index < 0 || index >= polylineList.size()) {
                throw new IllegalArgumentException("Index out of bounds.");
            }
            if (polyline == null) {
                throw new IllegalArgumentException("Argument polyline is null.");
            }

            polylineList.set(index, polyline);
            return this;
        }

        public Builder removePolyline(int index) {
            if (index < 0 || index >= polylineList.size()) {
                throw new IllegalArgumentException("Index out of bounds.");
            }

            polylineList.remove(index);
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

        public Builder setGroundOverlay(int index, GroundOverlay overlay) {
            if (index < 0 || index >= groundOverlayList.size()) {
                throw new IllegalArgumentException("Index out of bounds.");
            }
            if (overlay == null) {
                throw new IllegalArgumentException("Argument overlay is null.");
            }

            groundOverlayList.set(index, overlay);
            return this;
        }

        public Builder removeGroundOverlay(int index) {
            if (index < 0 || index >= groundOverlayList.size()) {
                throw new IllegalArgumentException("Index out of bounds.");
            }

            groundOverlayList.remove(index);
            return this;
        }

        public NavMap build() {
            NavMap navMap = new NavMap(id, scale);
            navMap.name = name == null ? "" : name;
            navMap.tag = tag == null ? "" : tag;
            navMap.navFile = navFile == null ? FileInfo.DEFAULT : navFile;
            navMap.groundOverlayList = Collections.unmodifiableList(groundOverlayList);
            navMap.markerList = Collections.unmodifiableList(markerList);
            navMap.polylineList = Collections.unmodifiableList(polylineList);

            return navMap;
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", tag='" + tag + '\'' +
                    ", scale=" + scale +
                    ", navFile=" + navFile +
                    ", groundOverlayList=" + groundOverlayList +
                    ", markerList=" + markerList +
                    ", polylineList=" + polylineList +
                    '}';
        }
    }
}