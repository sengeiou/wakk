package com.ubtrobot.master.competition;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.async.DoneCallback;
import com.ubtrobot.async.FailCallback;
import com.ubtrobot.async.ProgressCallback;
import com.ubtrobot.async.ProgressiveAsyncTask;
import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;

public class CompetitionSessionExt<C extends Competing> {

    private final CompetitionSession mSession;

    public CompetitionSessionExt(CompetitionSession session) {
        mSession = session;
    }

    public CompetitionSession getSession() {
        return mSession;
    }

    public <D, F, P> ProgressivePromise<D, F, P> execute(
            final C competing,
            final SessionProgressiveCallable<D, F, P, C> callable,
            final Converter<F> converter) {
        ProgressiveAsyncTask<D, F, P> task = new ProgressiveAsyncTask<D, F, P>() {

            private ProgressivePromise<D, F, P> mPromise;
            private final byte[] mLock = new byte[0];

            @Override
            protected void onStart() {
                mSession.activate(new ActivateCallback() {
                    @Override
                    public void onSuccess(String sessionId) {
                        synchronized (mLock) {
                            if (isCanceled()) {
                                return;
                            }

                            mPromise = callable.call(
                                    mSession,
                                    competing
                            ).progress(new ProgressCallback<P>() {
                                @Override
                                public void onProgress(P progress) {
                                    report(progress);
                                }
                            }).done(new DoneCallback<D>() {
                                @Override
                                public void onDone(D done) {
                                    resolve(done);
                                }
                            }).fail(new FailCallback<F>() {
                                @Override
                                public void onFail(F e) {
                                    reject(e);
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(ActivateException e) {
                        reject(converter.convert(e));
                    }
                });
            }

            @Override
            protected void onCancel() {
                synchronized (mLock) {
                    if (mPromise != null) {
                        mPromise.cancel();
                    }
                }
            }
        };

        task.start();
        return task.promise();
    }

    public <D, F> Promise<D, F> execute(
            final C competing,
            final SessionCallable<D, F, C> callable,
            final Converter<F> converter) {
        AsyncTask<D, F> task = new AsyncTask<D, F>() {

            private Promise<D, F> mPromise;
            private final byte[] mLock = new byte[0];

            @Override
            protected void onStart() {
                mSession.activate(new ActivateCallback() {
                    @Override
                    public void onSuccess(String sessionId) {
                        synchronized (mLock) {
                            if (isCanceled()) {
                                return;
                            }

                            mPromise = callable.call(
                                    mSession,
                                    competing
                            ).done(new DoneCallback<D>() {
                                @Override
                                public void onDone(D done) {
                                    resolve(done);
                                }
                            }).fail(new FailCallback<F>() {
                                @Override
                                public void onFail(F e) {
                                    reject(e);
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(ActivateException e) {
                        reject(converter.convert(e));
                    }
                });
            }

            @Override
            protected void onCancel() {
                synchronized (mLock) {
                    if (mPromise != null) {
                        mPromise.cancel();
                    }
                }
            }
        };

        task.start();
        return task.promise();
    }

    public interface SessionProgressiveCallable<D, F, P, C extends Competing> {

        ProgressivePromise<D, F, P> call(CompetitionSession session, C competing);
    }

    public interface SessionCallable<D, F, C extends Competing> {

        Promise<D, F> call(CompetitionSession session, C competing);
    }

    public interface Converter<F> {

        F convert(ActivateException e);
    }
}
