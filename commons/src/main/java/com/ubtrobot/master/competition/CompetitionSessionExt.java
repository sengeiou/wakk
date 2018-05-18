package com.ubtrobot.master.competition;

import com.ubtrobot.async.AbstractCancelable;
import com.ubtrobot.async.Cancelable;
import com.ubtrobot.async.DefaultProgressivePromise;
import com.ubtrobot.async.DefaultPromise;
import com.ubtrobot.async.DoneCallback;
import com.ubtrobot.async.FailCallback;
import com.ubtrobot.async.ProgressCallback;
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
        synchronized (mSession) {
            if (mSession.isActive()) {
                return callable.call(mSession, competing);
            }

            final Promise[] callablePromise = new Promise[1];
            Cancelable cancelable = new AbstractCancelable() {
                @Override
                protected void doCancel() {
                    synchronized (mSession) {
                        if (callablePromise[0] != null) {
                            callablePromise[0].cancel();
                        }
                    }
                }
            };

            final DefaultProgressivePromise<D, F, P> promise =
                    new DefaultProgressivePromise<>(cancelable);
            mSession.activate(new ActivateCallback() {
                @Override
                public void onSuccess(String s) {
                    synchronized (mSession) {
                        if (promise.isCanceled()) {
                            return;
                        }

                        callablePromise[0] = callable.call(
                                mSession,
                                competing
                        ).progress(new ProgressCallback<P>() {
                            @Override
                            public void onProgress(P progress) {
                                promise.report(progress);
                            }
                        }).done(new DoneCallback<D>() {
                            @Override
                            public void onDone(D done) {
                                promise.resolve(done);
                            }
                        }).fail(new FailCallback<F>() {
                            @Override
                            public void onFail(F fail) {
                                promise.reject(fail);
                            }
                        });
                    }
                }

                @Override
                public void onFailure(ActivateException e) {
                    synchronized (mSession) {
                        promise.reject(converter.convert(e));
                    }
                }
            });

            return promise;
        }
    }

    public <D, F> Promise<D, F> execute(
            final C competing,
            final SessionCallable<D, F, C> callable,
            final Converter<F> converter) {
        synchronized (mSession) {
            if (mSession.isActive()) {
                return callable.call(mSession, competing);
            }

            final Promise[] callablePromise = new ProgressivePromise[1];
            Cancelable cancelable = new AbstractCancelable() {
                @Override
                protected void doCancel() {
                    synchronized (mSession) {
                        if (callablePromise[0] != null) {
                            callablePromise[0].cancel();
                        }
                    }
                }
            };

            final DefaultPromise<D, F> promise = new DefaultPromise<>(cancelable);
            mSession.activate(new ActivateCallback() {
                @Override
                public void onSuccess(String s) {
                    synchronized (mSession) {
                        if (promise.isCanceled()) {
                            return;
                        }

                        callablePromise[0] = callable.call(
                                mSession,
                                competing
                        ).done(new DoneCallback<D>() {
                            @Override
                            public void onDone(D done) {
                                promise.resolve(done);
                            }
                        }).fail(new FailCallback<F>() {
                            @Override
                            public void onFail(F fail) {
                                promise.reject(fail);
                            }
                        });
                    }
                }

                @Override
                public void onFailure(ActivateException e) {
                    synchronized (mSession) {
                        promise.reject(converter.convert(e));
                    }
                }
            });

            return promise;
        }
    }

    public interface SessionCallable<D, F, C extends Competing> {

        Promise<D, F> call(CompetitionSession session, C competing);
    }

    public interface SessionProgressiveCallable<D, F, P, C extends Competing>
            extends SessionCallable<D, F, C> {

        ProgressivePromise<D, F, P> call(CompetitionSession session, C competing);
    }

    public interface Converter<F> {

        F convert(ActivateException e);
    }
}
