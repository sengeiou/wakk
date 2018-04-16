package com.ubtrobot.device.ipc;

import com.ubtrobot.device.Device;

public class DeviceConverters {

    private DeviceConverters() {
    }

    public static DeviceProto.Device toDeviceProto(Device device) {
        return DeviceProto.Device.newBuilder().setId(device.getId()).setName(device.getName()).
                setDescription(device.getDescription()).build();
    }
}
