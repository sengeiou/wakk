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
import android.util.Log;

import com.ubtrobot.analytics.Event;
import com.ubtrobot.analytics.Strategy;
import com.ubtrobot.analytics.ipc.AnalyticsConstants;
import com.ubtrobot.analytics.sal.AnalyticsFactory;
import com.ubtrobot.analytics.sal.AnalyticsService;

public class AnalyticsProvider extends ContentProvider {

    private static final String TAG = "AnalyticsProvider";

    private AnalyticsService mAnalytics;

    @Override
    public boolean onCreate() {
        mAnalytics = getAnalytics(getContext());

        return true;
    }

    private AnalyticsService getAnalytics(Context context) {
        Application application = (Application) context.getApplicationContext();

        if (!(application instanceof AnalyticsFactory)) {
//            throw new IllegalStateException("Your application implements com.ubtrobot.analytics.sal.AnalyticsFactory.");
            return null;
        }

        AnalyticsFactory factory = (AnalyticsFactory) application;
        AnalyticsService analytics = factory.createAnalyticsService();

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
                bundle = ping();
                break;
            case AnalyticsConstants.CALL_METHOD_SET_STRATEGY:
                setStrategy(extras);
                break;
            case AnalyticsConstants.CALL_METHOD_GET_STRATEGY:
                bundle = getStrategy();
                break;
            case AnalyticsConstants.CALL_METHOD_ENABLE:
                enable(extras);
                break;
            case AnalyticsConstants.CALL_METHOD_RECORD_EVENT:
                recordEvent(extras);
                break;
            case AnalyticsConstants.CALL_METHOD_SHUTDOWN:
                reportedShutdownEvent();
                break;
        }

        return bundle;
    }

    private void reportedShutdownEvent() {
        mAnalytics.reportShutdownEvent();
    }

    private void recordEvent(Bundle extras) {
        if (extras == null) {
            Log.w(TAG, "Argument(extras) is null.");
            return;
        }

        extras.setClassLoader(getClass().getClassLoader());
        Event event = extras.getParcelable(AnalyticsConstants.KEY_RECORD_EVENT);

        mAnalytics.recordEvent(event);
    }

    private void enable(Bundle extras) {
        if (extras == null) {
            Log.w(TAG, "Argument(extras) is null.");
            return;
        }

        boolean enable = extras.getBoolean(AnalyticsConstants.KEY_ENABLE, true);
        mAnalytics.enable(enable);
    }

    private void setStrategy(Bundle extras) {
        if (extras == null) {
            Log.w(TAG, "Argument(extras) is null.");
            return;
        }

        extras.setClassLoader(getClass().getClassLoader());
        Strategy strategy = extras.getParcelable(AnalyticsConstants.KEY_STRATEGY);
        if (strategy == null) {
            Log.w(TAG, "Get strategy is null.");
            return;
        }

        mAnalytics.setStrategy(strategy);
    }

    private Bundle getStrategy() {
        Strategy strategy = mAnalytics.getStrategy();
        Bundle bundle = new Bundle();
        bundle.putParcelable(AnalyticsConstants.KEY_STRATEGY, strategy);
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
