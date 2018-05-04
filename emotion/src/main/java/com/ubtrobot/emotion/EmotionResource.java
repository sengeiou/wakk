package com.ubtrobot.emotion;

/**
 * 情绪的 Android 资源
 */
public class EmotionResource {

    public static final EmotionResource DEFAULT = new EmotionResource("", 0);

    private String packageName;
    private int nameResource;
    private String iconUri;

    public EmotionResource(String packageName, int nameResource) {
        this(packageName, nameResource, "");
    }

    public EmotionResource(String packageName, int nameResource, String iconUri) {
        if (packageName == null) {
            throw new IllegalArgumentException("Argument packageName is null.");
        }

        this.packageName = packageName;
        this.nameResource = nameResource;
        this.iconUri = iconUri;
    }

    public String getPackageName() {
        return packageName;
    }

    public int getNameResource() {
        return nameResource;
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
                '}';
    }
}
