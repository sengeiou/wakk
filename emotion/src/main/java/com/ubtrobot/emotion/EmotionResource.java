package com.ubtrobot.emotion;

/**
 * 情绪的 Android 资源
 */
public class EmotionResource {

    public static final EmotionResource DEFAULT = new EmotionResource(
            "", 0, "", "");

    private String packageName;
    private int nameResource;
    private String name;
    private String iconUri;

    private EmotionResource(String packageName, int nameResource, String name, String iconUri) {
        if (packageName == null) {
            throw new IllegalArgumentException("Argument packageName is null.");
        }

        this.packageName = packageName;
        this.nameResource = nameResource;
        this.name = name;
        this.iconUri = iconUri;
    }

    public String getPackageName() {
        return packageName;
    }

    public int getNameResource() {
        return nameResource;
    }

    public String getName() {
        return name;
    }

    public String getIconUri() {
        return iconUri;
    }

    @Override
    public String toString() {
        return "EmotionResource{" +
                "packageName='" + packageName + '\'' +
                ", nameResource=" + nameResource +
                ", iconUri=" + iconUri +
                ", name='" + name + '\'' +
                '}';
    }

    public static class Builder {

        private String packageName;
        private int nameResource;
        private String name;
        private String iconUri;

        public Builder(String packageName, int nameResource) {
            this(packageName, nameResource, "", "");
        }

        public Builder(String packageName, String name) {
            this(packageName, 0, name, "");
        }

        public void setIconUri(String iconUri) {
            this.iconUri = iconUri;
        }

        public Builder(String packageName, int nameResource, String name, String iconUri) {
            if (packageName == null) {
                throw new IllegalArgumentException("Argument packageName is null.");
            }

            if (nameResource <= 0 && (name == null || name.length() == 0)) {
                throw new IllegalArgumentException(
                        "Argument name is null or name.length is 0 and nameResource is 0");
            }

            this.packageName = packageName;
            this.nameResource = nameResource;
            this.name = name;
            this.iconUri = iconUri;
        }

        public EmotionResource build() {
            return new EmotionResource(packageName, nameResource, name, iconUri);
        }
    }
}
