package com.ubtrobot.motion.ipc;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.ubtrobot.device.ipc.DeviceProto;
import com.ubtrobot.motion.Joint;
import com.ubtrobot.motion.JointDevice;
import com.ubtrobot.motion.LocomotorDevice;

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

    public static MotionProto.JointRotatingOption
    toJointRotatingOptionProto(String jointId, boolean relatively, float angle, float speed) {
        return MotionProto.JointRotatingOption.newBuilder().
                setJointId(jointId).
                setRelatively(relatively).
                setAngle(angle).
                setUseSpeed(true).
                setSpeed(speed).
                build();
    }

    public static MotionProto.JointRotatingOption
    toJointRotatingOptionProto(String jointId, boolean relatively, float angle, long timeMillis) {
        return MotionProto.JointRotatingOption.newBuilder().
                setJointId(jointId).
                setRelatively(relatively).
                setAngle(angle).
                setUseSpeed(false).
                setTimeMillis(timeMillis).
                build();
    }

    public static MotionProto.JointRotatingProgress
    toJointRotatingProgressProto(Joint.RotatingProgress progress) {
        return MotionProto.JointRotatingProgress.newBuilder().
                setState(progress.getState()).
                setCurrentAngle(progress.getCurrentAngle()).
                setRotatedAngle(progress.getRotatedAngle()).
                setRotatedTimeMillis(progress.getRotatedTimeMillis()).
                build();
    }

    public static Joint.RotatingProgress
    toJointRotatingProgressPojo(MotionProto.JointRotatingProgress progress) {
        return new Joint.RotatingProgress(
                progress.getState(),
                progress.getCurrentAngle(),
                progress.getRotatedAngle(),
                progress.getRotatedTimeMillis()
        );
    }

    public static LocomotorDevice toLocomotorDevicePojo(DeviceProto.Device deviceProto)
            throws InvalidProtocolBufferException {
        LocomotorDevice.Builder builder = new LocomotorDevice.Builder(
                deviceProto.getId(), deviceProto.getName());
        MotionProto.LocomotorDeviceExt ext = deviceProto.getExtension().
                unpack(MotionProto.LocomotorDeviceExt.class);
        return builder.
                setDescription(deviceProto.getDescription()).
                setMinTurningSpeed(ext.getMinTurningSpeed()).
                setMaxTurningSpeed(ext.getMaxTurningSpeed()).
                setDefaultTurningSpeed(ext.getDefaultTurningSpeed()).
                setMinMovingSpeed(ext.getMinMovingSpeed()).
                setMaxMovingSpeed(ext.getMaxMovingSpeed()).
                setDefaultMovingSpeed(ext.getDefaultMovingSpeed()).
                build();
    }

    public static DeviceProto.Device toLocomotorDeviceProto(LocomotorDevice device) {
        return DeviceProto.Device.newBuilder().
                setId(device.getId()).
                setName(device.getName()).
                setDescription(device.getDescription()).
                setExtension(Any.pack(
                        MotionProto.LocomotorDeviceExt.newBuilder().
                                setMinTurningSpeed(device.getMinTurningSpeed()).
                                setMaxTurningSpeed(device.getMaxTurningSpeed()).
                                setDefaultTurningSpeed(device.getDefaultTurningSpeed()).
                                setMinMovingSpeed(device.getMinMovingSpeed()).
                                setMaxMovingSpeed(device.getMaxMovingSpeed()).
                                setDefaultMovingSpeed(device.getDefaultMovingSpeed()).
                                build()
                )).
                build();
    }
}