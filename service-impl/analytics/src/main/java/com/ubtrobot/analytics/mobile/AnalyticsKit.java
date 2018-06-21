package com.ubtrobot.analytics.mobile;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.ubtrobot.analytics.Analytics;
import com.ubtrobot.analytics.AnalyticsServiceImpl;
import com.ubtrobot.analytics.Event;
import com.ubtrobot.analytics.Strategy;
import com.ubtrobot.analytics.ipc.AnalyticsConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class AnalyticsKit {

    private static volatile Analytics sAnalytics;

    private AnalyticsKit() {
    }

    public static void initialize(Context context, Executor executor,
                                  String appId, String appKey, String deviceId) {
        if (sAnalytics != null) {
            return;
        }

        synchronized (com.ubtrobot.analytics.AnalyticsKit.class) {
            if (sAnalytics != null) {
                return;
            }
            sAnalytics = new AnalyticsServiceImpl(context, executor, appId, appKey, deviceId);
        }
    }

    public static void setStrategy(Strategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy is null.");
        }

        checkAnalytics();
        sAnalytics.setStrategy(strategy);
    }

    private static void checkAnalytics() {
        if (sAnalytics == null) {
            throw new IllegalStateException(
                    "Please call com.ubtrobot.analytics.mobile.AnalyticsKit.initialize");
        }
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

    public static void recordEvent(String eventId, long duration) {
        recordEvent(eventId, duration, null);
    }

    public static void recordEvent(
            String eventId, long duration, Map<String, String> segmentation) {
        if (duration < 0) {
            throw new IllegalArgumentException("Argument duration < 0.");
        }

        String startTimeMillisKey = "startTimeMillis";
        segmentation = segmentation == null ? new HashMap<String, String>() : segmentation;

        if (!TextUtils.isEmpty(segmentation.get(startTimeMillisKey))) {
            throw new IllegalArgumentException("Argument segmentation key exist startTimeMillis.");
        }

        long currentTimeMillis = System.currentTimeMillis();
        long startTimeMillis = currentTimeMillis - duration;
        segmentation.put("startTimeMillis", String.valueOf(startTimeMillis / 1000));

        recordEvent(eventId, segmentation);
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