package com.ubtrobot.master.adapter;

import android.os.Handler;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.async.ProgressiveAsyncTask;
import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.master.call.ConvenientCallable;
import com.ubtrobot.master.call.ConvenientStickyCallable;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Cancelable;
import com.ubtrobot.transport.message.Param;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Response;
import com.ubtrobot.transport.message.ResponseCallback;
import com.ubtrobot.transport.message.StickyResponseCallback;

import java.util.concurrent.Semaphore;

public class CallAdapter {

    private final ConvenientCallable mCallable;
    private final Handler mHandler;

    public CallAdapter(ConvenientCallable callable, Handler handler) {
        mCallable = callable;
        mHandler = handler;
    }

    ConvenientCallable callable() {
        return mCallable;
    }

    ConvenientStickyCallable stickyCallable() {
        return (ConvenientStickyCallable) mCallable;
    }

    public <D, F extends Exception> Promise<D, F>
    call(String path, DFConverter<D, F> converter) {
        return call(path, null, converter);
    }

    public <D, F extends Exception> Promise<D, F>
    call(final String path, final Param param, final DFConverter<D, F> converter) {
        AsyncTask<D, F> task = new AsyncTask<D, F>() {

            Cancelable mCancelable;

            @Override
            protected void onStart() {
                mCancelable = mCallable.call(path, param, new ResponseCallback() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onResponse(Request request, Response response) {
                        try {
                            D done = converter.convertDone(response.getParam());
                            resolve(done);
                        } catch (Exception e) {
                            try {
                                F fail = (F) e;
                                reject(fail);
                            } catch (ClassCastException cce) {
                                throw new IllegalStateException(e);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Request request, CallException e) {
                        F fail = converter.convertFail(e);
                        reject(fail);
                    }
                });
            }

            @Override
            protected void onCancel() {
                mCancelable.cancel();
            }
        };

        task.start();
        return task.promise();
    }

    @SuppressWarnings("unchecked")
    public <F extends Exception> Promise<Void, F> call(String path, FConverter<F> converter) {
        return call(path, null, (DFConverter<Void, F>) converter);
    }

    @SuppressWarnings("unchecked")
    public <D, F extends Exception> Promise<D, F>
    call(String path, Param param, FConverter<F> converter) {
        return call(path, param, (DFConverter<D, F>) converter);
    }

    public <D, F extends Exception, P> ProgressivePromise<D, F, P>
    callStickily(String path, final DFPConverter<D, F, P> converter) {
        return callStickily(path, null, converter);
    }

    public <D, F extends Exception, P> ProgressivePromise<D, F, P>
    callStickily(final String path, final Param param, final DFPConverter<D, F, P> converter) {
        if (!(mCallable instanceof ConvenientStickyCallable)) {
            throw new IllegalStateException("The callable is NOT a ConvenientStickyCallable instance.");
        }

        final ConvenientStickyCallable stickyCallable = (ConvenientStickyCallable) mCallable;
        ProgressiveAsyncTask<D, F, P> task = new ProgressiveAsyncTask<D, F, P>() {

            Cancelable mCancelable;

            @Override
            protected void onStart() {
                mCancelable = stickyCallable.callStickily(
                        path,
                        param,
                        new StickyResponseCallback() {
                            @SuppressWarnings("unchecked")
                            @Override
                            public void onResponseStickily(Request request, Response response) {
                                try {
                                    P progress = converter.convertProgress(response.getParam());
                                    report(progress);
                                } catch (Exception e) {
                                    mCancelable.cancel();

                                    try {
                                        F fail = (F) e;
                                        reject(fail);
                                    } catch (ClassCastException cce) {
                                        throw new IllegalStateException(e);
                                    }
                                }
                            }

                            @SuppressWarnings("unchecked")
                            @Override
                            public void onResponseCompletely(Request request, Response response) {
                                try {
                                    D done = converter.convertDone(response.getParam());
                                    resolve(done);
                                } catch (Exception e) {
                                    try {
                                        F fail = (F) e;
                                        reject(fail);
                                    } catch (ClassCastException cce) {
                                        throw new IllegalStateException(e);
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Request request, CallException e) {
                                F fail = converter.convertFail(e);
                                reject(fail);
                            }
                        }
                );
            }

            @Override
            protected void onCancel() {
                mCancelable.cancel();
            }
        };

        task.start();
        return task.promise();
    }

    private void acquirePermit(Semaphore semaphore) {
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    semaphore.acquire();
                    break;
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted)
                Thread.currentThread().interrupt();
        }
    }

    public abstract static class FConverter<F extends Exception> implements DFConverter<Void, F> {

        @Override
        public Void convertDone(Param param) throws F {
            return null;
        }
    }

    public interface DFConverter<D, F extends Exception> {

        D convertDone(Param param) throws F;

        F convertFail(CallException e);
    }

    public interface DFPConverter<D, F extends Exception, P> extends DFConverter<D, F> {

        P convertProgress(Param param) throws F;
    }
}