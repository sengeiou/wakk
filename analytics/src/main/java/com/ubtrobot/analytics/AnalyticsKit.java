package com.ubtrobot.analytics;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;

import com.ubtrobot.analytics.ipc.AnalyticsConstants;

import java.util.HashMap;
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

            ContentResolver resolver = context.getContentResolver();
            sAnalytics = new ProviderAnalyticsProxy(resolver);
        }
    }

    private static void checkAnalytics() {
        if (sAnalytics == null) {
            throw new IllegalStateException(
                    "Please call com.ubtrobot.analytics.AnalyticsKit.initialize");
        }
    }

    public static Strategy getStrategy() {
        checkAnalytics();
        return sAnalytics.getStrategy();
    }

    public static void recordEvent(String eventId) {
        recordEvent(eventId, null);
    }

    public static void recordEvent(String eventId, Map<String, String> segmentation) {
        checkAnalytics();

        if (eventId == null || eventId.length() <= 0 ||
                eventId.length() > AnalyticsConstants.ID_MAX_LENGTH) {
            throw new IllegalArgumentException("Illegal id argument. 0 < id.length <= 64.");
        }

        Event event = new Event.Builder(eventId, AnalyticsConstants.CUSTOM_EVENT).
                setCustomSegmentation(segmentation).build();

        sAnalytics.recordEvent(event);
    }

    public static void recordActivityStart(Activity activity) {
        recordEvent(AnalyticsConstants.EVENT_ID_DEF_ACTIVITY_START,
                createSegmentation(activity, AnalyticsConstants.EVENT_TYPE_ACTIVITY));
    }

    public static void recordActivityStart(String activityName) {
        checkString("activityName", activityName);

        recordEvent(AnalyticsConstants.EVENT_ID_DEF_ACTIVITY_START,
                createSegmentation(activityName, AnalyticsConstants.EVENT_TYPE_ACTIVITY));
    }

    private static void checkString(String stringName, String stringValue) {
        if (stringValue == null || stringValue.length() <= 0) {
            throw new IllegalArgumentException(
                    "Argument:" + stringName + " is null.");
        }
    }

    public static void recordFragmentStart(String fragmentName) {
        checkString("fragmentName", fragmentName);

        recordEvent(AnalyticsConstants.EVENT_ID_DEF_FRAGMENT_START,
                createSegmentation(fragmentName, AnalyticsConstants.EVENT_TYPE_FRAGMENT));
    }

    public static void recordActivityStop(Activity activity) {
        recordEvent(AnalyticsConstants.EVENT_ID_DEF_ACTIVITY_STOP,
                createSegmentation(activity, AnalyticsConstants.EVENT_TYPE_ACTIVITY));
    }

    public static void recordActivityStop(String activityName) {
        checkString("activityName", activityName);

        recordEvent(AnalyticsConstants.EVENT_ID_DEF_ACTIVITY_STOP,
                createSegmentation(activityName, AnalyticsConstants.EVENT_TYPE_ACTIVITY));
    }

    public static void recordFragmentStop(String fragmentName) {
        checkString("fragmentName", fragmentName);

        recordEvent(AnalyticsConstants.EVENT_ID_DEF_FRAGMENT_STOP,
                createSegmentation(fragmentName, AnalyticsConstants.EVENT_TYPE_FRAGMENT));
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
