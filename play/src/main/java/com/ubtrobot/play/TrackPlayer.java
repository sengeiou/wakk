package com.ubtrobot.play;

import com.ubtrobot.async.Promise;

public class TrackPlayer<O> implements Player {

    private Track<O> mTrack;
    private PlayerFactory mPlayerFactory;

    public TrackPlayer(Track<O> track, PlayerFactory playerFactory) {
        mTrack = track;
        mPlayerFactory = playerFactory;
    }

    @Override
    public Promise<Void, PlayException> play() {
        synchronized (this) {
            return new SegmentGroupPlayer<>(mTrack.getSegmentGroup(), mPlayerFactory).play();
        }
    }
}
