package com.ubtrobot.master.competition;

import android.os.Handler;
import android.os.Looper;

import com.ubtrobot.async.CancelHandler;
import com.ubtrobot.async.Deferred;
import com.ubtrobot.async.DoneCallback;
import com.ubtrobot.async.FailCallback;
import com.ubtrobot.async.ProgressCallback;
import com.ubtrobot.async.Promise;

public class CompetitionSessionExt<C extends Competing> {

    private final CompetitionSession mSession;
    private final Handler mHandler;

    public CompetitionSessionExt(CompetitionSession session) {
        mSession = session;
        mHandler = new Handler(Looper.getMainLooper());
    }

    public CompetitionSession getSession() {
        return mSession;
    }

    public <D, F, P> Promise<D, F, P> execute(
            final C competing,
            final SessionCallable<D, F, P, C> callable,
            final Converter<F> converter) {
        synchronized (mSession) {
            if (mSession.isActive()) {
                return callable.call(mSession, competing);
            }

            final Deferred<D, F, P> deferred = new Deferred<>(mHandler);
            // share[0](Boolean) 是否已经取消
            // share[1](Promise) callable.call 返回的 promise
            final Object[] shared = new Object[]{false, null};

            deferred.setCancelHandler(new CancelHandler() {
                @Override
                public void onCancel() {
                    synchronized (shared) {
                        shared[0] = true;

                        if (shared[1] != null) {
                            ((Promise) shared[1]).cancel();
                        }
                    }
                }
            });

            mSession.activate(new ActivateCallback() {
                @Override
                public void onSuccess(String s) {
                    synchronized (mSession) {
                        synchronized (shared) {
                            boolean canceled = (boolean) shared[0];
                            if (canceled) {
                                return;
                            }

                            shared[1] = callable.call(
                                    mSession, competing
                            ).progress(new ProgressCallback<P>() {
                                @Override
                                public void onProgress(P progress) {
                                    deferred.notify(progress);
                                }
                            }).done(new DoneCallback<D>() {
                                @Override
                                public void onDone(D result) {
                                    deferred.resolve(result);
                                }
                            }).fail(new FailCallback<F>() {
                                @Override
                                public void onFail(F result) {
                                    deferred.reject(result);
                                }
                            });
                        }
                    }
                }

                @Override
                public void onFailure(ActivateException e) {
                    deferred.reject(converter.convert(e));
                }
            });

            return deferred.promise();
        }
    }

    public interface SessionCallable<D, F, P, C extends Competing> {

        Promise<D, F, P> call(CompetitionSession session, C competing);
    }

    public interface Converter<F> {

        F convert(ActivateException e);
    }
}
