package com.ubtrobot.navigation;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 地图标记物
 */
public class Marker extends Location {

    public static final Marker DEFAULT = new Marker.Builder("", Point.DEFAULT).build();

    private String id;
    private String title;
    private String description;
    private List<String> tagList;
    private String extension;

    protected Marker(Point position) {
        super(position);
    }

    private Marker(Builder builder) {
        super(builder);
        id = builder.id == null ? "" : builder.id;
        title = builder.title == null ? "" : builder.title;
        description = builder.description == null ? "" : builder.description;

        tagList = Collections.unmodifiableList(builder.tagList);
        extension = builder.extension == null ? "" : builder.extension;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTagList() {
        return tagList;
    }

    public String getExtension() {
        return extension;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Marker marker = (Marker) o;

        if (id != null ? !id.equals(marker.id) : marker.id != null) return false;
        if (title != null ? !title.equals(marker.title) : marker.title != null) return false;
        if (description != null ? !description.equals(marker.description) : marker.description != null)
            return false;
        if (tagList != null ? !tagList.equals(marker.tagList) : marker.tagList != null)
            return false;
        return extension != null ? extension.equals(marker.extension) : marker.extension == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (tagList != null ? tagList.hashCode() : 0);
        result = 31 * result + (extension != null ? extension.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Marker{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", tagList=" + tagList +
                ", extension='" + extension + '\'' +
                '}';
    }

    public Builder toMarkerBuilder() {
        return new Builder(this);
    }

    public static class Builder extends Location.GenericBuilder<Builder> {

        private String id;
        private String title;
        private String description;
        private LinkedList<String> tagList = new LinkedList<>();
        private String extension;

        public Builder(String id, Point position) {
            super(position);
            this.id = id;
        }

        public Builder(Point position) {
            super(position);
        }

        public Builder(Marker marker) {
            super(marker.getPosition());
            setZ(marker.getZ());
            setRotation(marker.getRotation());

            id = marker.id;
            title = marker.title;
            description = marker.getDescription();
            tagList.addAll(marker.getTagList());
            extension = marker.getExtension();
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder addTagList(List<String> tagList) {
            if (tagList == null) {
                throw new IllegalArgumentException("Argument tagList is null.");
            }

            this.tagList.addAll(tagList);
            return this;
        }

        public Builder setTagList(List<String> tagList) {
            if (tagList == null) {
                throw new IllegalArgumentException("Argument tagList is null.");
            }

            this.tagList.clear();
            this.tagList.addAll(tagList);
            return this;
        }

        public Builder addTag(String tag) {
            if (tag == null) {
                throw new IllegalArgumentException("Argument tag is null.");
            }

            tagList.add(tag);
            return this;
        }

        public Builder addTag(int index, String tag) {
            if (index < 0 || index > tagList.size()) {
                throw new IllegalArgumentException("Index out of bounds.");
            }
            if (tag == null) {
                throw new IllegalArgumentException("Argument tag is null.");
            }

            tagList.add(index, tag);
            return this;
        }

        public Builder setTag(int index, String tag) {
            if (index < 0 || index >= tagList.size()) {
                throw new IllegalArgumentException("Index out of bounds.");
            }
            if (tag == null) {
                throw new IllegalArgumentException("Argument tag is null.");
            }

            tagList.set(index, tag);
            return this;
        }

        public Builder removeTag(int index) {
            if (index < 0 || index >= tagList.size()) {
                throw new IllegalArgumentException("Index out of bounds.");
            }

            tagList.remove(index);
            return this;
        }

        public void setExtension(String extension) {
            if (extension == null) {
                throw new IllegalArgumentException("Argument extension is null.");
            }

            this.extension = extension;
        }

        @Override
        public Marker build() {
            return new Marker(this);
        }
    }
}