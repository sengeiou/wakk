package com.ubtrobot.motion.sal;

import com.ubtrobot.async.Promise;
import com.ubtrobot.motion.Joint;
import com.ubtrobot.motion.JointDevice;
import com.ubtrobot.motion.JointDeviceException;
import com.ubtrobot.motion.JointException;

import java.util.List;

public interface MotionService {

    Promise<List<JointDevice>, JointDeviceException, Void> getJointList();

    boolean isJointRotating(String jointId);

    float getJointAngle(String jointId);

    Promise<Void, JointException, Joint.RotatingProgress>
    jointRotateBy(String jointId, float angle, float speed);

    Promise<Void, JointException, Joint.RotatingProgress>
    jointRotateBy(String jointId, float angle, long timeMillis);

    Promise<Void, JointException, Joint.RotatingProgress>
    jointRotateTo(String jointId, float angle, float speed);

    Promise<Void, JointException, Joint.RotatingProgress>
    jointRotateTo(String jointId, float angle, long timeMillis);
}