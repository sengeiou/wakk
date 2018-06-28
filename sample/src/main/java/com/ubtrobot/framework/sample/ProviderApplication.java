package com.ubtrobot.framework.sample;

import android.app.Application;

import com.ubtrobot.analytics.sal.AnalyticsFactory;
import com.ubtrobot.analytics.sal.AnalyticsService;
import com.ubtrobot.analytics.sal.impl.PlatformAnalyticsService;
import com.ubtrobot.master.Master;

import java.util.concurrent.Executors;

public class ProviderApplication extends Application implements AnalyticsFactory {
//public class ProviderApplication extends Application{


    @Override
    public void onCreate() {
        super.onCreate();
        Master.initialize(this);
    }

    @Override
    public AnalyticsService createAnalyticsService() {
        return new PlatformAnalyticsService(this, Executors.newSingleThreadExecutor(),
                "100030021", "4e860a12638d45f392d8eec3a88b6f66","deviceId00000");
    }
}
