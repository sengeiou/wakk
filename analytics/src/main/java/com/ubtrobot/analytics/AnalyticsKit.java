package com.ubtrobot.analytics;

import android.app.Activity;
import android.app.Application;

import java.util.Map;

public class AnalyticsKit {

    private AnalyticsKit() {
    }

    public static void initialize(Application application) {

    }

    public static void setStrategy(Strategy strategy) {

    }

    public static Strategy getStrategy() {
        return null;
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
