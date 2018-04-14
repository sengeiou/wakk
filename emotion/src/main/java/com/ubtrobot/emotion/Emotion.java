package com.ubtrobot.emotion;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;

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
        if (context == null) {
            throw new IllegalArgumentException("Argument context is null.");
        }

        Context resApkContext = resApkContext(context);
        if (resApkContext == null) {
            throw new Resources.NotFoundException();
        }

        return context.getString(resource.getName());
    }

    private Context resApkContext(Context context) {
        try {
            return context.createPackageContext(resource.getPackageName(),
                    Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public Drawable getIcon(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Argument context is null.");
        }

        Context resApkContext = resApkContext(context);
        if (resApkContext == null) {
            throw new Resources.NotFoundException();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return resApkContext.getDrawable(resource.getIcon());
        } else {
            return resApkContext.getResources().getDrawable(resource.getIcon(), null);
        }
    }

    @Override
    public String toString() {
        return "Emotion{" +
                "id='" + id + '\'' +
                ", resource=" + resource +
                '}';
    }
}