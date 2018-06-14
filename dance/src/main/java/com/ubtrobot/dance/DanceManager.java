package com.ubtrobot.dance;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.async.AsyncTaskParallel;
import com.ubtrobot.async.DoneCallback;
import com.ubtrobot.async.FailCallback;
import com.ubtrobot.async.ProgressCallback;
import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.dance.music.MusicPlay;
import com.ubtrobot.dance.player.ArmMotionSegmentPlayer;
import com.ubtrobot.dance.player.ChassisMotionSegmentPlayer;
import com.ubtrobot.dance.player.EmotionSegmentPlayer;
import com.ubtrobot.dance.player.MusicSegmentPlayer;
import com.ubtrobot.emotion.EmotionManager;
import com.ubtrobot.master.Master;
import com.ubtrobot.motion.MotionManager;
import com.ubtrobot.play.PlayException;
import com.ubtrobot.play.Player;
import com.ubtrobot.play.PlayerFactory;
import com.ubtrobot.play.Segment;
import com.ubtrobot.play.Track;
import com.ubtrobot.play.TrackPlayer;
import com.ubtrobot.speech.SpeechManager;
import com.ubtrobot.speech.SynthesizeException;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import static com.ubtrobot.dance.ipc.DanceConstants.TYPE_ARM_MOTION;
import static com.ubtrobot.dance.ipc.DanceConstants.TYPE_CHASSIS_MOTION;
import static com.ubtrobot.dance.ipc.DanceConstants.TYPE_EMOTION;
import static com.ubtrobot.dance.ipc.DanceConstants.TYPE_MUSIC;

public class DanceManager {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("DanceManager");

    private static final String ARM_STATE_URI = "content://com.ubtechinc.settings.provider/CruiserSettings";

    private static volatile Context mContext;
    private static volatile DanceManager mDanceManager;
    private static volatile DanceList mDanceList;

    private SpeechManager mSpeechManager;

    private String mCurrentCategory;

    private static AsyncTaskParallel<PlayException> mTaskParallel;
    private static ProgressivePromise<Void, PlayException, Map.Entry<String,
            AsyncTaskParallel.DoneOrFail<Object, PlayException>>> mProgressivePromise;

    public DanceManager(Context context) {
        if (mDanceManager != null) {
            return;
        }

        synchronized (this) {
            if (mDanceManager != null) {
                return;
            }

            mDanceManager = this;
            mContext = context.getApplicationContext();
            mDanceList = new DanceList(context);
        }
    }

    public List<Dance> getDanceList() {
        if (mDanceList.all().size() <= 0) {
            throw new IllegalStateException("Pleases add dance.json");
        }
        return mDanceList.all();
    }

    private boolean checkTrack() {
        String armUsable = "true";
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = Uri.parse(ARM_STATE_URI);
        String info = null;
        Bundle bundle = cr.call(uri,
                "getSettings", "cruiser_hand_motion_state", null);
        if (bundle != null) {
            info = bundle.getString("value");
        }

        if (armUsable.equals(info)) {
            return true;
        }

        return false;
    }

    public ProgressivePromise<Void, PlayException,
            Map.Entry<String, AsyncTaskParallel.DoneOrFail<Object, PlayException>>>
    play(String danceCategory) {
        if (!checkTrack()) {
            LOGGER.w("Arm motion off.");
            ttsArmForbidden();
            return null;
        }

        final Dance dance = mDanceList.get(danceCategory);
        mCurrentCategory = danceCategory;

        if (mTaskParallel != null) {
            LOGGER.w("Cancel dance: dance is running.");
            if (mProgressivePromise != null) {
                mProgressivePromise.cancel();
            }
            mProgressivePromise = null;
            mTaskParallel = null;
        }

        mTaskParallel = new AsyncTaskParallel<>();

        for (final Track track : dance.getTracks()) {
            mTaskParallel.put(track.getType(), new ParallelAsyncTask(track));
        }

        ttsDance();
        mTaskParallel.start();
        mProgressivePromise = mTaskParallel.promise().done(new DoneCallback<Void>() {
            @Override
            public void onDone(Void aVoid) {
                LOGGER.w("Parallel dance: onDone");
                mProgressivePromise = null;
                mTaskParallel = null;
            }
        }).fail(new FailCallback<PlayException>() {
            @Override
            public void onFail(PlayException e) {
                LOGGER.w("Parallel dance: onFail");
            }
        }).progress(new ProgressCallback<Map.Entry<String,
                AsyncTaskParallel.DoneOrFail<Object, PlayException>>>() {
            @Override
            public void onProgress(Map.Entry<String,
                    AsyncTaskParallel.DoneOrFail<Object, PlayException>> stringDoneOrFailEntry) {
                if (mTaskParallel == null) {
                    return;
                }

                Iterator<String> iterator = mTaskParallel.getDoneOrFailMap().keySet().iterator();
                while (iterator.hasNext()) {
                    if (dance.getMainType().equals(iterator.next())) {
                        mTaskParallel.cancel();
                        mProgressivePromise = null;
                        mTaskParallel = null;
                    }
                }
            }
        });

        return mProgressivePromise;
    }

    private void ttsArmForbidden() {
        String armMotionOff = "The arm is forbidden now, I can't dance for you.";

        if (Language.causedByZh()) {
            armMotionOff = "手臂动作已关闭，我暂时不能为您跳舞";
        } else if (Language.causedByEn()) {
            armMotionOff = "The arm is forbidden now, I can't dance for you.";
        }

        tts(armMotionOff);
    }

    private void ttsDance() {
        String dance = "I'm going to start dancing. Keep a distance of one meter with me";

        if (Language.causedByZh()) {
            dance = "我要开始跳舞了，请跟我保持一些距离";
        } else if (Language.causedByEn()) {
            dance = "I'm going to start dancing. Keep a distance of one meter with me";
        }

        tts(dance);
    }

    private void tts(String ttsInfo) {
        if (mSpeechManager == null) {
            mSpeechManager = new SpeechManager(Master.get().getGlobalContext());
        }

        mSpeechManager.synthesize(ttsInfo).done(new DoneCallback<Void>() {
            @Override
            public void onDone(Void aVoid) {
            }
        }).fail(new FailCallback<SynthesizeException>() {
            @Override
            public void onFail(SynthesizeException e) {
                LOGGER.e("Speech synthesize: arm motion off fail." + e);
            }
        });
    }

    public String getCurrentDance() {
        return mCurrentCategory;
    }

    public String getLastDance() {
        int count = mDanceList.all().size();
        int lastIndex = (getCurrentDanceIndex() - 1 + count) % count;
        return mDanceList.all().get(lastIndex).getCategory();
    }

    private int getCurrentDanceIndex() {
        int currentIndex = 0;
        int count = mDanceList.all().size();

        for (int i = 0; i < count; i++) {
            if (mDanceList.all().get(i).equals(mCurrentCategory)) {
                currentIndex = i;
                break;
            }
        }

        return currentIndex;
    }

    public String getNextDance() {
        int count = mDanceList.all().size();
        int nextIndex = (getCurrentDanceIndex() + 1) % count;
        return mDanceList.all().get(nextIndex).getCategory();
    }

    public String getRandomDance() {
        int danceIndex = Math.abs(new Random().nextInt()) % mDanceList.all().size();
        if (mCurrentCategory != null && mCurrentCategory.length() > 1) {
            if (danceIndex == getCurrentDanceIndex()) {
                danceIndex = (danceIndex + 1) % mDanceList.all().size();
            }
        }
        LOGGER.w("Dance random index:" + danceIndex);
        return mDanceList.all().get(danceIndex).getCategory();
    }

    public ProgressivePromise<Void, PlayException, Map.Entry<String,
            AsyncTaskParallel.DoneOrFail<Object, PlayException>>> getProgressivePromise() {
        return mProgressivePromise;
    }

    private class ParallelAsyncTask extends AsyncTask<Object, PlayException> {

        private Track mTrack;
        private Promise<Void, PlayException> mPromise;

        public ParallelAsyncTask(Track track) {
            this.mTrack = track;
        }

        @Override
        protected void onStart() {
            TrackPlayer player = new TrackPlayer(mTrack, new TrackPlayerFactory(
                    mContext, mTrack.getType()));

            mPromise = player.play();
            mPromise.done(new DoneCallback<Void>() {
                @Override
                public void onDone(Void aVoid) {
                    resolve(aVoid);
                }
            }).fail(new FailCallback<PlayException>() {
                @Override
                public void onFail(PlayException e) {
                    reject(e);
                }
            });
        }

        @Override
        protected void onCancel() {
            mPromise.cancel();
            mTaskParallel = null;
        }
    }

    private class TrackPlayerFactory implements PlayerFactory {

        private Context mContext;
        private String mType;

        private TrackPlayerFactory(Context context, String type) {
            mContext = context;
            this.mType = type;
        }

        @Override
        public Player createPlayer(Segment segment) {
            LOGGER.w("type:" + mType + "\t segment:" + segment.toString());
            switch (mType) {
                case TYPE_MUSIC:
                    return new MusicSegmentPlayer(new MusicPlay(mContext), segment);
                case TYPE_EMOTION:
                    return new EmotionSegmentPlayer(
                            new EmotionManager(Master.get().getGlobalContext()), segment);
                case TYPE_ARM_MOTION:
                    return new ArmMotionSegmentPlayer(
                            new MotionManager(Master.get().getGlobalContext()), segment);
                case TYPE_CHASSIS_MOTION:
                    return new ChassisMotionSegmentPlayer(
                            new MotionManager(Master.get().getGlobalContext()), segment);
                default:
                    throw new IllegalStateException("Type:" + mType + " is not exits.");
            }
        }
    }

    private static class Language {

        private static final String ZH = "zh";
        private static final String EN = "en";

        private static String getCurrentLanguage() {
            return getLocale().getLanguage();
        }

        private static Locale getLocale() {
            Locale locale;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locale = LocaleList.getDefault().get(0);
            } else {
                locale = Locale.getDefault();
            }
            return locale;
        }

        private static boolean causedByZh() {
            return ZH.toLowerCase().equals(getCurrentLanguage().toLowerCase());
        }

        private static boolean causedByEn() {
            return EN.toLowerCase().equals(getCurrentLanguage().toLowerCase());
        }
    }

}
