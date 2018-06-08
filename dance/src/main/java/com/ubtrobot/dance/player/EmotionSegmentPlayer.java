package com.ubtrobot.dance.player;

import com.ubtrobot.async.DoneCallback;
import com.ubtrobot.async.FailCallback;
import com.ubtrobot.async.Promise;
import com.ubtrobot.emotion.EmotionManager;
import com.ubtrobot.emotion.ExpressException;
import com.ubtrobot.emotion.ExpressOption;
import com.ubtrobot.play.AbstractSegmentPlayer;
import com.ubtrobot.play.PlayException;
import com.ubtrobot.play.Segment;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import org.json.JSONException;
import org.json.JSONObject;

public class EmotionSegmentPlayer extends AbstractSegmentPlayer<EmotionSegmentPlayer.EmotionOption> {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("EmotionSegmentPlayer");

    private EmotionManager mEmotionManager;
    Promise<Void, ExpressException> mExpressPromise;

    public EmotionSegmentPlayer(EmotionManager emotionManager, Segment segment) {
        super(segment);
        mEmotionManager = emotionManager;
    }

    @Override
    protected void onLoopStart(EmotionOption option) {
        mExpressPromise = mEmotionManager.express(option.getEmotionId(),
                new ExpressOption.Builder().
                setLoops(0).
                setDismissAfterEnd(option.isDismissAfterEnd()).
                setLoopDefaultAfterEnd(option.isLoopDefaultAfterEnd()).
                build()).done(new DoneCallback<Void>() {
            @Override
            public void onDone(Void aVoid) {
                mExpressPromise = null;
            }
        }).fail(new FailCallback<ExpressException>() {
            @Override
            public void onFail(ExpressException e) {
                LOGGER.e(e);
                notifyLoopAborted(new PlayException.Factory().from(e.getCode(),e.getMessage()));
                mExpressPromise = null;
            }
        });
    }

    @Override
    protected void onLoopStop() {
        mEmotionManager.dismiss();

        if (mExpressPromise == null) {
            return;
        }

        mExpressPromise.cancel();
        mExpressPromise = null;
    }

    @Override
    protected void onEnd() {

    }

    public static EmotionOption parser(JSONObject optionJson) {
        try {
            return new EmotionOption(
                    optionJson.getString(EmotionOptionKey.EMOTION_ID)).
                    setLoops(optionJson.optInt(EmotionOptionKey.LOOPS)).
                    setDismissAfterEnd(
                            optionJson.optBoolean(
                                    EmotionOptionKey.DISMISS_AFTER_END)).
                    setLoopDefaultAfterEnd(
                            optionJson.optBoolean(
                                    EmotionOptionKey.LOOP_DEFAULT_AFTER_END));
        } catch (JSONException e) {
            throw new IllegalStateException("Please check json: emotion track.");
        }
    }

    private final class EmotionOptionKey {
        private static final String EMOTION_ID = "emotionId";
        private static final String LOOPS = "loops";
        private static final String DISMISS_AFTER_END = "dismissAfterEnd";
        private static final String LOOP_DEFAULT_AFTER_END = "loopDefaultAfterEnd";

        private EmotionOptionKey() {
        }
    }

    public static class EmotionOption {
        private String emotionId;
        private int loops;
        private boolean dismissAfterEnd;
        private boolean loopDefaultAfterEnd;

        private EmotionOption(String emotionId) {
            this.emotionId = emotionId;
        }

        private EmotionOption setLoops(int loops) {
            this.loops = loops;
            return this;
        }

        private EmotionOption setDismissAfterEnd(boolean dismissAfterEnd) {
            this.dismissAfterEnd = dismissAfterEnd;
            return this;
        }

        private EmotionOption setLoopDefaultAfterEnd(boolean loopDefaultAfterEnd) {
            this.loopDefaultAfterEnd = loopDefaultAfterEnd;
            return this;
        }

        private String getEmotionId() {
            return emotionId;
        }

        private int getLoops() {
            return loops;
        }

        private boolean isDismissAfterEnd() {
            return dismissAfterEnd;
        }

        private boolean isLoopDefaultAfterEnd() {
            return loopDefaultAfterEnd;
        }

        @Override
        public String toString() {
            return "EmotionOption{" +
                    "emotionId='" + emotionId + '\'' +
                    ", loops='" + loops + '\'' +
                    ", dismissAfterEnd=" + dismissAfterEnd +
                    ", loopDefaultAfterEnd=" + loopDefaultAfterEnd +
                    '}';
        }
    }

}
