package com.ubtrobot.motion.ipc;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.ubtrobot.device.ipc.DeviceProto;
import com.ubtrobot.motion.JointDevice;

import java.util.List;

public class MotionConverters {

    private MotionConverters() {
    }

    public static JointDevice toJointDevicePojo(DeviceProto.Device deviceProto)
            throws InvalidProtocolBufferException {
        JointDevice.Builder builder = new JointDevice.Builder(
                deviceProto.getId(), deviceProto.getName());
        MotionProto.JointDeviceExt ext = deviceProto.getExtension().
                unpack(MotionProto.JointDeviceExt.class);
        return builder.
                setDescription(deviceProto.getDescription()).
                setMinAngle(ext.getMinAngle()).
                setMaxAngle(ext.getMaxAngle()).
                setMinSpeed(ext.getMinSpeed()).
                setMaxSpeed(ext.getMaxSpeed()).
                setDefaultSpeed(ext.getDefaultSpeed()).
                build();
    }

    public static DeviceProto.Device toJointDeviceProto(JointDevice device) {
        return DeviceProto.Device.newBuilder().
                setId(device.getId()).
                setName(device.getName()).
                setDescription(device.getDescription()).
                setExtension(Any.pack(
                        MotionProto.JointDeviceExt.newBuilder().
                                setMinAngle(device.getMinAngle()).
                                setMaxAngle(device.getMaxAngle()).
                                setMinSpeed(device.getMinSpeed()).
                                setMaxSpeed(device.getMaxSpeed()).
                                setDefaultSpeed(device.getDefaultSpeed()).
                                build()
                )).
                build();
    }

    public static DeviceProto.DeviceList toJointDeviceListProto(List<JointDevice> deviceList) {
        DeviceProto.DeviceList.Builder builder = DeviceProto.DeviceList.newBuilder();
        for (JointDevice jointDevice : deviceList) {
            builder.addDevice(toJointDeviceProto(jointDevice));
        }

        return builder.build();
    }
}