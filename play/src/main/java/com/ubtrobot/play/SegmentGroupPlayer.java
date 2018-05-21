package com.ubtrobot.play;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.async.AsyncTaskSeries;
import com.ubtrobot.async.DoneCallback;
import com.ubtrobot.async.FailCallback;
import com.ubtrobot.async.Promise;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.util.Iterator;
import java.util.LinkedList;

public class SegmentGroupPlayer<O> implements Player {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("SegmentGroupPlayer");

    private static final int INFINITE_LOOP = 0;

    private PlayerFactory mPlayerFactory;
    private LinkedList<Segment<O>> mSegmentList = new LinkedList<>();
    private AsyncTaskSeries<PlayException> mTaskSeries;

    private int mInfiniteGroupIndex = -1;
    private Segment<O> mLastSegment;

    private Promise<Void, PlayException> mInfiniteSegmentPromise;
    private Promise<Void, PlayException> mInfiniteSegmentGroupPromise;

    public SegmentGroupPlayer(SegmentGroup<O> segmentGroup, PlayerFactory playerFactory) {
        mPlayerFactory = playerFactory;
        getRealSegments(mSegmentList, segmentGroup);
    }

    private void getRealSegments(
            LinkedList<Segment<O>> segments, SegmentGroup<O> segmentGroup) {
        int groupLoop = segmentGroup.getLoops();
        if (groupLoop < 0) {
            throw new IllegalStateException("SegmentGroup.loops < 0.");
        }

        if (groupLoop == INFINITE_LOOP) {
            mInfiniteGroupIndex = segments.size();
            mLastSegment = segmentGroup;
            return;
        }

        for (int i = 0; i < groupLoop; i++) {
            int childSize = segmentGroup.getChildren().size();
            if (childSize == 0) {
                Segment<O> realSegment = new Segment.Builder<O>().
                        setSegment(segmentGroup).setLoops(1).build();
                segments.add(realSegment);
                continue;
            }

            for (Segment<O> child : segmentGroup.getChildren()) {
                if (child instanceof SegmentGroup) {
                    getRealSegments(segments, (SegmentGroup<O>) child);
                }
            }
        }
    }

    @Override
    public Promise<Void, PlayException> play() {
        LOGGER.i("SegmentGroup play....");
        synchronized (this) {
            if (mTaskSeries != null) {
                throw new IllegalStateException("AbstractSegment playing error: taskSeries exits.");
            }

            mTaskSeries = new AsyncTaskSeries<>();

            Iterator<Segment<O>> iterator = mSegmentList.iterator();
            while (iterator.hasNext()) {
                appendTask(iterator.next());
            }
            addCirculationTask();

            mTaskSeries.start();
            return mTaskSeries.promise();
        }
    }

    private void appendTask(final Segment<O> segment) {
        mTaskSeries.append(new AsyncTask<Void, PlayException>() {
            @Override
            protected void onStart() {
                mPlayerFactory.createPlayer(segment).play().done(new DoneCallback<Void>() {
                    @Override
                    public void onDone(Void aVoid) {
                        LOGGER.i("Task success:" + segment.getOption().toString());
                        resolve(aVoid);
                    }
                }).fail(new FailCallback<PlayException>() {
                    @Override
                    public void onFail(PlayException e) {
                        LOGGER.e("Task fail:" + segment.getOption().toString());
                        reject(e);
                    }
                });
            }

            @Override
            public boolean cancel() {
                resolve(null);
                return super.cancel();
            }
        });
    }

    private void addCirculationTask() {
        AsyncTask<Void, PlayException> circulationTask = new AsyncTask<Void, PlayException>() {
            @Override
            protected void onStart() {
                if (causeByFiniteTask()) {
                    resolve(null);
                    return;
                }

                if (causedByInfiniteSegmentTask()) {
                    InfiniteLoopSegmentPlayer<O> infiniteLoopSegmentPlayer =
                            new InfiniteLoopSegmentPlayer<>(
                                    (AbstractSegmentPlayer<O>) mPlayerFactory.
                                            createPlayer(mLastSegment),
                                    mLastSegment, false);
                    mInfiniteSegmentPromise = infiniteLoopSegmentPlayer.play();
                    return;
                }

                if (causedByInfiniteGroupTask()) {
                    LinkedList<Segment<O>> infiniteTaskList = new LinkedList<>();
                    for (int i = mInfiniteGroupIndex, len = mSegmentList.size(); i < len; i++) {
                        infiniteTaskList.add(mSegmentList.get(i));
                    }

                    InfiniteLoopSegmentGroupPlayer infinitePlayer =
                            new InfiniteLoopSegmentGroupPlayer(infiniteTaskList, mPlayerFactory);
                    mInfiniteSegmentGroupPromise = infinitePlayer.play();
                }

            }

            @Override
            public boolean cancel() {
                if (mInfiniteSegmentPromise != null) {
                    mInfiniteSegmentPromise.cancel();
                }

                if (mInfiniteSegmentGroupPromise != null) {
                    mInfiniteSegmentGroupPromise.cancel();
                }
                return super.cancel();
            }
        };

        mTaskSeries.append(circulationTask);
    }

    private boolean causedByInfiniteSegmentTask() {
        return mLastSegment != null;
    }

    private boolean causeByFiniteTask() {
        return !causedByInfiniteSegmentTask() && mInfiniteGroupIndex < 0;
    }

    private boolean causedByInfiniteGroupTask() {
        return !causedByInfiniteSegmentTask() && !causeByFiniteTask();
    }
}