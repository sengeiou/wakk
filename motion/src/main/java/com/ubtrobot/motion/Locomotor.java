package com.ubtrobot.motion;

import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

public class Locomotor {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("Locomotor");

    private final LocomotorDevice mDevice;

    Locomotor(LocomotorDevice device) {
        mDevice = device;
    }

    public String getId() {
        return mDevice.getId();
    }

    public LocomotorDevice getDevice() {
        return mDevice;
    }
}