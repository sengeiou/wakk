package com.ubtrobot.navigation;

/**
 * 地图地面叠层
 */
public class GroundOverlay {

    public static final GroundOverlay DEFAULT = new Builder().build();

    private String name;
    private String tag;
    private int width;
    private int height;
    private Point originInImage;
    private String imageUri;
    private String remoteImageUri;

    private GroundOverlay(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return tag;
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

    public String getImageUri() {
        return imageUri;
    }

    public String getRemoteImageUri() {
        return remoteImageUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroundOverlay that = (GroundOverlay) o;

        if (width != that.width) return false;
        if (height != that.height) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (tag != null ? !tag.equals(that.tag) : that.tag != null) return false;
        if (originInImage != null ? !originInImage.equals(that.originInImage) : that.originInImage != null)
            return false;
        if (imageUri != null ? !imageUri.equals(that.imageUri) : that.imageUri != null)
            return false;
        return remoteImageUri != null ? remoteImageUri.equals(that.remoteImageUri) : that.remoteImageUri == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (tag != null ? tag.hashCode() : 0);
        result = 31 * result + width;
        result = 31 * result + height;
        result = 31 * result + (originInImage != null ? originInImage.hashCode() : 0);
        result = 31 * result + (imageUri != null ? imageUri.hashCode() : 0);
        result = 31 * result + (remoteImageUri != null ? remoteImageUri.hashCode() : 0);
        return result;
    }

    public static class Builder {

        private String name;
        private String tag;
        private int width;
        private int height;
        private Point originInImage;
        private String imageUri;
        private String remoteImageUri;

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

        public Builder setTag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder setOriginInImage(Point originInImage) {
            if (originInImage == null) {
                throw new IllegalArgumentException("Argument originInImage is null.");
            }

            this.originInImage = originInImage;
            return this;
        }

        public Builder setImageUri(String imageUri) {
            this.imageUri = imageUri;
            return this;
        }

        public Builder setRemoteImageUri(String remoteImageUri) {
            this.remoteImageUri = remoteImageUri;
            return this;
        }

        public GroundOverlay build() {
            GroundOverlay overlay = new GroundOverlay(width, height);
            overlay.name = name == null ? "" : name;
            overlay.tag = tag == null ? "" : tag;
            overlay.originInImage = originInImage;
            overlay.imageUri = imageUri == null ? "" : imageUri;
            overlay.remoteImageUri = remoteImageUri == null ? "" : remoteImageUri;
            return overlay;
        }
    }
}