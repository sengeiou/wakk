package com.ubtrobot.motion.sal;

import com.ubtrobot.async.Promise;
import com.ubtrobot.motion.JointDevice;
import com.ubtrobot.motion.JointDeviceException;

import java.util.List;

public interface MotionService {

    Promise<List<JointDevice>, JointDeviceException, Void> getJointList();
}