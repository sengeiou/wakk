package com.ubtrobot.motion.ipc;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.ubtrobot.device.ipc.DeviceProto;
import com.ubtrobot.motion.JointDevice;
import com.ubtrobot.motion.JointGroupRotatingProgress;
import com.ubtrobot.motion.JointRotatingOption;
import com.ubtrobot.motion.LocomotorDevice;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MotionConverters {

    private MotionConverters() {
    }

    public static MotionProto.JointIdList toJointIdListProto(List<JointDevice> jointDevices) {
        MotionProto.JointIdList.Builder builder = MotionProto.JointIdList.newBuilder();
        for (JointDevice jointDevice : jointDevices) {
            builder.addId(jointDevice.getId());
        }
        return builder.build();
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

    public static Map<String, List<JointRotatingOption>> toJointRotatingOptionSequenceMapPojo(
            MotionProto.JointRotatingOptionSequenceMap optionSequenceMap) {
        HashMap<String, List<JointRotatingOption>> ret = new HashMap<>();
        for (Map.Entry<String, MotionProto.JointRotatingOptionSequence> entry
                : optionSequenceMap.getOptionSequenceMap().entrySet()) {
            LinkedList<JointRotatingOption> optionSequence = new LinkedList<>();
            ret.put(entry.getKey(), optionSequence);

            for (MotionProto.JointRotatingOption option : entry.getValue().getOptionList()) {
                optionSequence.add(toJointRotatingOptionPojo(option));
            }
        }

        return ret;
    }

    public static MotionProto.JointRotatingOption
    toJointRotatingOptionProto(JointRotatingOption option) {
        return MotionProto.JointRotatingOption.newBuilder().
                setJointId(option.getJointId()).
                setAngle(option.getAngle()).
                setAngleAbsolute(option.isAngleAbsolute()).
                setDuration(option.getDuration()).
                setSpeed(option.getSpeed()).
                build();
    }

    public static JointRotatingOption
    toJointRotatingOptionPojo(MotionProto.JointRotatingOption option) {
        return new JointRotatingOption.Builder().
                setJointId(option.getJointId()).
                setAngle(option.getAngle()).
                setAngleAbsolute(option.getAngleAbsolute()).
                setDuration(option.getDuration()).
                setSpeed(option.getSpeed()).
                build();
    }

    public static MotionProto.JointGroupRotatingProgress
    toJointGroupRotatingProgressProto(JointGroupRotatingProgress progress) {
        return MotionProto.JointGroupRotatingProgress.newBuilder()
                .setSessionId(progress.getSessionId())
                .setState(progress.getState())
                .build();
    }

    public static JointGroupRotatingProgress
    toJointGroupRotatingProgressPojo(MotionProto.JointGroupRotatingProgress progress) {
        return new JointGroupRotatingProgress.Builder(
                progress.getSessionId(), progress.getState()).build();
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