package com.ubtrobot.light;

public class Light {

    private final LightDevice mDevice;

    Light(LightDevice device) {
        mDevice = device;
    }

    public String getId() {
        return mDevice.getId();
    }

    public LightDevice getDevice() {
        return mDevice;
    }
}
