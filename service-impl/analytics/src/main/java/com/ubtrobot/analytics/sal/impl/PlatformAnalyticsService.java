package com.ubtrobot.analytics.sal.impl;

import android.content.Context;

import com.ubtrobot.analytics.AnalyticsServiceImpl;
import com.ubtrobot.analytics.Event;
import com.ubtrobot.analytics.Strategy;
import com.ubtrobot.analytics.sal.AnalyticsService;

import java.util.concurrent.Executor;

public class PlatformAnalyticsService implements AnalyticsService {

    private static final String TAG = "PlatformAnalyticsService";

    private AnalyticsServiceImpl mAnalyticsService;
    private ShutdownAnalyticsService mShutdownAnalyticsService;

    public PlatformAnalyticsService(Context context, Executor executor,
                                    String appId, String appKey, String deviceId) {
        mAnalyticsService = new AnalyticsServiceImpl(context, executor, appId, appKey, deviceId);
        mShutdownAnalyticsService = new ShutdownAnalyticsService(context, appId, appKey, deviceId);
    }

    @Override
    public void enable(boolean enable) {
        mAnalyticsService.enable(enable);
    }

    @Override
    public void setStrategy(Strategy strategy) {
        mAnalyticsService.setStrategy(strategy);
    }

    @Override
    public Strategy getStrategy() {
        return mAnalyticsService.getStrategy();
    }

    @Override
    public void recordEvent(Event event) {
        mAnalyticsService.recordEvent(event);
    }

    @Override
    public void reportShutdownEvent() {
        mShutdownAnalyticsService.reportShutdownEvent();
    }

}
