package com.ubtrobot.analytics.sal;

import com.ubtrobot.analytics.Analytics;

public interface AnalyticsService extends Analytics {

    void reportShutdownEvent();
}