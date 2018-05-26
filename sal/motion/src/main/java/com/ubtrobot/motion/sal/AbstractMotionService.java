package com.ubtrobot.motion.sal;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.async.InterruptibleProgressiveAsyncTask;
import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.cache.CachedField;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.competition.InterruptibleTaskHelper;
import com.ubtrobot.motion.JointDevice;
import com.ubtrobot.motion.JointException;
import com.ubtrobot.motion.JointGroupRotatingProgress;
import com.ubtrobot.motion.JointRotatingOption;
import com.ubtrobot.motion.LocomotorDevice;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractMotionService implements MotionService {

    private static final String TASK_RECEIVER_JOINT_PREFIX = "joint-";
    private static final String TASK_NAME_JOINT_ROTATE = "joint-rotate";

    private final CachedField<Promise<List<JointDevice>, AccessServiceException>> mJoingListPromise;
    private final InterruptibleTaskHelper mInterruptibleTaskHelper;

    public AbstractMotionService() {
        mInterruptibleTaskHelper = new InterruptibleTaskHelper();

        mJoingListPromise = new CachedField<>(new CachedField.FieldGetter<
                Promise<List<JointDevice>, AccessServiceException>>() {
            @Override
            public Promise<List<JointDevice>, AccessServiceException> get() {
                AsyncTask<List<JointDevice>, AccessServiceException> task = createGettingJointListTask();
                if (task == null) {
                    throw new IllegalStateException("createGetJointListTask returns null.");
                }

                task.start();
                return task.promise();
            }
        });
    }

    @Override
    public Promise<List<JointDevice>, AccessServiceException> getJointList() {
        return mJoingListPromise.get();
    }

    protected abstract AsyncTask<List<JointDevice>, AccessServiceException> createGettingJointListTask();

    @Override
    public Promise<List<String>, AccessServiceException>
    isJointsRotating(List<String> jointIdList) {
        AsyncTask<List<String>, AccessServiceException> task = createGettingJointsRotatingTask(jointIdList);
        if (task == null) {
            throw new IllegalStateException("createGetJointsRotatingTask returns null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<List<String>, AccessServiceException>
    createGettingJointsRotatingTask(List<String> jointIdList);

    @Override
    public Promise<Map<String, Float>, AccessServiceException>
    getJointsAngle(List<String> jointIdList) {
        AsyncTask<Map<String, Float>, AccessServiceException> task
                = createGettingJointsAngleTask(jointIdList);
        if (task == null) {
            throw new IllegalStateException("createGettingJointsAngleTask returns null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<Map<String, Float>, AccessServiceException>
    createGettingJointsAngleTask(List<String> jointIdList);

    @Override
    public ProgressivePromise<Void, JointException, JointGroupRotatingProgress>
    jointRotate(final Map<String, List<JointRotatingOption>> optionSequenceMap) {
        LinkedList<String> jointReceivers = new LinkedList<>();
        for (String jointId : optionSequenceMap.keySet()) {
            jointReceivers.add(TASK_RECEIVER_JOINT_PREFIX + jointId);
        }

        final InterruptibleTaskHelper.Session session = new InterruptibleTaskHelper.Session();
        return mInterruptibleTaskHelper.start(
                jointReceivers,
                TASK_NAME_JOINT_ROTATE,
                session,
                new InterruptibleProgressiveAsyncTask<
                        Void, JointException, JointGroupRotatingProgress>() {
                    @Override
                    protected void onStart() {
                        jointStartRotating(session.getId(), optionSequenceMap);
                    }

                    @Override
                    protected void onCancel() {
                        jointStopRotating(session.getId());
                    }
                },
                new InterruptibleTaskHelper.InterruptedExceptionCreator<JointException>() {
                    @Override
                    public JointException createInterruptedException(Set<String> interrupters) {
                        return new JointException.Factory().interrupted("Interrupted by joints("
                                + interrupters + ").");
                    }
                }
        );
    }

    protected abstract void
    jointStartRotating(String sessionId, Map<String, List<JointRotatingOption>> optionSequenceMap);

    protected void jointReportRotatingProgress(String sessionId, JointGroupRotatingProgress progress) {
        mInterruptibleTaskHelper.report(sessionId, progress);
    }

    protected void jointResolveRotating(String sessionId) {
        mInterruptibleTaskHelper.resolve(sessionId, null);
    }

    protected void jointRejectRotating(String sessionId, JointException e) {
        mInterruptibleTaskHelper.reject(sessionId, e);
    }

    protected abstract void
    jointStopRotating(String sessionId);

    @Override
    public Promise<LocomotorDevice, AccessServiceException> getLocomotor() {
        return null;
    }
}