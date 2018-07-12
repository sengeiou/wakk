package com.ubtrobot.motion;

import android.os.Handler;
import android.os.Looper;

import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.competition.ActivateException;
import com.ubtrobot.master.competition.Competing;
import com.ubtrobot.master.competition.CompetitionSession;
import com.ubtrobot.master.competition.CompetitionSessionExt;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.motion.ipc.MotionConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MotionManager {

    private final MasterContext mMasterContext;
    private final ProtoCallAdapter mMotionService;

    private final JointList mJointList;
    private final LocomotorGetter mLocomotorGetter;
    private final MotionScriptExecutor mScriptExecutor;

    private final Map<String, SessionEnv> mSessions = new HashMap<>();

    public MotionManager(MasterContext masterContext) {
        if (masterContext == null) {
            throw new IllegalArgumentException("Argument masterContext is null.");
        }

        mMasterContext = masterContext;
        Handler handler = new Handler(Looper.getMainLooper());
        mMotionService = new ProtoCallAdapter(
                mMasterContext.createSystemServiceProxy(MotionConstants.SERVICE_NAME),
                handler
        );

        mJointList = new JointList(mMotionService);
        mLocomotorGetter = new LocomotorGetter(mMotionService);
        mScriptExecutor = new MotionScriptExecutor(mJointList, mLocomotorGetter);
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

    public Promise<Map<String, Boolean>, AccessServiceException>
    isJointsRotating(String... jointIds) {
        return isJointsRotating(Arrays.asList(jointIds));
    }

    public Promise<Map<String, Boolean>, AccessServiceException>
    isJointsRotating(List<String> jointIds) {
        return createJointGroup(jointIds).isRotating();
    }

    /**
     * 获取关节当前角度
     *
     * @return 当前角度
     */
    public Promise<Float, AccessServiceException> getJointAngle(String jointId) {
        return mJointList.get(jointId).getAngle();
    }

    public Promise<Map<String, Float>, AccessServiceException> getJointsAngle(List<String> jointIds) {
        return createJointGroup(jointIds).getAngles();
    }

    public Promise<Map<String, Float>, AccessServiceException> getJointsAngle(String... jointIds) {
        return getJointsAngle(Arrays.asList(jointIds));
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

    private SessionEnv getSession(
            List<String> jointIds,
            boolean containLocomotor,
            boolean containScriptExecutor) {
        LinkedList<String> sortedJointIds;
        if (jointIds == null || jointIds.isEmpty()) {
            sortedJointIds = new LinkedList<>();
        } else {
            sortedJointIds = new LinkedList<>(new HashSet<>(jointIds));
            Collections.sort(sortedJointIds);
        }

        StringBuilder builder = new StringBuilder();
        for (String jointId : sortedJointIds) {
            builder.append("Joint").append(jointId);
        }
        if (containLocomotor) {
            builder.append("Locomotor");
        }
        if (containScriptExecutor) {
            builder.append("Executor");
        }
        if (builder.length() == 0) {
            throw new AssertionError("Should NOT be here.");
        }

        String key = builder.toString();
        synchronized (this) {
            SessionEnv sessionEnv = mSessions.get(key);
            if (sessionEnv == null) {
                CompetitionSession session = mMasterContext.openCompetitionSession();
                sessionEnv = new SessionEnv(new CompetitionSessionExt<>(session));

                if (!sortedJointIds.isEmpty()) {
                    sessionEnv.jointGroup = createJointGroup(sortedJointIds);
                    session.addCompeting(sessionEnv.jointGroup);
                }
                if (containLocomotor) {
                    sessionEnv.locomotor = mLocomotorGetter.get();
                    session.addCompeting(sessionEnv.locomotor);
                }
                if (containScriptExecutor) {
                    sessionEnv.scriptExecutor = mScriptExecutor;
                    session.addCompeting(sessionEnv.scriptExecutor);
                }

                mSessions.put(key, sessionEnv);
            }

            return sessionEnv;
        }
    }

    private SessionEnv getLocomotorSession() {
        return getSession(null, true, false);
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
     * @param jointId  关节 id
     * @param angle    旋转角度。正值顺时针旋转，负值逆时针旋转
     * @param duration 旋转时间
     * @return promise。promise.done 通知成功后旋转到的角度
     */
    public ProgressivePromise<Void, JointException, JointRotatingProgress>
    jointRotateBy(String jointId, final float angle, final long duration) {
        return jointRotate(
                jointId,
                new CompetitionSessionExt.SessionProgressiveCallable<
                        Void, JointException, JointRotatingProgress, Joint>() {
                    @Override
                    public ProgressivePromise<Void, JointException, JointRotatingProgress>
                    call(CompetitionSession session, Joint joint) {
                        return joint.rotateBy(session, angle, duration);
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
     * @param jointId  关节 id
     * @param angle    旋转停留角度
     * @param duration 旋转时间
     * @return promise
     */
    public ProgressivePromise<Void, JointException, JointRotatingProgress>
    jointRotateTo(String jointId, final float angle, final long duration) {
        return jointRotate(
                jointId,
                new CompetitionSessionExt.SessionProgressiveCallable<
                        Void, JointException, JointRotatingProgress, Joint>() {
                    @Override
                    public ProgressivePromise<Void, JointException, JointRotatingProgress>
                    call(CompetitionSession session, Joint joint) {
                        return joint.rotateTo(session, angle, duration);
                    }
                }
        );
    }

    public ProgressivePromise<Void, JointException, JointRotatingProgress>
    jointRotate(String jointId, final List<JointRotatingOption> optionSequence) {
        return jointRotate(
                jointId,
                new CompetitionSessionExt.SessionProgressiveCallable<
                        Void, JointException, JointRotatingProgress, Joint>() {
                    @Override
                    public ProgressivePromise<Void, JointException, JointRotatingProgress>
                    call(CompetitionSession session, Joint joint) {
                        return joint.rotate(session, optionSequence);
                    }
                }
        );
    }

    private ProgressivePromise<Void, JointException, JointRotatingProgress> jointRotate(
            String jointId,
            final CompetitionSessionExt.SessionProgressiveCallable<
                    Void, JointException, JointRotatingProgress, Joint> callable) {
        final Joint joint = mJointList.get(jointId);
        SessionEnv sessionEnv = getSession(Collections.singletonList(jointId), false, false);
        return sessionEnv.session.execute(
                joint,
                new CompetitionSessionExt.SessionProgressiveCallable<
                        Void, JointException, JointRotatingProgress, Competing>() {
                    @Override
                    public ProgressivePromise<Void, JointException, JointRotatingProgress>
                    call(CompetitionSession session, Competing competing) {
                        return callable.call(session, joint);
                    }
                },
                new CompetitionSessionExt.Converter<JointException>() {
                    @Override
                    public JointException convert(ActivateException e) {
                        return null;
                    }
                }
        );
    }


    public ProgressivePromise<Void, JointException, JointRotatingProgress>
    jointRotate(String jointId, JointRotatingOption... optionSequence) {
        return jointRotate(jointId, Arrays.asList(optionSequence));
    }

    public ProgressivePromise<Void, JointException, JointGroupRotatingProgress>
    jointsRotate(final Map<String, List<JointRotatingOption>> optionSequenceMap) {
        if (optionSequenceMap.isEmpty()) {
            throw new IllegalArgumentException("Argument optionSequenceMap's content is empty.");
        }

        LinkedList<String> jointIds = new LinkedList<>(optionSequenceMap.keySet());
        final SessionEnv sessionEnv = getSession(jointIds, false, false);
        return sessionEnv.session.execute(
                sessionEnv.jointGroup,
                new CompetitionSessionExt.SessionProgressiveCallable<
                        Void, JointException, JointGroupRotatingProgress, Competing>() {
                    @Override
                    public ProgressivePromise<Void, JointException, JointGroupRotatingProgress>
                    call(CompetitionSession session, Competing competing) {
                        return sessionEnv.jointGroup.rotate(session, optionSequenceMap);
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

    public JointGroup createJointGroup(List<String> jointIds) {
        if (jointIds == null || jointIds.isEmpty()) {
            throw new IllegalArgumentException("Argument jointIds is null or an empty list.");
        }

        LinkedList<JointDevice> jointDevices = new LinkedList<>();
        HashSet<String> idSet = new HashSet<>();
        for (String jointId : jointIds) {
            if (idSet.add(jointId)) {
                jointDevices.add(mJointList.get(jointId).getDevice());
            }
        }

        return new JointGroup(mMotionService, jointDevices);
    }

    public JointGroup createJointGroup(String... jointIds) {
        return createJointGroup(Arrays.asList(jointIds));
    }

    public JointGroup createJointGroup() {
        LinkedList<JointDevice> jointDevices = new LinkedList<>();
        for (Joint joint : mJointList.all()) {
            jointDevices.add(joint.getDevice());
        }

        return new JointGroup(mMotionService, jointDevices);
    }

    /**
     * 获取移动装置
     *
     * @return 移动装置
     */
    public Locomotor getLocomotor() {
        return mLocomotorGetter.get();
    }

    public ProgressivePromise<Void, LocomotionException, LocomotionProgress>
    locomote(final List<LocomotionOption> optionSequence) {
        final SessionEnv sessionEnv = getLocomotorSession();
        return sessionEnv.session.execute(
                mLocomotorGetter.get(),
                new CompetitionSessionExt.SessionProgressiveCallable<
                        Void, LocomotionException, LocomotionProgress, Competing>() {
                    @Override
                    public ProgressivePromise<Void, LocomotionException, LocomotionProgress>
                    call(CompetitionSession session, Competing competing) {
                        return sessionEnv.locomotor.locomote(session, optionSequence);
                    }
                },
                new CompetitionSessionExt.Converter<LocomotionException>() {
                    @Override
                    public LocomotionException convert(ActivateException e) {
                        return new LocomotionException.Factory().occupied(e);
                    }
                }
        );
    }

    public ProgressivePromise<Void, LocomotionException, LocomotionProgress>
    locomote(LocomotionOption... optionSequence) {
        return locomote(Arrays.asList(optionSequence));
    }

    /**
     * 以默认速度朝某个方位移动一定距离
     *
     * @param angle    移动方位
     * @param distance 移动距离
     */
    public ProgressivePromise<Void, LocomotionException, LocomotionProgress>
    moveStraightBy(float angle, float distance) {
        return locomote(new LocomotionOption.Builder().setMovingAngle(angle)
                .setMovingDistance(distance)
                .setMovingSpeed(mLocomotorGetter.get().getDevice().getDefaultMovingSpeed()).build());
    }

    /**
     * 以一定速度朝某个方位移动一定距离
     *
     * @param angle    移动方位
     * @param distance 移动距离
     * @param speed    移动速度
     */
    public ProgressivePromise<Void, LocomotionException, LocomotionProgress>
    moveStraightBy(float angle, float distance, float speed) {
        return locomote(new LocomotionOption.Builder().setMovingAngle(angle)
                .setMovingDistance(distance).setMovingSpeed(speed).build());
    }


    /**
     * 一定时间内朝某个方位移动一定距离
     *
     * @param angle    移动方位
     * @param distance 移动距离
     * @param duration 移动时间
     */
    public ProgressivePromise<Void, LocomotionException, LocomotionProgress>
    moveStraightBy(float angle, float distance, long duration) {
        return locomote(new LocomotionOption.Builder().setMovingAngle(angle)
                .setMovingDistance(distance).setDuration(duration).build());
    }

    /**
     * 以一定速度朝某个方位持续移动
     *
     * @param angle 移动方位
     * @param speed 移动速度
     */
    public ProgressivePromise<Void, LocomotionException, LocomotionProgress>
    moveStraight(float angle, float speed) {
        return locomote(new LocomotionOption.Builder().setMovingAngle(angle)
                .setMovingSpeed(speed).build());
    }

    /**
     * 以默认速度朝某个方位持续移动
     *
     * @param angle 移动方位
     */
    public ProgressivePromise<Void, LocomotionException, LocomotionProgress>
    moveStraight(float angle) {
        return locomote(new LocomotionOption.Builder().setMovingAngle(angle).setMovingSpeed(
                mLocomotorGetter.get().getDevice().getDefaultMovingSpeed()).build());
    }

    /**
     * 以默认速度旋转一定角度
     *
     * @param angle 旋转角度。正值顺时针旋转，负值逆时针旋转
     */
    public ProgressivePromise<Void, LocomotionException, LocomotionProgress>
    turnBy(float angle) {
        return locomote(new LocomotionOption.Builder().setTurningAngle(angle).setTurningSpeed(
                mLocomotorGetter.get().getDevice().getDefaultTurningSpeed()).build());
    }

    /**
     * 以一定速度旋转一定角度
     *
     * @param angle 旋转角度。正值顺时针旋转，负值逆时针旋转
     * @param speed 旋转速度
     */
    public ProgressivePromise<Void, LocomotionException, LocomotionProgress>
    turnBy(float angle, float speed) {
        return locomote(new LocomotionOption.Builder().setTurningAngle(angle)
                .setTurningSpeed(speed).build());
    }

    /**
     * 一定时间内旋转一定角度
     *
     * @param angle    旋转角度。正值顺时针旋转，负值逆时针旋转
     * @param duration 旋转时间
     */
    public ProgressivePromise<Void, LocomotionException, LocomotionProgress>
    turnBy(float angle, long duration) {
        return locomote(new LocomotionOption.Builder().setTurningAngle(angle)
                .setDuration(duration).build());
    }

    /**
     * 以一定速度向某个时针方向持续旋转
     *
     * @param speed 旋转速度。正为顺时针，负为逆时针
     */
    public ProgressivePromise<Void, LocomotionException, LocomotionProgress>
    turn(float speed) {
        return locomote(new LocomotionOption.Builder().setTurningSpeed(speed).build());
    }

    /**
     * 以默认速度向某个时针方向持续旋转
     *
     * @param clockwise 是否顺时针
     */
    public ProgressivePromise<Void, LocomotionException, LocomotionProgress>
    turn(boolean clockwise) {
        return locomote(new LocomotionOption.Builder().setTurningSpeed(mLocomotorGetter.get()
                .getDevice().getDefaultTurningSpeed() * (clockwise ? 1 : -1)).build());
    }

    public Promise<Boolean, AccessServiceException> isLocomoting() {
        return mLocomotorGetter.get().isLocomoting();
    }

    public Promise<Void, ExecuteException> executeScript(final String scriptId) {
        LinkedList<String> jointIds = new LinkedList<>();
        for (Joint joint : mJointList.all()) {
            jointIds.add(joint.getId());
        }

        final SessionEnv sessionEnv = getSession(jointIds, mLocomotorGetter.exists(), true);
        return sessionEnv.session.execute(
                mScriptExecutor,
                new CompetitionSessionExt.SessionCallable<
                        Void, ExecuteException, Competing>() {
                    @Override
                    public Promise<Void, ExecuteException>
                    call(CompetitionSession session, Competing competing) {
                        return sessionEnv.scriptExecutor.execute(session, scriptId);
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

    public Promise<Void, JointException> release(final String jointId) {
        SessionEnv sessionEnv = getSession(Collections.singletonList(jointId), false, false);
        return sessionEnv.session.execute(
                mJointList.get(jointId),
                new CompetitionSessionExt.SessionCallable<Void, JointException, Competing>() {
                    @Override
                    public Promise<Void, JointException> call(CompetitionSession session, Competing competing) {
                        return mJointList.get(jointId).release(session);
                    }
                }, new CompetitionSessionExt.Converter<JointException>() {
                    @Override
                    public JointException convert(ActivateException e) {
                        return new JointException.Factory().occupied(e);
                    }
                });
    }

    public Promise<Void, JointException> release(String... jointIds) {
        return release(Arrays.asList(jointIds));
    }

    public Promise<Void, JointException> release(final List<String> jointIdList) {
        final SessionEnv sessionEnv = getSession(jointIdList, false, false);
        return sessionEnv.session.execute(
                sessionEnv.jointGroup,
                new CompetitionSessionExt.SessionCallable<Void, JointException, Competing>() {
                    @Override
                    public Promise<Void, JointException>
                    call(CompetitionSession session, Competing competing) {
                        return sessionEnv.jointGroup.release(session, jointIdList);
                    }
                }, new CompetitionSessionExt.Converter<JointException>() {
                    @Override
                    public JointException convert(ActivateException e) {
                        return new JointException.Factory().occupied(e);
                    }
                });
    }

    public Promise<Boolean, JointException> isReleased(String jointId) {
        return mJointList.get(jointId).isReleased();
    }

    public Promise<Map<String, Boolean>, JointException> isReleased(String... jointIds) {
        return createJointGroup(Arrays.asList(jointIds)).isReleased();
    }

    public Promise<Map<String, Boolean>, JointException> isReleased(List<String> jointIdList) {
        return createJointGroup(jointIdList).isReleased();
    }

    private static class SessionEnv {

        JointGroup jointGroup;
        Locomotor locomotor;
        MotionScriptExecutor scriptExecutor;
        CompetitionSessionExt<Competing> session;

        public SessionEnv(CompetitionSessionExt<Competing> session) {
            this.session = session;
        }
    }
}