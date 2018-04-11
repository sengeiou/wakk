package com.ubtrobot.analytics.ipc.provider;

import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ubtrobot.analytics.Analytics;
import com.ubtrobot.analytics.ipc.AnalyticsConstants;
import com.ubtrobot.analytics.sal.AnalyticsFactory;

public class AnalyticsProvider extends ContentProvider {

    private Analytics mAnalytics;

    @Override
    public boolean onCreate() {
        mAnalytics = getAnalytics(getContext());

        return true;
    }

    private Analytics getAnalytics(Context context) {
        Application application = (Application) context.getApplicationContext();

        if (!(application instanceof AnalyticsFactory)) {
            throw new IllegalStateException("Your application implements com.ubtrobot.analytics.sal.AnalyticsFactory.");
        }

        AnalyticsFactory factory = (AnalyticsFactory) application;
        Analytics analytics = factory.createAnalyticsService();

        if (analytics == null) {
            throw new IllegalStateException("Your application createAnalyticsService return is null.");
        }

        return analytics;
    }


    @Nullable
    @Override
    public Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras) {
        Bundle bundle = null;

        switch (method) {
            case AnalyticsConstants.CALL_METHOD_PING:
                ping();
                break;
        }

        return bundle;
    }

    private Bundle ping() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(AnalyticsConstants.KEY_PROVIDER_PONG, true);
        return bundle;
    }

    @Nullable
    @Override
    public Cursor query(
            @NonNull Uri uri,
            @Nullable String[] projection,
            @Nullable String selection,
            @Nullable String[] selectionArgs,
            @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int
    delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(
            @NonNull Uri uri,
            @Nullable ContentValues values,
            @Nullable String selection,
            @Nullable String[] selectionArgs) {
        return 0;
    }
}
