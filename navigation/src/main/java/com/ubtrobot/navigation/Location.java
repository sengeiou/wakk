package com.ubtrobot.navigation;

/**
 * 地图标记物
 */
public class Location {

    public static final Location DEFAULT = new Location.Builder(LatLng.DEFAULT).build();

    private LatLng position;
    private double elevation;
    private float rotation;

    protected Location(LatLng position) {
        this.position = position;
    }

    protected Location(GenericBuilder builder) {
        position = builder.position;
        elevation = builder.elevation;
        rotation = builder.rotation;
    }

    public LatLng getPosition() {
        return position;
    }

    public double getElevation() {
        return elevation;
    }

    public float getRotation() {
        return rotation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        if (Double.compare(location.elevation, elevation) != 0) return false;
        if (Float.compare(location.rotation, rotation) != 0) return false;
        return position != null ? position.equals(location.position) : location.position == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = position != null ? position.hashCode() : 0;
        temp = Double.doubleToLongBits(elevation);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (rotation != +0.0f ? Float.floatToIntBits(rotation) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Location{" +
                "position=" + position +
                ", elevation=" + elevation +
                ", rotation=" + rotation +
                '}';
    }

    public static class Builder extends GenericBuilder<Builder> {

        public Builder(LatLng position) {
            super(position);
        }
    }

    protected static class GenericBuilder<T extends GenericBuilder<T>> {

        private LatLng position;
        private double elevation;
        private float rotation;

        public GenericBuilder(LatLng position) {
            if (position == null) {
                throw new IllegalArgumentException("Argument position is null.");
            }

            this.position = position;
        }

        public T setElevation(double elevation) {
            this.elevation = elevation;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T setRotation(float rotation) {
            this.rotation = rotation;
            return (T) this;
        }

        public Location build() {
            Location location = new Location(position);
            location.elevation = elevation;
            location.rotation = rotation;
            return location;
        }
    }
}