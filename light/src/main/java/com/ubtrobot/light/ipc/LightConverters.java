package com.ubtrobot.light.ipc;

import com.ubtrobot.device.ipc.DeviceConverters;
import com.ubtrobot.device.ipc.DeviceProto;
import com.ubtrobot.light.LightDevice;

import java.util.List;

public class LightConverters {

    private LightConverters() {
    }

    public static LightDevice toLightDevicePojo(DeviceProto.Device deviceProto) {
        return new LightDevice.Builder(deviceProto.getId(), deviceProto.getName()).
                setDescription(deviceProto.getDescription()).
                build();
    }

    public static DeviceProto.DeviceList toLightDeviceListProto(List<LightDevice> deviceList) {
        DeviceProto.DeviceList.Builder builder = DeviceProto.DeviceList.newBuilder();
        for (LightDevice lightDevice : deviceList) {
            builder.addDevice(DeviceConverters.toDeviceProto(lightDevice));
        }

        return builder.build();
    }
}
