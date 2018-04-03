package com.ubtrobot.ulog;

import com.ubtrobot.ulog.infrequent.InfrequentLoggerFactory;

public class FwLoggerFactory {

    private static volatile LoggerFactory sRealLoggerFactory = new InfrequentLoggerFactory();

    private FwLoggerFactory() {
    }

    public static void setup(LoggerFactory loggerFactory) {
        if(loggerFactory == null) {
            throw new IllegalArgumentException("Argument loggerFactory is null.");
        } else {
            sRealLoggerFactory = loggerFactory;
        }
    }

    public static Logger getLogger(String tag) {
        return sRealLoggerFactory.getLogger("FW|" + tag);
    }
}