package com.ubtrobot.motion;

import android.os.Handler;
import android.os.Looper;

import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.competition.ActivateException;
import com.ubtrobot.master.competition.CompetitionSession;
import com.ubtrobot.master.competition.CompetitionSessionExt;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.motion.ipc.MotionConstants;

import java.util.HashMap;
import java.util.List;

public class MotionManager {

    private final MasterContext mMasterContext;

    private final JointList mJointList;
    private final HashMap<String, CompetitionSessionExt<Joint>> mJointSessions = new HashMap<>();

    private final LocomotorGetter mLocomotorGetter;

    private final MotionScriptExecutor mScriptExecutor;
    private CompetitionSessionExt<MotionScriptExecutor> mScriptSession;

    public MotionManager(MasterContext masterContext) {
        if (masterContext == null) {
            throw new IllegalArgumentException("Argument masterContext is null.");
        }

        mMasterContext = masterContext;
        Handler handler = new Handler(Looper.getMainLooper());
        ProtoCallAdapter motionService = new ProtoCallAdapter(
                mMasterContext.createSystemServiceProxy(MotionConstants.SERVICE_NAME),
                handler
        );

        mJointList = new JointList(motionService);
        mLocomotorGetter = new LocomotorGetter(motionService);
        mScriptExecutor = new MotionScriptExecutor(mJointList);
    }

    public List<Joint> getJointList() {
        return mJointList.all();
    }

    public Joint getJoint(String jointId) {
        return mJointList.get(jointId);
    }


    /**
     * 查询关节是否正在旋转
     *
     * @param jointId 关节 id
     * @return 是否正在旋转
     */
    public Promise<Boolean, AccessServiceException> isJointRotating(String jointId) {
        return mJointList.get(jointId).isRotating();
    }

    /**
     * 获取关节当前角度
     *
     * @return 当前角度
     */
    public Promise<Float, AccessServiceException> getJointAngle(String jointId) {
        return mJointList.get(jointId).getAngle();
    }

    /**
     * 使关节以默认速度旋转一定角度
     *
     * @param jointId 关节 id
     * @param angle   旋转角度。正值顺时针旋转，负值逆时针旋转
     * @return promise。promise.done 通知成功后旋转到的角度
     */
    public ProgressivePromise<Void, JointException, JointRotatingProgress>
    jointRotateBy(String jointId, final float angle) {
        return jointRotate(
                jointId,
                new CompetitionSessionExt.SessionProgressiveCallable<
                        Void, JointException, JointRotatingProgress, Joint>() {
                    @Override
                    public ProgressivePromise<Void, JointException, JointRotatingProgress>
                    call(CompetitionSession session, Joint joint) {
                        return joint.rotateBy(session, angle);
                    }
                }
        );
    }

    private ProgressivePromise<Void, JointException, JointRotatingProgress> jointRotate(
            String jointId,
            final CompetitionSessionExt.SessionProgressiveCallable<
                    Void, JointException, JointRotatingProgress, Joint> callable) {
        final Joint joint = mJointList.get(jointId);
        final CompetitionSessionExt<Joint> sessionExt = jointSession(joint);

        return sessionExt.execute(
                joint,
                new CompetitionSessionExt.SessionProgressiveCallable<
                        Void, JointException, JointRotatingProgress, Joint>() {
                    @Override
                    public ProgressivePromise<Void, JointException, JointRotatingProgress>
                    call(CompetitionSession session, Joint joint) {
                        return callable.call(session, joint);
                    }
                },
                new CompetitionSessionExt.Converter<JointException>() {
                    @Override
                    public JointException convert(ActivateException e) {
                        return new JointException.Factory().occupied(e);
                    }
                }
        );
    }

    private CompetitionSessionExt<Joint> jointSession(Joint joint) {
        synchronized (mJointSessions) {
            CompetitionSessionExt<Joint> sessionExt = mJointSessions.get(joint.getId());
            if (sessionExt != null) {
                return sessionExt;
            }

            sessionExt = new CompetitionSessionExt<>(mMasterContext.openCompetitionSession().
                    addCompeting(joint));
            mJointSessions.put(joint.getId(), sessionExt);

            return sessionExt;
        }
    }

    /**
     * 使关节以一定速度旋转一定角度
     *
     * @param jointId 关节 id
     * @param angle   旋转角度。正值顺时针旋转，负值逆时针旋转
     * @param speed   旋转速度
     * @return promise。promise.done 通知成功后旋转到的角度
     */
    public ProgressivePromise<Void, JointException, JointRotatingProgress>
    jointRotateBy(String jointId, final float angle, final float speed) {
        return jointRotate(
                jointId,
                new CompetitionSessionExt.SessionProgressiveCallable<
                        Void, JointException, JointRotatingProgress, Joint>() {
                    @Override
                    public ProgressivePromise<Void, JointException, JointRotatingProgress>
                    call(CompetitionSession session, Joint joint) {
                        return joint.rotateBy(session, angle, speed);
                    }
                }
        );
    }

    /**
     * 使关节在一定时间内旋转一定角度
     *
     * @param jointId    关节 id
     * @param angle      旋转角度。正值顺时针旋转，负值逆时针旋转
     * @param timeMillis 旋转时间
     * @return promise。promise.done 通知成功后旋转到的角度
     */
    public ProgressivePromise<Void, JointException, JointRotatingProgress>
    jointRotateBy(String jointId, final float angle, final long timeMillis) {
        return jointRotate(
                jointId,
                new CompetitionSessionExt.SessionProgressiveCallable<
                        Void, JointException, JointRotatingProgress, Joint>() {
                    @Override
                    public ProgressivePromise<Void, JointException, JointRotatingProgress>
                    call(CompetitionSession session, Joint joint) {
                        return joint.rotateBy(session, angle, timeMillis);
                    }
                }
        );
    }

    /**
     * 使关节以默认速度旋转到某个角度
     *
     * @param jointId 关节 id
     * @param angle   旋转停留角度
     * @return promise
     */
    public ProgressivePromise<Void, JointException, JointRotatingProgress>
    jointRotateTo(String jointId, final float angle) {
        return jointRotate(
                jointId,
                new CompetitionSessionExt.SessionProgressiveCallable<
                        Void, JointException, JointRotatingProgress, Joint>() {
                    @Override
                    public ProgressivePromise<Void, JointException, JointRotatingProgress>
                    call(CompetitionSession session, Joint joint) {
                        return joint.rotateTo(session, angle);
                    }
                }
        );
    }

    /**
     * 使关节以一定速度旋转到某个角度
     *
     * @param jointId 关节 id
     * @param angle   旋转停留角度
     * @param speed   旋转速度
     * @return promise
     */
    public ProgressivePromise<Void, JointException, JointRotatingProgress>
    jointRotateTo(String jointId, final float angle, final float speed) {
        return jointRotate(
                jointId,
                new CompetitionSessionExt.SessionProgressiveCallable<
                        Void, JointException, JointRotatingProgress, Joint>() {
                    @Override
                    public ProgressivePromise<Void, JointException, JointRotatingProgress>
                    call(CompetitionSession session, Joint joint) {
                        return joint.rotateTo(session, angle, speed);
                    }
                }
        );
    }

    /**
     * 使关节在一定时间内旋转到某个角度
     *
     * @param jointId    关节 id
     * @param angle      旋转停留角度
     * @param timeMillis 旋转时间
     * @return promise
     */
    public ProgressivePromise<Void, JointException, JointRotatingProgress>
    jointRotateTo(String jointId, final float angle, final long timeMillis) {
        return jointRotate(
                jointId,
                new CompetitionSessionExt.SessionProgressiveCallable<
                        Void, JointException, JointRotatingProgress, Joint>() {
                    @Override
                    public ProgressivePromise<Void, JointException, JointRotatingProgress>
                    call(CompetitionSession session, Joint joint) {
                        return joint.rotateTo(session, angle, timeMillis);
                    }
                }
        );
    }

    /**
     * 获取移动装置
     *
     * @return 移动装置
     */
    public Locomotor getLocomotor() {
        return mLocomotorGetter.get();
    }

    public Promise<Void, ExecuteException> executeScript(final String scriptId) {
        return scriptSession().execute(
                mScriptExecutor,
                new CompetitionSessionExt.SessionCallable<
                        Void, ExecuteException, MotionScriptExecutor>() {
                    @Override
                    public Promise<Void, ExecuteException>
                    call(CompetitionSession session, MotionScriptExecutor executor) {
                        return executor.execute(session, scriptId);
                    }
                },
                new CompetitionSessionExt.Converter<ExecuteException>() {
                    @Override
                    public ExecuteException convert(ActivateException e) {
                        return new ExecuteException.Factory().occupied(e);
                    }
                }
        );
    }

    private CompetitionSessionExt<MotionScriptExecutor> scriptSession() {
        synchronized (mScriptExecutor) {
            if (mScriptSession == null) {
                mScriptSession = new CompetitionSessionExt<>(mMasterContext
                        .openCompetitionSession().addCompeting(mScriptExecutor));
            }

            return mScriptSession;
        }
    }
}