package com.ubtrobot.motion.sal;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.async.Promise;
import com.ubtrobot.motion.JointDevice;
import com.ubtrobot.motion.JointDeviceException;

import java.util.List;

public abstract class AbstractMotionService implements MotionService {

    @Override
    public Promise<List<JointDevice>, JointDeviceException, Void> getJointList() {
        AsyncTask<List<JointDevice>, JointDeviceException, Void> task = createGetJointListTask();
        if (task == null) {
            throw new IllegalStateException("createGetJointListTask returns null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<List<JointDevice>, JointDeviceException, Void>
    createGetJointListTask();
}