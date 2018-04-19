package com.ubtrobot.motion.sal;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.async.InterruptibleAsyncTask;
import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.competition.InterruptibleTaskHelper;
import com.ubtrobot.motion.Joint;
import com.ubtrobot.motion.JointDevice;
import com.ubtrobot.motion.JointException;

import java.util.List;

public abstract class AbstractMotionService implements MotionService {

    private static final String TASK_RECEIVER_JOINT_PREFIX = "joint-";
    private static final String TASK_NAME_OINT_ROTATE = "joint-rotate";

    private final InterruptibleTaskHelper mInterruptibleTaskHelper;

    public AbstractMotionService() {
        mInterruptibleTaskHelper = new InterruptibleTaskHelper();
    }

    @Override
    public Promise<List<JointDevice>, AccessServiceException, Void> getJointList() {
        AsyncTask<List<JointDevice>, AccessServiceException, Void> task = createGetJointListTask();
        if (task == null) {
            throw new IllegalStateException("createGetJointListTask returns null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<List<JointDevice>, AccessServiceException, Void>
    createGetJointListTask();

    @Override
    public boolean isJointRotating(String jointId) {
        return doGetIsJointRotating(jointId);
    }

    protected abstract boolean doGetIsJointRotating(String jointId);

    @Override
    public float getJointAngle(String jointId) {
        return doGetJointAngle(jointId);
    }

    protected abstract float doGetJointAngle(String jointId);

    @Override
    public Promise<Void, JointException, Joint.RotatingProgress>
    jointRotateBy(String jointId, final float angle, final float speed) {
        return jointRotate(new JointInterruptibleAsyncTask(jointId) {
            @Override
            protected void onStart() {
                jointStartRotatingBy(jointId, angle, speed);
            }
        });
    }

    private Promise<Void, JointException, Joint.RotatingProgress>
    jointRotate(JointInterruptibleAsyncTask task) {
        return mInterruptibleTaskHelper.start(
                TASK_RECEIVER_JOINT_PREFIX + task.jointId,
                TASK_NAME_OINT_ROTATE,
                task,
                new InterruptibleTaskHelper.InterruptedExceptionCreator<JointException>() {
                    @Override
                    public JointException createInterruptedException(String interrupter) {
                        return new JointException.Factory().interrupted("Interrupt the " +
                                interrupter + " task.");
                    }
                }
        );
    }

    protected abstract void jointStartRotatingBy(String jointId, float angle, float speed);

    protected abstract void jointStopRotating(String jointId);

    @Override
    public Promise<Void, JointException, Joint.RotatingProgress>
    jointRotateBy(String jointId, final float angle, final long timeMillis) {
        return jointRotate(new JointInterruptibleAsyncTask(jointId) {
            @Override
            protected void onStart() {
                jointStartRotatingBy(jointId, angle, timeMillis);
            }
        });
    }

    protected abstract void jointStartRotatingBy(String jointId, float angle, long timeMillis);

    @Override
    public Promise<Void, JointException, Joint.RotatingProgress>
    jointRotateTo(String jointId, final float angle, final float speed) {
        return jointRotate(new JointInterruptibleAsyncTask(jointId) {
            @Override
            protected void onStart() {
                jointStartRotatingBy(jointId, angle, speed);
            }
        });
    }

    protected abstract void jointStartRotatingTo(String jointId, float angle, float speed);

    @Override
    public Promise<Void, JointException, Joint.RotatingProgress>
    jointRotateTo(String jointId, final float angle, final long timeMillis) {
        return jointRotate(new JointInterruptibleAsyncTask(jointId) {
            @Override
            protected void onStart() {
                jointStartRotatingBy(jointId, angle, timeMillis);
            }
        });
    }

    protected abstract void jointStartRotatingTo(String jointId, float angle, long timeMillis);

    private abstract class JointInterruptibleAsyncTask
            extends InterruptibleAsyncTask<Void, JointException, Joint.RotatingProgress> {

        final String jointId;

        public JointInterruptibleAsyncTask(String jointId) {
            this.jointId = jointId;
        }

        @Override
        protected void onCancel() {
            jointStopRotating(jointId);
        }
    }
}