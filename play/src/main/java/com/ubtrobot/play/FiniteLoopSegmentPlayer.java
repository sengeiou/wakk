package com.ubtrobot.play;

import android.os.Handler;
import android.os.Looper;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.async.AsyncTaskSeries;
import com.ubtrobot.async.DoneCallback;
import com.ubtrobot.async.Promise;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

public class FiniteLoopSegmentPlayer<O> extends SegmentPlayer<O> {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("InfiniteLoopSegmentPlayer");

    private final SegmentPlayer<O> mRealSegmentPlayer;
    private final Segment<O> mSegment;

    private final Handler mHandler;

    private AsyncTaskSeries<PlayException> mTaskSeries;

    FiniteLoopSegmentPlayer(AbstractSegmentPlayer<O> realSegmentPlayer, Segment<O> segment) {
        if (segment.getLoops() <= 0) {
            throw new IllegalStateException("Segment.loops <= 0.");
        }

        mSegment = segment;
        mRealSegmentPlayer = realSegmentPlayer;

        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public Promise<Void, PlayException> play() {
        synchronized (this) {
            if (mTaskSeries != null) {
                throw new IllegalStateException("Segment playing error: taskSeries exits.");
            }

            mTaskSeries = new AsyncTaskSeries<>();
            for (int i = 0; i < mSegment.getLoops(); i++) {
                mTaskSeries.append(new PlayTask(mSegment));
            }

            mTaskSeries.start();

            return mTaskSeries.promise().done(new DoneCallback<Void>() {
                @Override
                public void onDone(Void aVoid) {
                    mTaskSeries = null;
                }
            });
        }
    }

    @Override
    protected void onLoopStart(O option) {
        mRealSegmentPlayer.onLoopStart(option);
    }

    @Override
    protected void onLoopStop() {
        mRealSegmentPlayer.onLoopStop();
    }

    @Override
    protected void onEnd() {
        mRealSegmentPlayer.onEnd();
    }

    @Override
    protected void notifyLoopStopped() {
        mTaskSeries.resolveRunningTask(null);
    }

    @Override
    protected void notifyLoopAborted(final PlayException e) {
        mTaskSeries.rejectRunningTask(e);
    }

    private class PlayTask extends AsyncTask<Void, PlayException> implements Runnable {

        private Segment<O> mSegment;
        private boolean mTaskEnd;

        private PlayTask(Segment<O> segment) {
            mSegment = segment;
        }

        @Override
        protected void onStart() {
            mTaskEnd = false;

            if (mSegment.getDuration() > 0) {
                mHandler.postDelayed(this, mSegment.getDuration());
            }

            if (!mSegment.isBlank()) {
                onLoopStart(mSegment.getOption());
            }
        }

        @Override
        protected void onCancel() {
            if (!mSegment.isBlank()) {
                onLoopStop();
                onEnd();
            }

            mHandler.removeCallbacks(this);
        }

        @Override
        public void run() {
            mTaskEnd = true;

            if (!mSegment.isBlank()) {
                onLoopStop();
            }

            resolve(null);
        }

        @Override
        public boolean resolve(Void resolve) {
            if (mSegment.getDuration() > 0 && !mTaskEnd) {
                return false;
            }

            return super.resolve(resolve);
        }

        @Override
        public boolean reject(PlayException reject) {
            mHandler.removeCallbacks(this);
            return super.reject(reject);
        }
    }

}
