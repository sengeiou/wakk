package com.ubtrobot.analytics;

import android.content.ContentResolver;

public class ProviderAnalyticsProxy implements Analytics {

    private ContentResolver mContentResolver;

    public ProviderAnalyticsProxy(ContentResolver contentResolver) {
        if (contentResolver == null) {
            throw new IllegalArgumentException("Argument(contentResolver) is null.");
        }

        mContentResolver = contentResolver;
    }

    @Override
    public void enable(boolean enable) {
    }

    @Override
    public void setStrategy(Strategy strategy) {
    }

    @Override
    public Strategy getStrategy() {
        return null;
    }

    @Override
    public void recordEvent(Event event) {

    }
}
