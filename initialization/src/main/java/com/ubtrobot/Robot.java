package com.ubtrobot;

import android.content.Context;

import com.ubtrobot.analytics.AnalyticsKit;
import com.ubtrobot.master.Master;
import com.ubtrobot.master.log.MasterLoggerFactory;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;
import com.ubtrobot.ulog.LoggerFactory;

public class Robot {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("Robot");

    private static volatile Context sApplicationContext;

    public static void initialize(Context context) {
        if (context == null) {
            throw new IllegalArgumentException(
                    "Master initialized with null context."
            );
        }

        if (sApplicationContext == null) {
            synchronized (Robot.class) {
                if (sApplicationContext == null) {
                    sApplicationContext = context.getApplicationContext();
                    Master.initialize(sApplicationContext);
                    AnalyticsKit.initialize(sApplicationContext);
                    return;
                }

                LOGGER.w("Robot has been initialized before.");
            }
        }
    }

    public static void setLoggerFactory(LoggerFactory loggerFactory) {
        FwLoggerFactory.setup(loggerFactory);
        MasterLoggerFactory.setup(loggerFactory);
    }

    public static Master master() {
        if (sApplicationContext == null) {
            synchronized (Master.class) {
                if (sApplicationContext == null) {
                    throw new IllegalStateException("Robot MUST be initialized first.");
                }
            }
        }

        return Master.get();
    }
}