package com.ubtrobot.play;

import android.os.Handler;
import android.os.Looper;

import com.ubtrobot.async.AbstractCancelable;
import com.ubtrobot.async.DefaultPromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

public class InfiniteLoopSegmentPlayer<O> extends SegmentPlayer<O> {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("InfiniteLoopSegmentPlayer");

    private boolean mStarted;
    private SegmentPlayer<O> mRealSegmentPlayer;

    private long mDuration;
    private InfiniteRunnable mRunnable;
    private Handler mHandler;
    private DefaultPromise<Void, PlayException> mPromise;

    InfiniteLoopSegmentPlayer(AbstractSegmentPlayer<O> realSegmentPlayer, Segment<O> segment) {
        if (segment.getLoops() != 0) {
            throw new IllegalStateException("Segment.loops != 0.");
        }

        mRealSegmentPlayer = realSegmentPlayer;
        mDuration = segment.getDuration();

        mHandler = new Handler(Looper.getMainLooper());
        mRunnable = new InfiniteRunnable(segment.getOption());
        mPromise = new DefaultPromise<>(mHandler, new AbstractCancelable() {
            @Override
            protected void doCancel() {
                mHandler.removeCallbacks(mRunnable);

                onLoopStop();
                onEnd();
            }
        });
    }

    @Override
    public Promise<Void, PlayException> play() {
        synchronized (this) {
            mHandler.post(mRunnable);
        }
        return mPromise;
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
        mHandler.removeCallbacks(mRunnable);
        mPromise.resolve(null);
    }

    @Override
    protected void notifyLoopAborted(PlayException e) {
        mHandler.removeCallbacks(mRunnable);
        mPromise.reject(e);
    }

    private class InfiniteRunnable implements Runnable {

        private O option;

        private InfiniteRunnable(O option) {
            this.option = option;
        }

        @Override
        public void run() {
            if (mStarted) {
                mRealSegmentPlayer.onLoopStop();
            } else {
                mStarted = true;
            }

            mRealSegmentPlayer.onLoopStart(option);
            mHandler.postDelayed(this, mDuration);
        }
    }
}
