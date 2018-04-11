package com.ubtrobot.analytics;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;

import com.ubtrobot.analytics.ipc.AnalyticsConstants;

public class ProviderAnalyticsProxy implements Analytics {

    private ContentResolver mContentResolver;
    private Uri mUri;

    public ProviderAnalyticsProxy(ContentResolver contentResolver) {
        if (contentResolver == null) {
            throw new IllegalArgumentException("Argument(contentResolver) is null.");
        }

        mContentResolver = contentResolver;
        mUri = Uri.parse(AnalyticsConstants.PROVIDER_URI);
    }

    @Override
    public void enable(boolean enable) {
    }

    @Override
    public void setStrategy(Strategy strategy) {
        if (strategy == null) {
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putParcelable(AnalyticsConstants.KEY_STRATEGY, strategy);
        mContentResolver.call(mUri, AnalyticsConstants.CALL_METHOD_SET_STRATEGY, null, bundle);
    }

    @Override
    public Strategy getStrategy() {
        Strategy strategy = null;
        Bundle bundle = mContentResolver.call(mUri, AnalyticsConstants.CALL_METHOD_GET_STRATEGY, null, null);

        if (bundle != null) {
            strategy = bundle.getParcelable(AnalyticsConstants.CALL_METHOD_GET_STRATEGY);
        }

        return strategy != null ? strategy : (new Strategy.Builder().build());
    }

    @Override
    public void recordEvent(Event event) {

    }
}
