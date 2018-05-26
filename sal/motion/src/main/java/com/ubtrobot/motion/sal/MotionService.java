package com.ubtrobot.motion.sal;

import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.motion.ExecuteException;
import com.ubtrobot.motion.JointDevice;
import com.ubtrobot.motion.JointException;
import com.ubtrobot.motion.JointGroupRotatingProgress;
import com.ubtrobot.motion.JointRotatingOption;
import com.ubtrobot.motion.LocomotorDevice;

import java.util.List;
import java.util.Map;

public interface MotionService {

    Promise<List<JointDevice>, AccessServiceException> getJointList();

    Promise<List<String>, AccessServiceException> isJointsRotating(List<String> jointIdList);

    Promise<Map<String, Float>, AccessServiceException> getJointsAngle(List<String> jointIdList);

    ProgressivePromise<Void, JointException, JointGroupRotatingProgress>
    jointRotate(Map<String, List<JointRotatingOption>> optionSequenceMap);

    Promise<LocomotorDevice, AccessServiceException> getLocomotor();

    Promise<Void, ExecuteException> executeScript(String scriptId);
}