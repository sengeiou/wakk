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

public class InfiniteLoopSegmentGroupPlayer<O> implements Player {

    private static final Logger LOGGER = FwLoggerFactory.
            getLogger("InfiniteLoopSegmentGroupPlayer");

    private AsyncTaskSeries<PlayException> mTaskSeries;
    private PlayerFactory mPlayerFactory;
    private LinkedList<Segment<O>> mSegmentList = new LinkedList<>();

    InfiniteLoopSegmentGroupPlayer(
            LinkedList<Segment<O>> segmentList, PlayerFactory playerFactory) {
        mPlayerFactory = playerFactory;
        mSegmentList.addAll(segmentList);
    }

    @Override
    public Promise<Void, PlayException> play() {
        synchronized (this) {
            if (mTaskSeries != null) {
                throw new IllegalStateException("AbstractSegment playing error: taskSeries exits.");
            }

            mTaskSeries = new AsyncTaskSeries<>();
            appendTask(mTaskSeries);
            mTaskSeries.start();

            return mTaskSeries.promise().fail(new FailCallback<PlayException>() {
                @Override
                public void onFail(PlayException e) {
                    LOGGER.e(e);
                }
            });
        }
    }

    private void appendTask(final AsyncTaskSeries<PlayException> taskSeries) {
        Iterator<Segment<O>> iterator = mSegmentList.iterator();
        while (iterator.hasNext()) {
            final Segment<O> segment = iterator.next();
            taskSeries.append(new AsyncTask<Void, PlayException>() {
                @Override
                protected void onStart() {
                    mPlayerFactory.createPlayer(segment).play().done(new DoneCallback<Void>() {
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
                public boolean cancel() {
                    resolve(null);
                    return super.cancel();
                }
            });
        }

        taskSeries.append(new AsyncTask<Void, PlayException>() {

            Promise<Void, PlayException> mCirculationSeries;

            @Override
            protected void onStart() {
                AsyncTaskSeries<PlayException> circulationSeries = new AsyncTaskSeries<>();
                appendTask(circulationSeries);
                circulationSeries.start();

                mCirculationSeries = circulationSeries.promise().done(new DoneCallback<Void>() {
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
            public boolean cancel() {
                mCirculationSeries.cancel();
                resolve(null);
                return super.cancel();
            }
        });
    }
}
