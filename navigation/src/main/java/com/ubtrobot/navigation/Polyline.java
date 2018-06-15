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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Polyline polyline = (Polyline) o;

        if (id != null ? !id.equals(polyline.id) : polyline.id != null) return false;
        if (name != null ? !name.equals(polyline.name) : polyline.name != null) return false;
        if (description != null ? !description.equals(polyline.description) : polyline.description != null)
            return false;
        if (locationList != null ? !locationList.equals(polyline.locationList) : polyline.locationList != null)
            return false;
        return extension != null ? extension.equals(polyline.extension) : polyline.extension == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (locationList != null ? locationList.hashCode() : 0);
        result = 31 * result + (extension != null ? extension.hashCode() : 0);
        return result;
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

    public Builder toBuilder() {
        return new Builder(this);
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

        public Builder(Polyline polyline) {
            id = polyline.id;
            name = polyline.name;
            description = polyline.description;
            locationList.addAll(polyline.locationList);
            extension = polyline.extension;
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

        public Builder addLocation(int index, Location location) {
            if (index < 0 || index > locationList.size()) {
                throw new IllegalArgumentException("Index out of bounds.");
            }
            if (location == null) {
                throw new IllegalArgumentException("Argument location is null.");
            }

            locationList.add(index, location);
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
