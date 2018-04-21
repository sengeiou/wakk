package com.ubtrobot.motion;

import android.os.Handler;
import android.os.Looper;

import com.ubtrobot.async.Promise;
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

        mJointList = new JointList(motionService, handler);
        mLocomotorGetter = new LocomotorGetter(motionService);
    }

    /**
     * 获取关节列表
     *
     * @return 关节列表
     */
    public List<Joint> getJointList() {
        return mJointList.all();
    }

    /**
     * 获取某个关节
     *
     * @param jointId 关节 id
     * @return 关节
     */
    public Joint getJoint(String jointId) {
        return mJointList.get(jointId);
    }

    /**
     * 查询关节是否正在旋转
     *
     * @param jointId 关节 id
     * @return 是否正在旋转
     */
    public boolean isJointRotating(String jointId) {
        return mJointList.get(jointId).isRotating();
    }

    /**
     * 获取关节当前角度
     *
     * @return 当前角度
     */
    public float getJointAngle(String jointId) {
        return mJointList.get(jointId).getAngle();
    }

    /**
     * 使关节以默认速度旋转一定角度
     *
     * @param jointId 关节 id
     * @param angle   旋转角度。正值顺时针旋转，负值逆时针旋转
     * @return promise。promise.done 通知成功后旋转到的角度
     */
    public Promise<Void, JointException, Joint.RotatingProgress>
    jointRotateBy(String jointId, final float angle) {
        return jointRotate(
                jointId,
                new CompetitionSessionExt.SessionCallable<
                        Void, JointException, Joint.RotatingProgress, Joint>() {
                    @Override
                    public Promise<Void, JointException, Joint.RotatingProgress>
                    call(CompetitionSession session, Joint joint) {
                        return joint.rotateBy(session, angle);
                    }
                }
        );
    }

    private Promise<Void, JointException, Joint.RotatingProgress> jointRotate(
            String jointId,
            final CompetitionSessionExt.SessionCallable<
                    Void, JointException, Joint.RotatingProgress, Joint> callable) {
        final Joint joint = mJointList.get(jointId);
        final CompetitionSessionExt<Joint> sessionExt = jointSession(joint);

        return sessionExt.execute(
                joint,
                new CompetitionSessionExt.SessionCallable<
                        Void, JointException, Joint.RotatingProgress, Joint>() {
                    @Override
                    public Promise<Void, JointException, Joint.RotatingProgress>
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
    public Promise<Void, JointException, Joint.RotatingProgress>
    jointRotateBy(String jointId, final float angle, final float speed) {
        return jointRotate(
                jointId,
                new CompetitionSessionExt.SessionCallable<
                        Void, JointException, Joint.RotatingProgress, Joint>() {
                    @Override
                    public Promise<Void, JointException, Joint.RotatingProgress>
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
    public Promise<Void, JointException, Joint.RotatingProgress>
    jointRotateBy(String jointId, final float angle, final long timeMillis) {
        return jointRotate(
                jointId,
                new CompetitionSessionExt.SessionCallable<
                        Void, JointException, Joint.RotatingProgress, Joint>() {
                    @Override
                    public Promise<Void, JointException, Joint.RotatingProgress>
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
    public Promise<Void, JointException, Joint.RotatingProgress>
    jointRotateTo(String jointId, final float angle) {
        return jointRotate(
                jointId,
                new CompetitionSessionExt.SessionCallable<
                        Void, JointException, Joint.RotatingProgress, Joint>() {
                    @Override
                    public Promise<Void, JointException, Joint.RotatingProgress>
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
    public Promise<Void, JointException, Joint.RotatingProgress>
    jointRotateTo(String jointId, final float angle, final float speed) {
        return jointRotate(
                jointId,
                new CompetitionSessionExt.SessionCallable<
                        Void, JointException, Joint.RotatingProgress, Joint>() {
                    @Override
                    public Promise<Void, JointException, Joint.RotatingProgress>
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
    public Promise<Void, JointException, Joint.RotatingProgress>
    jointRotateTo(String jointId, final float angle, final long timeMillis) {
        return jointRotate(
                jointId,
                new CompetitionSessionExt.SessionCallable<
                        Void, JointException, Joint.RotatingProgress, Joint>() {
                    @Override
                    public Promise<Void, JointException, Joint.RotatingProgress>
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
}