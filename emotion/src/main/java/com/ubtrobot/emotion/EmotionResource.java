package com.ubtrobot.emotion;

/**
 * 情绪的 Android 资源
 */
public class EmotionResource {

    public static final EmotionResource DEFAULT = new EmotionResource("", 0);

    private String packageName;
    private int name;
    private int icon;

    public EmotionResource(String packageName, int name) {
        this(packageName, name, 0);
    }

    public EmotionResource(String packageName, int name, int icon) {
        if (packageName == null) {
            throw new IllegalArgumentException("Argument packageName is null.");
        }

        this.packageName = packageName;
        this.name = name;
        this.icon = icon;
    }

    public String getPackageName() {
        return packageName;
    }

    public int getName() {
        return name;
    }

    public int getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return "EmotionResource{" +
                "packageName='" + packageName + '\'' +
                ", name=" + name +
                ", icon=" + icon +
                '}';
    }
}
