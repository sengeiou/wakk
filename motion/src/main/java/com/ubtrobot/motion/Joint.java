package com.ubtrobot.motion;

public class Joint {

    private final JointDevice mDevice;

    Joint(JointDevice device) {
        mDevice = device;
    }

    public String getId() {
        return mDevice.getId();
    }

    public JointDevice getDevice() {
        return mDevice;
    }
}