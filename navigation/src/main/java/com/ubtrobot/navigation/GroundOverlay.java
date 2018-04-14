package com.ubtrobot.navigation;

/**
 * 地图地面叠层
 */
public class GroundOverlay {

    public static final GroundOverlay DEFAULT = new GroundOverlay("", "");

    private String image;
    private String remoteImage;

    public GroundOverlay(String image, String remoteImage) {
        if (image == null || remoteImage == null) {
            throw new IllegalArgumentException("Argument image or remoteImage is null.");
        }

        this.image = image;
        this.remoteImage = remoteImage;
    }

    public GroundOverlay(String remoteImage) {
        this("", remoteImage);
    }

    public String getImage() {
        return image;
    }

    public String getRemoteImage() {
        return remoteImage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroundOverlay that = (GroundOverlay) o;

        if (image != null ? !image.equals(that.image) : that.image != null) return false;
        return remoteImage != null ? remoteImage.equals(that.remoteImage) : that.remoteImage == null;
    }

    @Override
    public int hashCode() {
        int result = image != null ? image.hashCode() : 0;
        result = 31 * result + (remoteImage != null ? remoteImage.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GroundOverlay{" +
                "image='" + image + '\'' +
                ", remoteImage='" + remoteImage + '\'' +
                '}';
    }
}