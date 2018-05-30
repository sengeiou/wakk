package com.ubtrobot.motion;

import com.google.protobuf.InvalidProtocolBufferException;
import com.ubtrobot.cache.CachedField;
import com.ubtrobot.device.ipc.DeviceProto;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.motion.ipc.MotionConstants;
import com.ubtrobot.motion.ipc.MotionConverters;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JointList {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("JointList");

    private final CachedField<List<Joint>> mJoints;
    private final CachedField<Map<String, Joint>> mJointMap;

    JointList(final ProtoCallAdapter motionService) {
        mJoints = new CachedField<>(new CachedField.FieldGetter<List<Joint>>() {
            @Override
            public List<Joint> get() {
                try {
                    DeviceProto.DeviceList deviceList = motionService.syncCall(
                            MotionConstants.CALL_PATH_GET_JOINT_LIST, DeviceProto.DeviceList.class);
                    LinkedList<Joint> joints = new LinkedList<>();
                    for (DeviceProto.Device device : deviceList.getDeviceList()) {
                        joints.add(new Joint(motionService, MotionConverters.toJointDevicePojo(device)));
                    }

                    return Collections.unmodifiableList(joints);
                } catch (CallException e) {
                    LOGGER.e(e, "Framework error when getting the joint list.");
                } catch (InvalidProtocolBufferException e) {
                    LOGGER.e(e, "Framework error when getting the joint list.");
                }

                return null;
            }
        });

        mJointMap = new CachedField<>(new CachedField.FieldGetter<Map<String, Joint>>() {
            @Override
            public Map<String, Joint> get() {
                List<Joint> joints = mJoints.get();
                if (joints == null) {
                    return null;
                }

                HashMap<String, Joint> jointMap = new HashMap<>();
                for (Joint joint : joints) {
                    jointMap.put(joint.getId(), joint);
                }

                return jointMap;
            }
        });
    }

    public List<Joint> all() {
        List<Joint> joints = mJoints.get();
        return joints == null ? Collections.<Joint>emptyList() : joints;
    }

    public Joint get(String jointId) {
        Map<String, Joint> jointMap = mJointMap.get();
        Joint joint = jointMap == null ? null : jointMap.get(jointId);

        if (joint == null) {
            throw new JointNotFoundException();
        }
        return joint;
    }

    public static class JointNotFoundException extends RuntimeException {
    }
}