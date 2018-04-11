package com.ubtrobot.analytics;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.ubtrobot.analytics.ipc.AnalyticsConstants;

import java.util.Map;

public class AnalyticsKit {

    private static volatile Analytics sAnalytics;

    private AnalyticsKit() {
    }

    public static void initialize(Context context) {
        if (sAnalytics != null) {
            return;
        }

        synchronized (AnalyticsKit.class) {
            if (sAnalytics != null) {
                return;
            }

            Class<Analytics> cls = getDelegateAnalyticsCls(AnalyticsConstants.DELEGATE_ANALYTICS_SERVICE_NAME);

            if (cls != null) {
                sAnalytics = new AnalyticsDelegate(cls);
            } else {
                Uri uri = Uri.parse(AnalyticsConstants.PROVIDER_URI);
                ContentResolver resolver = context.getContentResolver();
                try {
                    Bundle bundle = resolver.call(uri, AnalyticsConstants.CALL_METHOD_PING, null, null);
                    if (bundle == null || !(bundle.getBoolean(AnalyticsConstants.KEY_PROVIDER_PONG))) {
                        throw new IllegalStateException("Provider illegal.");
                    }
                } catch (IllegalArgumentException e) {
                    throw new IllegalStateException("Pleases install AnalyticsSystemService.");
                }

                sAnalytics = new ProviderAnalyticsProxy(resolver);
            }
        }
    }

    private static void checkAnalytics() {
        if (sAnalytics == null) {
            throw new IllegalStateException("Please call com.ubtrobot.analytics.AnalyticsKit.initialize");
        }
    }

    private static Class<Analytics> getDelegateAnalyticsCls(String analyticsServiceName) {
        if (analyticsServiceName == null || analyticsServiceName.length() == 0) {
            return null;
        }

        Class<Analytics> cls;
        try {
            cls = (Class<Analytics>) Class.forName(analyticsServiceName);
        } catch (ClassNotFoundException e) {
            cls = null;
        }

        return cls;
    }

    public static void setStrategy(Strategy strategy) {
        checkAnalytics();
        sAnalytics.setStrategy(strategy);
    }

    public static Strategy getStrategy() {
        checkAnalytics();
        return sAnalytics.getStrategy();
    }

    public void enable(boolean enable) {
        checkAnalytics();
        sAnalytics.enable(enable);
    }

    public static void recordEvent(String eventId) {
    }

    public static void recordEvent(String eventId, Map<String, String> customSegmentation) {
    }

    public static void recordPageStart(Activity activity) {
    }

    public static void recordPageStart(Activity activity, String eventId) {
    }

    public static void recordPageStart(String fragmentName) {
    }

    public static void recordPageStart(String fragmentName, String eventId) {
    }

    public static void recordPageStop(Activity activity) {
    }

}
