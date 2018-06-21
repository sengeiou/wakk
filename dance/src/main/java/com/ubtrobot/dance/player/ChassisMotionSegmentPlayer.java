package com.ubtrobot.dance.player;

import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.motion.LocomotionException;
import com.ubtrobot.motion.LocomotionOption;
import com.ubtrobot.motion.LocomotionProgress;
import com.ubtrobot.motion.MotionManager;
import com.ubtrobot.play.AbstractSegmentPlayer;
import com.ubtrobot.play.Segment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class ChassisMotionSegmentPlayer extends
        AbstractSegmentPlayer<List<ChassisMotionSegmentPlayer.ChassisMotionOption>> {

    private MotionManager mMotionManager;
    private ProgressivePromise<Void, LocomotionException, LocomotionProgress> mProgressivePromise;

    public ChassisMotionSegmentPlayer(MotionManager motionManager, Segment segment) {
        super(segment);
        mMotionManager = motionManager;
    }

    @Override
    protected void onLoopStart(List<ChassisMotionOption> chassisMotionOptions) {
        LinkedList<LocomotionOption> locomotionOptions = new LinkedList<>();
        for (ChassisMotionOption option : chassisMotionOptions) {
            LocomotionOption locomotionOption = new LocomotionOption.Builder().
                    setDuration(option.getDuration()).
                    setMovingAngle(option.getMovingAngle()).
                    setMovingDistance(option.getMovingDistance()).
                    setMovingSpeed(option.getMovingSpeed()).
                    setTurningAngle(option.getTurningAngle()).
                    setTurningSpeed(option.getTurningSpeed()).
                    build();
            locomotionOptions.add(locomotionOption);
        }

        mProgressivePromise = mMotionManager.locomote(locomotionOptions);
    }

    @Override
    protected void onLoopStop() {
        cancel();
    }

    private void cancel() {
        if (mProgressivePromise == null) {
            return;
        }
        mProgressivePromise.cancel();
        mProgressivePromise = null;
    }

    @Override
    protected void onEnd() {
        cancel();
    }

    public static List<ChassisMotionOption> parser(JSONObject optionJson) {
        JSONArray jsonArray;
        try {
            jsonArray = optionJson.getJSONArray(ChassisMotionOptionKey.CHASSIS_MOTIONS);
        } catch (JSONException e) {
            throw new IllegalStateException("Please check json: chassis motion track.");
        }

        LinkedList<ChassisMotionOption> options = new LinkedList<>();

        for (int i = 0, len = jsonArray.length(); i < len; i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                options.add(createMotionOption(jsonObject));
            } catch (JSONException e) {
                throw new IllegalStateException("Please check json: chassis motion track.");
            }
        }

        return options;
    }

    private static ChassisMotionOption createMotionOption(JSONObject optionJson) {
        return new ChassisMotionOption().
                setMovingSpeed((float) optionJson.optDouble(ChassisMotionOptionKey.MOVING_SPEED)).
                setMovingAngle((float) optionJson.optDouble(ChassisMotionOptionKey.MOVING_ANGLE)).
                setMovingDistance(
                        (float) optionJson.optDouble(ChassisMotionOptionKey.MOVING_DISTANCE)).
                setTurningSpeed((float) optionJson.optDouble(ChassisMotionOptionKey.TURNING_SPEED)).
                setTurningAngle((float) optionJson.optDouble(ChassisMotionOptionKey.TURNING_ANGLE)).
                setDuration(optionJson.optLong(ChassisMotionOptionKey.DURATION));
    }

    private static final class ChassisMotionOptionKey {
        private static final String CHASSIS_MOTIONS = "chassisMotions";

        private static final String MOVING_SPEED = "movingSpeed";
        private static final String MOVING_ANGLE = "movingAngle";
        private static final String MOVING_DISTANCE = "movingDistance";

        private static final String TURNING_SPEED = "turningSpeed";
        private static final String TURNING_ANGLE = "turningAngle";

        private static final String DURATION = "duration";

        private ChassisMotionOptionKey() {
        }
    }

    public static class ChassisMotionOption {

        private float movingSpeed;
        private float movingAngle;
        private float movingDistance;

        private float turningSpeed;
        private float turningAngle;

        private long duration;

        private ChassisMotionOption() {
        }

        private ChassisMotionOption setMovingSpeed(float movingSpeed) {
            this.movingSpeed = movingSpeed;
            return this;
        }

        private ChassisMotionOption setMovingAngle(float movingAngle) {
            this.movingAngle = movingAngle;
            return this;
        }

        private ChassisMotionOption setMovingDistance(float movingDistance) {
            this.movingDistance = movingDistance;
            return this;
        }

        private ChassisMotionOption setTurningSpeed(float turningSpeed) {
            this.turningSpeed = turningSpeed;
            return this;
        }

        private ChassisMotionOption setTurningAngle(float turningAngle) {
            this.turningAngle = turningAngle;
            return this;
        }

        private ChassisMotionOption setDuration(long duration) {
            this.duration = duration;
            return this;
        }

        private float getMovingSpeed() {
            return movingSpeed;
        }

        private float getMovingAngle() {
            return movingAngle;
        }

        private float getMovingDistance() {
            return movingDistance;
        }

        private float getTurningSpeed() {
            return turningSpeed;
        }

        private float getTurningAngle() {
            return turningAngle;
        }

        private long getDuration() {
            return duration;
        }

        @Override
        public String toString() {
            return "ChassisMotionOption{" +
                    ", movingSpeed=" + movingSpeed +
                    ", movingAngle=" + movingAngle +
                    ", movingDistance=" + movingDistance +
                    ", turningSpeed=" + turningSpeed +
                    ", turningAngle=" + turningAngle +
                    ", duration=" + duration +
                    '}';
        }
    }
}
