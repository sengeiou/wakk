package com.ubtrobot.navigation;

/**
 * 地图标记物
 */
public class Marker extends Location {

    public static final Marker DEFAULT = new Marker.Builder("", Position.DEFAULT).build();

    private String title;

    protected Marker(String title, Position position) {
        super(position);
        this.title = title;
    }

    public Marker(Builder builder) {
        super(builder);
        title = builder.title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Marker marker = (Marker) o;

        return title != null ? title.equals(marker.title) : marker.title == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (title != null ? title.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Marker{" +
                "title='" + title + '\'' +
                "position=" + getPosition() +
                ", z=" + getZ() +
                ", rotation=" + getRotation() +
                '}';
    }

    public static class Builder extends Location.GenericBuilder<Builder> {

        private final String title;

        public Builder(String title, Position position) {
            super(position);

            if (title == null) {
                throw new IllegalArgumentException("Argument title is null.");
            }
            this.title = title;
        }

        @Override
        public Marker build() {
            return new Marker(this);
        }
    }
}