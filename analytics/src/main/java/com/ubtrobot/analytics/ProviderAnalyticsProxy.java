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
        Bundle bundle = new Bundle();
        bundle.putBoolean(AnalyticsConstants.KEY_ENABLE, enable);
        mContentResolver.call(mUri, AnalyticsConstants.CALL_METHOD_ENABLE, null, bundle);
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
            bundle.setClassLoader(getClass().getClassLoader());
            strategy = bundle.getParcelable(AnalyticsConstants.CALL_METHOD_GET_STRATEGY);
        }

        return strategy != null ? strategy : Strategy.DEFAULT;
    }

    @Override
    public void recordEvent(Event event) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(AnalyticsConstants.KEY_RECORD_EVENT, event);
        mContentResolver.call(mUri, AnalyticsConstants.CALL_METHOD_RECORD_EVENT, null, bundle);
    }
}
