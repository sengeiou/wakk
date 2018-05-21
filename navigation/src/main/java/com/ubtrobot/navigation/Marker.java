package com.ubtrobot.navigation;

import android.text.TextUtils;

/**
 * 地图标记物
 */
public class Marker extends Location {

    public static final Marker DEFAULT = new Marker.Builder("DEFAULT_ID", Point.DEFAULT).build();

    private String id;
    private String title;
    private String tag;
    private String description;

    protected Marker(Point position) {
        super(position);
    }

    private Marker(Builder builder) {
        super(builder);
        id = builder.id;
        title = builder.title == null ? "" : builder.title;
        tag = builder.tag == null ? "" : builder.tag;
        description = builder.description == null ? "" : builder.description;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getTag() {
        return tag;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Marker marker = (Marker) o;

        if (id != null ? !id.equals(marker.id) : marker.id != null) return false;
        if (title != null ? !title.equals(marker.title) : marker.title != null) return false;
        if (tag != null ? !tag.equals(marker.tag) : marker.tag != null) return false;
        return description != null ? description.equals(marker.description) : marker.description == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (tag != null ? tag.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Marker{" +
                "id='" + id + '\'' +
                "position=" + getPosition() +
                ", z=" + getZ() +
                ", rotation=" + getRotation() +
                ", title='" + title + '\'' +
                ", tag='" + tag + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    public static class Builder extends Location.GenericBuilder<Builder> {

        private String id;
        private String title;
        private String tag;
        private String description;

        public Builder(String id, Point position) {
            super(position);
            if (TextUtils.isEmpty(id)) {
                throw new IllegalArgumentException("Argument id is an empty string.");
            }
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setTag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        @Override
        public Marker build() {
            return new Marker(this);
        }
    }
}