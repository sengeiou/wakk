package com.ubtrobot.analytics.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.ubtrobot.analytics.ipc.AnalyticsConstants;

public class ShutdownReceiver extends BroadcastReceiver {

    private static final String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!intent.getAction().equals(ACTION_SHUTDOWN)) {
            return;
        }

        ContentResolver resolver = context.getContentResolver();
        resolver.call(Uri.parse(AnalyticsConstants.PROVIDER_URI),
                AnalyticsConstants.CALL_METHOD_SHUTDOWN, null, null);
    }
}
