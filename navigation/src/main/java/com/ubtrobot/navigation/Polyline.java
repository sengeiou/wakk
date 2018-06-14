package com.ubtrobot.navigation;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Polyline {

    private String id;
    private String name;
    private String description;
    private List<Location> locationList;
    private String extension;

    private Polyline(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<Location> getLocationList() {
        return locationList;
    }

    public String getExtension() {
        return extension;
    }

    @Override
    public String toString() {
        return "Polyline{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", locationList=" + locationList +
                ", extension='" + extension + '\'' +
                '}';
    }

    public static class Builder {

        private String id;
        private String name;
        private String description;
        private LinkedList<Location> locationList = new LinkedList<>();
        private String extension;

        public Builder(String id) {
            this.id = id;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder addLocationList(List<Location> locationList) {
            if (locationList == null) {
                throw new IllegalArgumentException("Argument locations is null.");
            }

            this.locationList.addAll(locationList);
            return this;
        }

        public Builder setLocationList(List<Location> locationList) {
            if (locationList == null) {
                throw new IllegalArgumentException("Argument locations is null.");
            }

            this.locationList.clear();
            this.locationList.addAll(locationList);
            return this;
        }

        public Builder addLocation(Location location) {
            if (location == null) {
                throw new IllegalArgumentException("Argument location is null.");
            }

            locationList.add(location);
            return this;
        }

        public Builder setLocation(int index, Location location) {
            if (index < 0 || index >= locationList.size()) {
                throw new IllegalArgumentException("Index out of bounds.");
            }
            if (location == null) {
                throw new IllegalArgumentException("Argument location is null.");
            }

            locationList.set(index, location);
            return this;
        }

        public Builder removeLocation(int index) {
            if (index < 0 || index >= locationList.size()) {
                throw new IllegalArgumentException("Index out of bounds.");
            }

            locationList.remove(index);
            return this;
        }

        public Builder setExtension(String extension) {
            this.extension = extension;
            return this;
        }

        public Polyline build() {
            Polyline polyline = new Polyline(id == null ? "" : id);
            polyline.name = name;
            polyline.description = description;
            polyline.locationList = Collections.unmodifiableList(locationList);
            polyline.extension = extension == null ? "" : extension;
            return polyline;
        }
    }
}
