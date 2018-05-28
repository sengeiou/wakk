package com.ubtrobot.motion;

import android.os.Handler;
import android.os.Looper;

import com.google.protobuf.BoolValue;
import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.competition.Competing;
import com.ubtrobot.master.competition.CompetingItem;
import com.ubtrobot.master.competition.CompetitionSession;
import com.ubtrobot.motion.ipc.MotionConstants;
import com.ubtrobot.motion.ipc.MotionConverters;
import com.ubtrobot.motion.ipc.MotionProto;
import com.ubtrobot.transport.message.CallException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Locomotor implements Competing {

    private final ProtoCallAdapter mMotionService;
    private final LocomotorDevice mDevice;

    private final Handler mHandler;

    Locomotor(ProtoCallAdapter motionService, LocomotorDevice device) {
        mMotionService = motionService;
        mDevice = device;

        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public List<CompetingItem> getCompetingItems() {
        return Collections.singletonList(new CompetingItem(MotionConstants.SERVICE_NAME,
                MotionConstants.COMPETING_ITEM_LOCOMOTOR));
    }

    public String getId() {
        return mDevice.getId();
    }

    public LocomotorDevice getDevice() {
        return mDevice;
    }

    public ProgressivePromise<Void, LocomotionException, LocomotionProgress>
    locomote(CompetitionSession session, List<LocomotionOption> optionSequence) {
        if (session == null || !session.isAccessible(this)) {
            throw new IllegalArgumentException("The session is null or does not contain " +
                    "the competing of the joint group.");
        }

        return new ProtoCallAdapter(
                session.createSystemServiceProxy(MotionConstants.SERVICE_NAME),
                mHandler
        ).callStickily(
                MotionConstants.CALL_PATH_LOCOMOTE,
                MotionConverters.toLocomotionOptionSequenceProto(optionSequence),
                new ProtoCallAdapter.FPProtoConverter<
                        LocomotionException, LocomotionProgress, MotionProto.LocomotionProgress>() {
                    @Override
                    public LocomotionException convertFail(CallException e) {
                        return new LocomotionException.Factory().from(e);
                    }

                    @Override
                    public Class<MotionProto.LocomotionProgress> progressProtoClass() {
                        return MotionProto.LocomotionProgress.class;
                    }

                    @Override
                    public LocomotionProgress
                    convertProgress(MotionProto.LocomotionProgress progress) {
                        return MotionConverters.toLocomotionProgressPojo(progress);
                    }
                }
        );
    }

    public ProgressivePromise<Void, LocomotionException, LocomotionProgress>
    locomote(CompetitionSession session, LocomotionOption... optionSequence) {
        return locomote(session, Arrays.asList(optionSequence));
    }

    /**
     * 以默认速度朝某个方位移动一定距离
     *
     * @param session  竞争会话
     * @param angle    移动方位
     * @param distance 移动距离
     */
    public ProgressivePromise<Void, LocomotionException, LocomotionProgress>
    moveStraightBy(CompetitionSession session, float angle, float distance) {
        return locomote(session, new LocomotionOption.Builder().setMovingAngle(angle)
                .setMovingDistance(distance).setMovingSpeed(mDevice.getDefaultMovingSpeed()).build());
    }

    /**
     * 以一定速度朝某个方位移动一定距离
     *
     * @param session  竞争会话
     * @param angle    移动方位
     * @param distance 移动距离
     * @param speed    移动速度
     */
    public ProgressivePromise<Void, LocomotionException, LocomotionProgress>
    moveStraightBy(CompetitionSession session, float angle, float distance, float speed) {
        return locomote(session, new LocomotionOption.Builder().setMovingAngle(angle)
                .setMovingDistance(distance).setMovingSpeed(speed).build());
    }


    /**
     * 一定时间内朝某个方位移动一定距离
     *
     * @param session  竞争会话
     * @param angle    移动方位
     * @param distance 移动距离
     * @param duration 移动时间
     */
    public ProgressivePromise<Void, LocomotionException, LocomotionProgress>
    moveStraightBy(CompetitionSession session, float angle, float distance, long duration) {
        return locomote(session, new LocomotionOption.Builder().setMovingAngle(angle)
                .setMovingDistance(distance).setDuration(duration).build());
    }

    /**
     * 以一定速度朝某个方位持续移动
     *
     * @param session 竞争会话
     * @param angle   移动方位
     * @param speed   移动速度
     */
    public ProgressivePromise<Void, LocomotionException, LocomotionProgress>
    moveStraight(CompetitionSession session, float angle, float speed) {
        return locomote(session, new LocomotionOption.Builder().setMovingAngle(angle)
                .setMovingSpeed(speed).build());
    }

    /**
     * 以默认速度朝某个方位持续移动
     *
     * @param session 竞争会话
     * @param angle   移动方位
     */
    public ProgressivePromise<Void, LocomotionException, LocomotionProgress>
    moveStraight(CompetitionSession session, float angle) {
        return locomote(session, new LocomotionOption.Builder().setMovingAngle(angle)
                .setMovingSpeed(mDevice.getDefaultMovingSpeed()).build());
    }

    /**
     * 以默认速度旋转一定角度
     *
     * @param session 竞争会话
     * @param angle   旋转角度。正值顺时针旋转，负值逆时针旋转
     */
    public ProgressivePromise<Void, LocomotionException, LocomotionProgress>
    turnBy(CompetitionSession session, float angle) {
        return locomote(session, new LocomotionOption.Builder().setTurningAngle(angle)
                .setTurningSpeed(mDevice.getDefaultTurningSpeed()).build());
    }

    /**
     * 以一定速度旋转一定角度
     *
     * @param session 竞争会话
     * @param angle   旋转角度。正值顺时针旋转，负值逆时针旋转
     * @param speed   旋转速度
     */
    public ProgressivePromise<Void, LocomotionException, LocomotionProgress>
    turnBy(CompetitionSession session, float angle, float speed) {
        return locomote(session, new LocomotionOption.Builder().setTurningAngle(angle)
                .setTurningSpeed(speed).build());
    }

    /**
     * 一定时间内旋转一定角度
     *
     * @param session  竞争会话
     * @param angle    旋转角度。正值顺时针旋转，负值逆时针旋转
     * @param duration 旋转时间
     */
    public ProgressivePromise<Void, LocomotionException, LocomotionProgress>
    turnBy(CompetitionSession session, float angle, long duration) {
        return locomote(session, new LocomotionOption.Builder().setTurningAngle(angle)
                .setDuration(duration).build());
    }

    /**
     * 以一定速度向某个时针方向持续旋转
     *
     * @param session 竞争会话
     * @param speed   旋转速度。正为顺时针，负为逆时针
     */
    public ProgressivePromise<Void, LocomotionException, LocomotionProgress>
    turn(CompetitionSession session, float speed) {
        return locomote(session, new LocomotionOption.Builder().setTurningSpeed(speed).build());
    }

    /**
     * 以默认速度向某个时针方向持续旋转
     *
     * @param session   竞争会话
     * @param clockwise 是否顺时针
     */
    public ProgressivePromise<Void, LocomotionException, LocomotionProgress>
    turn(CompetitionSession session, boolean clockwise) {
        return locomote(session, new LocomotionOption.Builder().setTurningSpeed(
                mDevice.getDefaultTurningSpeed() * (clockwise ? 1 : -1)).build());
    }

    public Promise<Boolean, AccessServiceException> isLocomoting() {
        return mMotionService.call(
                MotionConstants.CALL_PATH_QUERY_IS_LOCOMOTING,
                new ProtoCallAdapter.DFProtoConverter<Boolean, BoolValue, AccessServiceException>() {
                    @Override
                    public Class<BoolValue> doneProtoClass() {
                        return BoolValue.class;
                    }

                    @Override
                    public Boolean convertDone(BoolValue locomoting) throws Exception {
                        return locomoting.getValue();
                    }

                    @Override
                    public AccessServiceException convertFail(CallException e) {
                        return new AccessServiceException.Factory().from(e);
                    }
                }
        );
    }
}