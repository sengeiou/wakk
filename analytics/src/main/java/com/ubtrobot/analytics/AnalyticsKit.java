package com.ubtrobot.analytics;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.ubtrobot.analytics.ipc.AnalyticsConstants;

import java.util.HashMap;
import java.util.Map;

public class AnalyticsKit {

    private static final int ID_MAX_LENGTH = 64;

    private static final String CUSTOM_EVENT = "custom_event";

    private static final String EVENT_TYPE_ACTIVITY = "activity";                // 页面的类型：activity
    private static final String EVENT_TYPE_FRAGMENT = "fragment";                // 页面类型：fragment

    private static final String EVENT_ID_DEF_ACTIVITY_START = "activityStart";   // activity启动时，默认的eventId
    private static final String EVENT_ID_DEF_ACTIVITY_STOP = "activityStop";     // activity退出时，默认的eventId
    private static final String EVENT_ID_DEF_FRAGMENT_START = "fragmentStart";   // fragment展示时，默认的eventId
    private static final String EVENT_ID_DEF_FRAGMENT_STOP = "fragmentStop";     // fragment退出前台时，默认的eventId

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

    public static void enable(boolean enable) {
        checkAnalytics();
        sAnalytics.enable(enable);
    }

    public static void recordEvent(String eventId) {
        recordEvent(eventId, null);
    }

    public static void recordEvent(String eventId, Map<String, String> segmentation) {
        checkAnalytics();

        if (eventId == null || eventId.length() <= 0 || eventId.length() > ID_MAX_LENGTH) {
            throw new IllegalArgumentException("Illegal id argument. 0 < id.length <= 64.");
        }

        Event event = new Event.Builder(eventId, CUSTOM_EVENT).setCustomSegmentation(segmentation).build();

        sAnalytics.recordEvent(event);
    }

    public static void recordActivityStart(Activity activity) {
        recordEvent(EVENT_ID_DEF_ACTIVITY_START, createSegmentation(activity, EVENT_TYPE_ACTIVITY));
    }

    public static void recordActivityStart(String activityName) {
        checkString("activityName", activityName);

        recordEvent(EVENT_ID_DEF_ACTIVITY_START, createSegmentation(activityName, EVENT_TYPE_ACTIVITY));
    }

    private static void checkString(String stringName, String stringValue) {
        if (stringValue == null || stringValue.length() <= 0) {
            throw new IllegalArgumentException(
                    "Argument:" + stringName + " is null.");
        }
    }

    public static void recordFragmentStart(String fragmentName) {
        checkString("fragmentName", fragmentName);

        recordEvent(EVENT_ID_DEF_FRAGMENT_START, createSegmentation(fragmentName, EVENT_TYPE_FRAGMENT));
    }

    public static void recordActivityStop(Activity activity) {
        recordEvent(EVENT_ID_DEF_ACTIVITY_STOP, createSegmentation(activity, EVENT_TYPE_ACTIVITY));
    }

    public static void recordActivityStop(String activityName) {
        checkString("activityName", activityName);

        recordEvent(EVENT_ID_DEF_ACTIVITY_STOP, createSegmentation(activityName, EVENT_TYPE_ACTIVITY));
    }

    public static void recordFragmentStop(String fragmentName) {
        checkString("fragmentName", fragmentName);

        recordEvent(EVENT_ID_DEF_FRAGMENT_STOP, createSegmentation(fragmentName, EVENT_TYPE_FRAGMENT));
    }

    private static Map<String, String> createSegmentation(Object page, String pageType) {
        Map<String, String> segmentation = new HashMap<>();
        segmentation.put("_pageType", pageType);

        if (page instanceof String) {
            segmentation.put("_pageName", String.valueOf(page));
        } else {
            segmentation.put("_pageName", page.getClass().getName());
        }

        return segmentation;
    }

}
