package com.ubtrobot.navigation;

import com.ubtrobot.io.FileInfo;

/**
 * 地图地面叠层
 */
public class GroundOverlay {

    public static final GroundOverlay DEFAULT = new Builder().build();

    private String name;
    private String type;
    private int width;
    private int height;
    private Point originInImage;
    private FileInfo image;

    private GroundOverlay(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Point getOriginInImage() {
        return originInImage;
    }

    public FileInfo getImage() {
        return image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroundOverlay overlay = (GroundOverlay) o;

        if (width != overlay.width) return false;
        if (height != overlay.height) return false;
        if (name != null ? !name.equals(overlay.name) : overlay.name != null) return false;
        if (type != null ? !type.equals(overlay.type) : overlay.type != null) return false;
        if (originInImage != null ? !originInImage.equals(overlay.originInImage) : overlay.originInImage != null)
            return false;
        return image != null ? image.equals(overlay.image) : overlay.image == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + width;
        result = 31 * result + height;
        result = 31 * result + (originInImage != null ? originInImage.hashCode() : 0);
        result = 31 * result + (image != null ? image.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GroundOverlay{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", originInImage=" + originInImage +
                ", image=" + image +
                '}';
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static class Builder {

        private String name;
        private String type;
        private int width;
        private int height;
        private Point originInImage;
        private FileInfo image;

        public Builder() {
        }

        public Builder(GroundOverlay groundOverlay) {
            name = groundOverlay.name;
            type = groundOverlay.type;
            width = groundOverlay.width;
            height = groundOverlay.height;
            originInImage = groundOverlay.originInImage;
            image = groundOverlay.image;
        }

        public Builder setWidth(int width) {
            if (width <= 0) {
                throw new IllegalArgumentException("Argument width <= 0");
            }

            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            if (height <= 0) {
                throw new IllegalArgumentException("Argument height <= 0");
            }

            this.height = height;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setOriginInImage(Point originInImage) {
            if (originInImage == null) {
                throw new IllegalArgumentException("Argument originInImage is null.");
            }

            this.originInImage = originInImage;
            return this;
        }

        public Builder setImage(FileInfo image) {
            if (image == null) {
                throw new IllegalArgumentException("Argument image is null.");
            }

            this.image = image;
            return this;
        }

        public GroundOverlay build() {
            GroundOverlay overlay = new GroundOverlay(width, height);
            overlay.name = name == null ? "" : name;
            overlay.type = type == null ? "" : type;
            overlay.originInImage = originInImage;
            overlay.image = image == null ? FileInfo.DEFAULT : image;
            return overlay;
        }
    }
}