package com.ubtrobot.dance.player;

import com.ubtrobot.async.DoneCallback;
import com.ubtrobot.async.FailCallback;
import com.ubtrobot.async.Promise;
import com.ubtrobot.motion.ExecuteException;
import com.ubtrobot.motion.MotionManager;
import com.ubtrobot.play.AbstractSegmentPlayer;
import com.ubtrobot.play.PlayException;
import com.ubtrobot.play.Segment;

import org.json.JSONException;
import org.json.JSONObject;

public class ArmMotionSegmentPlayer extends AbstractSegmentPlayer<ArmMotionSegmentPlayer.ArmMotionOption> {

    private MotionManager mMotionManager;
    private Promise<Void, ExecuteException> mExecutePromise;

    public ArmMotionSegmentPlayer(MotionManager motionManager, Segment segment) {
        super(segment);
        mMotionManager = motionManager;
    }

    public static ArmMotionOption parser(JSONObject optionJson) {
        try {
            return new ArmMotionOption(optionJson.getString(ArmMotionOptionKey.ID));
        } catch (JSONException e) {
            throw new IllegalStateException("Please check json: motion track.");
        }
    }

    @Override
    protected void onLoopStart(ArmMotionOption option) {
        mExecutePromise = mMotionManager.executeScript(
                option.getId()).done(new DoneCallback<Void>() {
            @Override
            public void onDone(Void aVoid) {
                mExecutePromise = null;
            }
        }).fail(new FailCallback<ExecuteException>() {
            @Override
            public void onFail(ExecuteException e) {
                notifyLoopAborted(new PlayException.Factory().from(e.getCode(), e.getMessage()));
                mExecutePromise = null;
            }
        });
    }

    @Override
    protected void onLoopStop() {
        mMotionManager.executeScript("reset");

        if (mExecutePromise == null) {
            return;
        }
        mExecutePromise.cancel();
        mExecutePromise = null;
    }

    @Override
    protected void onEnd() {

    }

    private static final class ArmMotionOptionKey {

        private static final String ID = "id";

        private ArmMotionOptionKey() {
        }
    }

    public static class ArmMotionOption {

        private String id;

        public ArmMotionOption(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return "ArmMotionOption{" +
                    "id='" + id + '\'' +
                    '}';
        }
    }
}
