package com.ubtrobot.motion;

import com.ubtrobot.async.Function;
import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.ProgressivePromiseOperators;
import com.ubtrobot.async.Promise;
import com.ubtrobot.async.PromiseOperators;
import com.ubtrobot.async.ReturnInputFunction;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.competition.Competing;
import com.ubtrobot.master.competition.CompetingItem;
import com.ubtrobot.master.competition.CompetitionSession;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Joint implements Competing {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("Joint");

    private final JointDevice mDevice;
    private final JointGroup mJointGroup;

    Joint(ProtoCallAdapter motionService, JointDevice device) {
        mJointGroup = new JointGroup(motionService, Collections.singletonList(device));
        mDevice = device;
    }

    @Override
    public List<CompetingItem> getCompetingItems() {
        return mJointGroup.getCompetingItems();
    }

    public String getId() {
        return mDevice.getId();
    }

    public JointDevice getDevice() {
        return mDevice;
    }

    public ProgressivePromise<Void, JointException, JointRotatingProgress>
    rotate(CompetitionSession session, List<JointRotatingOption> optionSequence) {
        HashMap<String, List<JointRotatingOption>> optionSequenceMap = new HashMap<>();
        optionSequenceMap.put(mDevice.getId(), optionSequence);
        return ProgressivePromiseOperators.mapProgress(
                mJointGroup.rotate(session, optionSequenceMap),
                new Function<JointGroupRotatingProgress, JointRotatingProgress, JointException>() {
                    @Override
                    public JointRotatingProgress
                    apply(JointGroupRotatingProgress progress)
                            throws JointException {
                        return new JointRotatingProgress.Builder(
                                progress.getSessionId(), progress.getState()).build();
                    }
                }
        );
    }

    public ProgressivePromise<Void, JointException, JointRotatingProgress>
    rotate(CompetitionSession session, JointRotatingOption... optionSequence) {
        return rotate(session, Arrays.asList(optionSequence));
    }

    public ProgressivePromise<Void, JointException, JointRotatingProgress>
    rotateBy(CompetitionSession session, float angle) {
        return rotate(session, new JointRotatingOption.Builder()
                .setJointId(getId())
                .setAngle(angle)
                .setAngleAbsolute(false)
                .setSpeed(mDevice.getDefaultSpeed())
                .build());
    }

    public ProgressivePromise<Void, JointException, JointRotatingProgress>
    rotateBy(CompetitionSession session, float angle, float speed) {
        return rotate(session, new JointRotatingOption.Builder()
                .setJointId(getId())
                .setAngle(angle)
                .setAngleAbsolute(false)
                .setSpeed(speed)
                .build());
    }

    public ProgressivePromise<Void, JointException, JointRotatingProgress>
    rotateBy(CompetitionSession session, float angle, long duration) {
        return rotate(session, new JointRotatingOption.Builder()
                .setJointId(getId())
                .setAngle(angle)
                .setAngleAbsolute(false)
                .setDuration(duration)
                .build());
    }

    public ProgressivePromise<Void, JointException, JointRotatingProgress>
    rotateTo(CompetitionSession session, float angle) {
        return rotate(session, new JointRotatingOption.Builder()
                .setJointId(getId())
                .setAngle(angle)
                .setAngleAbsolute(true)
                .setSpeed(mDevice.getDefaultSpeed())
                .build());
    }

    public ProgressivePromise<Void, JointException, JointRotatingProgress>
    rotateTo(CompetitionSession session, float angle, float speed) {
        return rotate(session, new JointRotatingOption.Builder()
                .setJointId(getId())
                .setAngle(angle)
                .setAngleAbsolute(true)
                .setSpeed(speed)
                .build());
    }

    public ProgressivePromise<Void, JointException, JointRotatingProgress>
    rotateTo(CompetitionSession session, float angle, long duration) {
        return rotate(session, new JointRotatingOption.Builder()
                .setJointId(getId())
                .setAngle(angle)
                .setAngleAbsolute(true)
                .setDuration(duration)
                .build());
    }

    public Promise<Boolean, AccessServiceException> isRotating() {
        return PromiseOperators.mapDone(
                mJointGroup.isRotating(),
                new Function<Map<String, Boolean>, Boolean, AccessServiceException>() {
                    @Override
                    public Boolean
                    apply(Map<String, Boolean> rotatingMap) throws AccessServiceException {
                        Boolean rotating = rotatingMap.get(mDevice.getId());
                        if (rotating == null) {
                            rotating = false;
                            LOGGER.e("Query joint is rotating failed. " +
                                    "Rotating state in map NOT found.");
                        }

                        return rotating;
                    }
                }
        );
    }

    public Promise<Float, AccessServiceException> getAngle() {
        return PromiseOperators.mapDone(
                mJointGroup.getAngles(),
                new Function<Map<String, Float>, Float, AccessServiceException>() {
                    @Override
                    public Float
                    apply(Map<String, Float> angleMap) throws AccessServiceException {
                        Float angle = angleMap.get(mDevice.getId());
                        if (angle == null) {
                            angle = 0f;
                            LOGGER.e("Query the angle of joint failed. " +
                                    "joint angle in map NOT found.");
                        }

                        return angle;
                    }
                }
        );
    }

    public Promise<Void, JointException> release(CompetitionSession session) {
        return PromiseOperators.mapDone(
                mJointGroup.release(session, Arrays.asList(mDevice.getId())),
                new Function<Void, Void, JointException>() {

                    @Override
                    public Void apply(Void aVoid) throws JointException {
                        return null;
                    }
                });
    }

    public Promise<Boolean, JointException> isReleased() {
        return PromiseOperators.mapDone(
                mJointGroup.isReleased(),
                new Function<Map<String, Boolean>, Boolean, JointException>() {
                    @Override
                    public Boolean
                    apply(Map<String, Boolean> releasedMap) throws JointException {
                        Boolean isReleased = releasedMap.get(mDevice.getId());
                        if (isReleased == null) {
                            isReleased = false;
                            LOGGER.e("Query joint is released failed. " +
                                    "Released state in map NOT found.");
                        }

                        return isReleased;
                    }
                }
        );
    }

    @Override
    public String toString() {
        return "Joint{" +
                "device=" + mDevice +
                '}';
    }
}