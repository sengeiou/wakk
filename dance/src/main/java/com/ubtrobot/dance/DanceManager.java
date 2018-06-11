package com.ubtrobot.dance;

import android.content.Context;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.async.AsyncTaskParallel;
import com.ubtrobot.async.DoneCallback;
import com.ubtrobot.async.FailCallback;
import com.ubtrobot.async.ProgressCallback;
import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.dance.music.MusicPlay;
import com.ubtrobot.dance.player.ArmMotionSegmentPlayer;
import com.ubtrobot.dance.player.EmotionSegmentPlayer;
import com.ubtrobot.dance.player.ChassisMotionSegmentPlayer;
import com.ubtrobot.dance.player.MusicSegmentPlayer;
import com.ubtrobot.emotion.EmotionManager;
import com.ubtrobot.master.Master;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.motion.MotionManager;
import com.ubtrobot.play.PlayException;
import com.ubtrobot.play.Player;
import com.ubtrobot.play.PlayerFactory;
import com.ubtrobot.play.Segment;
import com.ubtrobot.play.Track;
import com.ubtrobot.play.TrackPlayer;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.ubtrobot.dance.ipc.DanceConstants.TYPE_ARM_MOTION;
import static com.ubtrobot.dance.ipc.DanceConstants.TYPE_CHASSIS_MOTION;
import static com.ubtrobot.dance.ipc.DanceConstants.TYPE_EMOTION;
import static com.ubtrobot.dance.ipc.DanceConstants.TYPE_MUSIC;

public class DanceManager {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("DanceManager");

    private static volatile DanceManager mDanceManager;

    private DanceList mDanceList;

    private AsyncTaskParallel<PlayException> mTaskParallel;
    private ProgressivePromise<Void, PlayException, Map.Entry<String,
            AsyncTaskParallel.DoneOrFail<Object, PlayException>>> mProgressivePromise;

    public DanceManager(Context context) {
        if (mDanceManager == null) {
            synchronized (this) {
                if (mDanceManager != null) {
                    return;
                }

                mDanceManager = this;
                mDanceList = new DanceList(context);
                getDanceList();
            }
        }
    }

    public List<Dance> getDanceList() {
        return mDanceList.all();
    }

    public Dance getDance(String danceName) {
        return mDanceList.get(danceName);
    }

    public Promise<Void, PlayException> play(String danceName) {
        if (!checkTrack()) {
            LOGGER.w("Dance underprepared.");
        }

        return null;
    }

    private boolean checkTrack() {
        // TODO 跳舞前的环境检查
        return false;
    }

    public ProgressivePromise<Void, PlayException,
            Map.Entry<String, AsyncTaskParallel.DoneOrFail<Object, PlayException>>>
    play(final Context context, final Dance dance) {
        if (mTaskParallel != null) {
            LOGGER.w("Cancel dance: dance is running.");
            mProgressivePromise.cancel();
            mProgressivePromise = null;
        }

        mTaskParallel = new AsyncTaskParallel<>();
        final String mainType = dance.getMainType();
        final List<Track> tracks = dance.getTracks();

        for (final Track track : tracks) {
            final String type = track.getType();
            mTaskParallel.put(type, new AsyncTask<Object, PlayException>() {
                Promise promise;

                @Override
                protected void onStart() {
                    TrackPlayer player = new TrackPlayer(track,
                            new TrackPlayerFactory(context, type));
                    promise = player.play().done(new DoneCallback() {
                        @Override
                        public void onDone(Object o) {
                            resolve(o);
                        }
                    }).fail(new FailCallback() {
                        @Override
                        public void onFail(Object o) {
                            mTaskParallel = null;
                        }
                    });
                }

                @Override
                protected void onCancel() {
                    promise.cancel();
                    mTaskParallel = null;
                }
            });
        }

        mTaskParallel.start();
        mProgressivePromise = mTaskParallel.promise().done(new DoneCallback<Void>() {
            @Override
            public void onDone(Void aVoid) {
                if (mTaskParallel == null) {
                    return;
                }

                Iterator<String> iterator = mTaskParallel.getDoneOrFailMap().keySet().iterator();
                while (iterator.hasNext()) {
                    if (mainType.equals(iterator.next())) {
                        mTaskParallel.cancel();
                        mTaskParallel = null;
                    }
                }
            }
        });

        return mProgressivePromise;
    }

    public ProgressivePromise<Void, PlayException, Map.Entry<String,
            AsyncTaskParallel.DoneOrFail<Object, PlayException>>> getProgressivePromise() {
        return mProgressivePromise;
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
            LOGGER.i("type:" + mType + "\t segment:" + segment.toString());
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
}
