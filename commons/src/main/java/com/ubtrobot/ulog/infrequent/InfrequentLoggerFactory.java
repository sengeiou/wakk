package com.ubtrobot.ulog.infrequent;

import com.ubtrobot.ulog.Logger;
import com.ubtrobot.ulog.LoggerFactory;

public class InfrequentLoggerFactory implements LoggerFactory {

    @Override
    public Logger getLogger(String tag) {
        return new InfrequentLogger(tag);
    }
}