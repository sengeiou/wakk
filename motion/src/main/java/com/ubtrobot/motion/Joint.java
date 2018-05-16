package com.ubtrobot.motion;

import android.os.Handler;

import com.google.protobuf.BoolValue;
import com.google.protobuf.FloatValue;
import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.competition.Competing;
import com.ubtrobot.master.competition.CompetingItem;
import com.ubtrobot.master.competition.CompetitionSession;
import com.ubtrobot.motion.ipc.MotionConstants;
import com.ubtrobot.motion.ipc.MotionConverters;
import com.ubtrobot.motion.ipc.MotionProto;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.util.Collections;
import java.util.List;

public class Joint implements Competing {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("Joint");

    private final ProtoCallAdapter mMotionService;
    private final Handler mHandler;

    private final JointDevice mDevice;

    Joint(ProtoCallAdapter motionService, JointDevice device, Handler handler) {
        mMotionService = motionService;
        mDevice = device;
        mHandler = handler;
    }

    @Override
    public List<CompetingItem> getCompetingItems() {
        return Collections.singletonList(new CompetingItem(
                MotionConstants.SERVICE_NAME,
                MotionConstants.COMPETING_ITEM_PREFIX_JOINT + getId()
        ));
    }

    public String getId() {
        return mDevice.getId();
    }

    public JointDevice getDevice() {
        return mDevice;
    }

    /**
     * 查询是否正在旋转
     *
     * @return 是否正在旋转
     */
    public boolean isRotating() {
        try {
            return mMotionService.syncCall(MotionConstants.CALL_PATH_QUERY_JOINT_IS_ROTATING,
                    BoolValue.class).getValue();
        } catch (CallException e) {
            LOGGER.e(e, "Framework error when querying if the joint is rotating.");
            return false;
        }
    }

    /**
     * 获取当前角度
     *
     * @return 当前角度
     */
    public float getAngle() {
        try {
            return mMotionService.syncCall(MotionConstants.CALL_PATH_GET_JOINT_ANGLE,
                    FloatValue.class).getValue();
        } catch (CallException e) {
            LOGGER.e(e, "Framework error when querying the joint's angle.");
            return 0;
        }
    }

    /**
     * 以默认速度旋转一定角度
     *
     * @param session 竞争会话
     * @param angle   旋转角度。正值顺时针旋转，负值逆时针旋转
     * @return promise。promise.done 通知成功后旋转到的角度
     */
    public ProgressivePromise<Void, JointException, RotatingProgress>
    rotateBy(CompetitionSession session, float angle) {
        return rotateBy(session, angle, mDevice.getDefaultSpeed());
    }

    /**
     * 以一定速度旋转一定角度
     *
     * @param session 竞争会话
     * @param angle   旋转角度。正值顺时针旋转，负值逆时针旋转
     * @param speed   旋转速度
     * @return promise。promise.done 通知成功后旋转到的角度
     */
    public ProgressivePromise<Void, JointException, RotatingProgress>
    rotateBy(CompetitionSession session, float angle, float speed) {
        checkSpeed(speed);

        return rotate(session, MotionConverters.toJointRotatingOptionProto(getId(), true, angle, speed));
    }

    private void checkSpeed(float speed) {
        if (speed <= 0) {
            throw new IllegalArgumentException("Argument speed <= 0.");
        }
    }

    private ProgressivePromise<Void, JointException, RotatingProgress>
    rotate(CompetitionSession session, MotionProto.JointRotatingOption option) {
        checkSession(session);

        ProtoCallAdapter motionService = new ProtoCallAdapter(
                session.createSystemServiceProxy(MotionConstants.SERVICE_NAME),
                mHandler
        );
        return motionService.callStickily(
                MotionConstants.CALL_PATH_JOINT_ROTATE,
                option,
                new ProtoCallAdapter.FPProtoConverter<
                        JointException, RotatingProgress, MotionProto.JointRotatingProgress>() {
                    @Override
                    public JointException convertFail(CallException e) {
                        return new JointException.Factory().from(e);
                    }

                    @Override
                    public Class<MotionProto.JointRotatingProgress> progressProtoClass() {
                        return MotionProto.JointRotatingProgress.class;
                    }

                    @Override
                    public RotatingProgress
                    convertProgress(MotionProto.JointRotatingProgress progress) {
                        return MotionConverters.toJointRotatingProgressPojo(progress);
                    }
                }
        );
    }

    private void checkSession(CompetitionSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Argument session is null.");
        }

        if (!session.containsCompeting(this)) {
            throw new IllegalArgumentException("The competition session does NOT contain the joint.");
        }
    }

    /**
     * 一定时间内旋转一定角度
     *
     * @param session    竞争会话
     * @param angle      旋转角度。正值顺时针旋转，负值逆时针旋转
     * @param timeMillis 旋转时间
     * @return promise。promise.done 通知成功后旋转到的角度
     */
    public ProgressivePromise<Void, JointException, RotatingProgress>
    rotateBy(CompetitionSession session, float angle, long timeMillis) {
        checkTimeMillis(timeMillis);

        return rotate(session, MotionConverters.toJointRotatingOptionProto(getId(), true, angle,
                timeMillis));
    }

    private void checkTimeMillis(long timeMillis) {
        if (timeMillis <= 0) {
            throw new IllegalArgumentException("Argument timeMillis <= 0.");
        }
    }

    /**
     * 以默认速度旋转到某个角度
     *
     * @param session 竞争会话
     * @param angle   旋转停留角度
     * @return promise
     */
    public ProgressivePromise<Void, JointException, RotatingProgress>
    rotateTo(CompetitionSession session, float angle) {
        return rotateTo(session, angle, mDevice.getDefaultSpeed());
    }

    /**
     * 以一定速度旋转到某个角度
     *
     * @param session 竞争会话
     * @param angle   旋转停留角度
     * @param speed   旋转速度
     * @return promise
     */
    public ProgressivePromise<Void, JointException, RotatingProgress>
    rotateTo(CompetitionSession session, float angle, float speed) {
        checkSpeed(speed);

        return rotate(session, MotionConverters.toJointRotatingOptionProto(getId(), false, angle,
                speed));
    }

    /**
     * 一定时间内旋转到某个角度
     *
     * @param session    竞争会话
     * @param angle      旋转停留角度
     * @param timeMillis 旋转时间
     * @return promise
     */
    public ProgressivePromise<Void, JointException, RotatingProgress>
    rotateTo(CompetitionSession session, float angle, long timeMillis) {
        checkTimeMillis(timeMillis);

        return rotate(session, MotionConverters.toJointRotatingOptionProto(getId(), false, angle,
                timeMillis));
    }

    public static class RotatingProgress {

        public static final int STATE_BEGAN = 0;
        public static final int STATE_ENDED = 1;

        private final int state;
        private final float currentAngle;
        private final float rotatedAngle;
        private final float rotatedTimeMillis;

        public RotatingProgress(
                int state,
                float currentAngle,
                float rotatedAngle,
                float rotatedTimeMillis) {
            this.state = state;
            this.currentAngle = currentAngle;
            this.rotatedAngle = rotatedAngle;
            this.rotatedTimeMillis = rotatedTimeMillis;
        }

        public int getState() {
            return state;
        }

        public boolean isBegan() {
            return state == STATE_BEGAN;
        }

        public boolean isEnded() {
            return state == STATE_ENDED;
        }

        public float getCurrentAngle() {
            return currentAngle;
        }

        public float getRotatedAngle() {
            return rotatedAngle;
        }

        public float getRotatedTimeMillis() {
            return rotatedTimeMillis;
        }

        @Override
        public String toString() {
            return "TurningProgress{" +
                    "state=" + state +
                    ", currentAngle=" + currentAngle +
                    ", rotatedAngle=" + rotatedAngle +
                    ", rotatedTimeMillis=" + rotatedTimeMillis +
                    '}';
        }
    }
}