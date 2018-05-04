package com.ubtrobot.emotion;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * 情绪
 */
public class Emotion {

    private static final String TAG = "Emotion";

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
        return getResApkContext(context).getString(resource.getNameResource());
    }

    private Context getResApkContext(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Argument context is null.");
        }

        Context resApkContext = resApkContext(context);
        if (resApkContext == null) {
            throw new Resources.NotFoundException();
        }

        return resApkContext;
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
        Context resApkContext = getResApkContext(context);

        return new BitmapDrawable(resApkContext.getResources(),
                getAssetsInputStream(resApkContext, resource.getIconUri()));
    }

    private InputStream getAssetsInputStream(Context context, String uri) {
        if (context == null) {
            throw new IllegalArgumentException("Argument context is null.");
        }

        Context resApkContext = resApkContext(context);

        try {
            return resApkContext.getAssets().open(uri);
        } catch (IOException e) {
            // 不处理: 不能正常获取资源时，会抛异常
            Log.e(TAG, "Please check uri : " + uri);
        }

        throw new Resources.NotFoundException();
    }

    @Override
    public String toString() {
        return "Emotion{" +
                "id='" + id + '\'' +
                ", resource=" + resource +
                '}';
    }

}