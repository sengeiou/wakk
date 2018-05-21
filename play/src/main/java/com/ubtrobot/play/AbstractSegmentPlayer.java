package com.ubtrobot.play;

import com.ubtrobot.async.Promise;

public abstract class AbstractSegmentPlayer<O> extends SegmentPlayer<O> {

    private SegmentPlayer<O> mPlayer;

    public AbstractSegmentPlayer(Segment<O> segment) {
        int loops = segment.getLoops();
        if (loops < 0) {
            throw new IllegalStateException("Segment.loops < 0.");
        }

        if (loops > 0) {
            mPlayer = new FiniteLoopSegmentPlayer<>(this, segment);
        } else {
            mPlayer = new InfiniteLoopSegmentPlayer<>(
                    this, segment, true);
        }
    }

    @Override
    public Promise<Void, PlayException> play() {
        return mPlayer.play();
    }

    @Override
    protected void notifyLoopStopped() {
        mPlayer.notifyLoopStopped();
    }

    @Override
    protected void notifyLoopAborted(final PlayException e) {
        mPlayer.notifyLoopAborted(e);
    }
}
