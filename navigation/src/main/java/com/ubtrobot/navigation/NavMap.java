package com.ubtrobot.navigation;

import android.text.TextUtils;

import com.ubtrobot.cache.CachedField;
import com.ubtrobot.io.FileInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 导航地图
 */
public class NavMap {

    private String id;
    private String name;
    private float scale;
    private List<GroundOverlay> groundOverlayList;
    private List<Marker> markerList;
    private List<Polyline> polylineList;
    private FileInfo navFile;
    private String extension;

    private final CachedField<Map<String, Marker>> mIdMarkerMap;
    private final CachedField<Map<String, List<Marker>>> mTagMarkerMap;
    private final CachedField<Map<String, Polyline>> mIdPolylineMap;

    private NavMap(String id, float scale) {
        this.id = id;
        this.scale = scale;

        mIdMarkerMap = new CachedField<>(new CachedField.FieldGetter<Map<String, Marker>>() {
            @Override
            public Map<String, Marker> get() {
                HashMap<String, Marker> markerMap = new HashMap<>();
                for (Marker marker : markerList) {
                    markerMap.put(marker.getId(), marker);
                }
                return markerMap;
            }
        });
        mTagMarkerMap = new CachedField<>(new CachedField.FieldGetter<Map<String, List<Marker>>>() {
            @Override
            public Map<String, List<Marker>> get() {
                HashMap<String, List<Marker>> markerMap = new HashMap<>();
                for (Marker marker : markerList) {
                    for (String markerTag : marker.getTagList()) {
                        List<Marker> markers = markerMap.get(markerTag);
                        if (markers == null) {
                            markers = new LinkedList<>();
                            markerMap.put(markerTag, markers);
                        }

                        markers.add(marker);
                    }
                }

                HashMap<String, List<Marker>> ret = new HashMap<>();
                for (Map.Entry<String, List<Marker>> entry : markerMap.entrySet()) {
                    ret.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
                }
                return ret;
            }
        });
        mIdPolylineMap = new CachedField<>(new CachedField.FieldGetter<Map<String, Polyline>>() {
            @Override
            public Map<String, Polyline> get() {
                HashMap<String, Polyline> polylineMap = new HashMap<>();
                for (Polyline polyline : polylineList) {
                    polylineMap.put(polyline.getId(), polyline);
                }

                return polylineMap;
            }
        });

    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
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

    public List<Marker> getMarkerList(String tag) {
        return mTagMarkerMap.get().get(tag);
    }

    public Marker getMarker(String id) {
        return mIdMarkerMap.get().get(id);
    }

    public List<Polyline> getPolylineList() {
        return polylineList;
    }

    public Polyline getPolyline(String id) {
        return mIdPolylineMap.get().get(id);
    }

    public FileInfo getNavFile() {
        return navFile;
    }

    public String getExtension() {
        return extension;
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public String toString() {
        return "NavMap{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", scale=" + scale +
                ", groundOverlayList=" + groundOverlayList +
                ", markerList=" + markerList +
                ", polylineList=" + polylineList +
                ", navFile=" + navFile +
                ", extension='" + extension + '\'' +
                '}';
    }

    public static class Builder {

        private String id;
        private String name;
        private float scale;
        private FileInfo navFile;
        private String extension;

        private List<GroundOverlay> groundOverlayList = new LinkedList<>();
        private LinkedList<Marker> markerList = new LinkedList<>();
        private LinkedList<Polyline> polylineList = new LinkedList<>();

        public Builder(NavMap map) {
            if (map == null) {
                throw new IllegalArgumentException("Argument map is null.");
            }

            id = map.getId();
            name = map.getName();
            scale = map.scale;
            navFile = map.getNavFile();
            extension = map.getExtension();

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

        public Builder setNavFile(FileInfo navFile) {
            if (navFile == null) {
                throw new IllegalArgumentException("Argument navFile is null.");
            }

            this.navFile = navFile;
            return this;
        }

        public Builder setExtension(String extension) {
            this.extension = extension;
            return this;
        }

        public Builder addMarkerList(List<Marker> markers) {
            if (markers == null) {
                throw new IllegalArgumentException("Argument markers is null.");
            }

            markerList.addAll(markers);
            return this;
        }

        public Builder setMarkerList(List<Marker> markers) {
            if (markers == null) {
                throw new IllegalArgumentException("Argument markers is null.");
            }

            markerList.clear();
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

        public Builder addGroundOverlayList(List<GroundOverlay> overlays) {
            if (overlays == null) {
                throw new IllegalArgumentException("Argument overlays is null.");
            }

            groundOverlayList.addAll(overlays);
            return this;
        }

        public Builder setGroundOverlayList(List<GroundOverlay> overlays) {
            if (overlays == null) {
                throw new IllegalArgumentException("Argument overlays is null.");
            }

            groundOverlayList.clear();
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
            navMap.navFile = navFile == null ? FileInfo.DEFAULT : navFile;
            navMap.extension = extension == null ? "" : extension;
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
                    ", scale=" + scale +
                    ", navFile=" + navFile +
                    ", extension='" + extension + '\'' +
                    ", groundOverlayList=" + groundOverlayList +
                    ", markerList=" + markerList +
                    ", polylineList=" + polylineList +
                    '}';
        }
    }
}