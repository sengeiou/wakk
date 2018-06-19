package com.ubtrobot.analytics;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;

import com.ubtrobot.analytics.ipc.AnalyticsConstants;

public class ProviderAnalyticsProxy implements Analytics {

    private ContentResolver mContentResolver;
    private Uri mUri;
    private volatile boolean mIsProviderInstalled;

    public ProviderAnalyticsProxy(ContentResolver contentResolver) {
        if (contentResolver == null) {
            throw new IllegalArgumentException("Argument(contentResolver) is null.");
        }

        mContentResolver = contentResolver;
        mUri = Uri.parse(AnalyticsConstants.PROVIDER_URI);
    }

    @Override
    public void enable(boolean enable) {
        throw new UnsupportedOperationException("Robot nonsupport enable(boolean enable).");
    }

    @Override
    public void setStrategy(Strategy strategy) {
        throw new UnsupportedOperationException("Robot nonsupport setStrategy(Strategy strategy).");
    }

    @Override
    public Strategy getStrategy() {
        checkProviderInstalled(mUri, mContentResolver);

        Strategy strategy = null;
        Bundle bundle = mContentResolver.call(mUri,
                AnalyticsConstants.CALL_METHOD_GET_STRATEGY, null, null);

        if (bundle != null) {
            bundle.setClassLoader(getClass().getClassLoader());
            strategy = bundle.getParcelable(AnalyticsConstants.KEY_STRATEGY);
        }

        return strategy != null ? strategy : Strategy.DEFAULT;
    }

    @Override
    public void recordEvent(Event event) {
        checkProviderInstalled(mUri, mContentResolver);

        Bundle bundle = new Bundle();
        bundle.putParcelable(AnalyticsConstants.KEY_RECORD_EVENT, event);
        mContentResolver.call(mUri, AnalyticsConstants.CALL_METHOD_RECORD_EVENT, null, bundle);
    }

    private void checkProviderInstalled(Uri uri, ContentResolver resolver) {
        if (mIsProviderInstalled) {
            return;
        }

        synchronized (ProviderAnalyticsProxy.class) {
            if (mIsProviderInstalled) {
                return;
            }

            try {
                Bundle bundle = resolver.call(uri, AnalyticsConstants.CALL_METHOD_PING, null, null);
                if (bundle == null || !(bundle.getBoolean(AnalyticsConstants.KEY_PROVIDER_PONG))) {
                    throw new IllegalStateException("Provider illegal.");
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Pleases install AnalyticsSystemService.");
            }

            mIsProviderInstalled = true;
        }
    }
}
