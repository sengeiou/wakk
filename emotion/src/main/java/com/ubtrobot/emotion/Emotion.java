package com.ubtrobot.emotion;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.IOException;
import java.io.InputStream;

/**
 * 情绪
 */
public class Emotion {

    public static final Emotion DEFAULT = new Emotion("", EmotionResource.DEFAULT);

    private String id;
    private EmotionResource resource;

    public Emotion(String id, EmotionResource resource) {
        if (id == null || resource == null) {
            throw new IllegalArgumentException("Argument id or resource is null.");
        }

        this.id = id;
        this.resource = resource;
    }

    public String getId() {
        return id;
    }

    public EmotionResource getResource() {
        return resource;
    }

    public String getName(Context context) {
        Context resApkContext = getResApkContext(context);
        return resApkContext != null ? resApkContext.getString(resource.getNameResource()) : "";
    }

    private Context getResApkContext(Context context) {
        if (context != null) {
            try {
                return context.createPackageContext(resource.getPackageName(),
                        Context.CONTEXT_IGNORE_SECURITY);
            } catch (PackageManager.NameNotFoundException e) {
                // 不处理，因为最终返回是空
            }
        }

        return null;
    }

    public Drawable getIcon(Context context) {
        Context resApkContext = getResApkContext(context);

        return resApkContext != null ? new BitmapDrawable(resApkContext.getResources(),
                getAssetsInputStream(resApkContext, resource.getIconUri())) : null;
    }

    private InputStream getAssetsInputStream(Context context, String uri) {
        if (context == null) {
            throw new IllegalArgumentException("Argument context is null.");
        }

        Context resApkContext = getResApkContext(context);

        try {
            return resApkContext != null ? resApkContext.getAssets().open(uri) : null;
        } catch (IOException e) {
            // 不处理，出错的话，最终返回 null
        }

        return null;
    }

    @Override
    public String toString() {
        return "Emotion{" +
                "id='" + id + '\'' +
                ", resource=" + resource +
                '}';
    }
}