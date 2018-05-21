package com.ubtrobot.play;

public abstract class SegmentPlayer<O> implements Player {

    protected abstract void onLoopStart(O option);

    protected abstract void onLoopStop();

    protected abstract void onEnd();

    protected abstract void notifyLoopStopped();

    protected abstract void notifyLoopAborted(PlayException e);
}