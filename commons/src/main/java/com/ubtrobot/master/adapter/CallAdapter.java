package com.ubtrobot.master.adapter;

import android.os.Handler;

import com.ubtrobot.async.CancelHandler;
import com.ubtrobot.async.Deferred;
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

    public <D, F extends Exception> Promise<D, F, Void>
    call(String path, DFConverter<D, F> converter) {
        return call(path, null, converter);
    }

    public <D, F extends Exception> Promise<D, F, Void>
    call(String path, Param param, final DFConverter<D, F> converter) {
        final Deferred<D, F, Void> deferred = new Deferred<>(mHandler);
        final Cancelable cancelable = mCallable.call(path, param, new ResponseCallback() {
            @SuppressWarnings("unchecked")
            @Override
            public void onResponse(Request request, Response response) {
                try {
                    D done = converter.convertDone(response.getParam());
                    deferred.resolve(done);
                } catch (Exception e) {
                    try {
                        F fail = (F) e;
                        deferred.reject(fail);
                    } catch (ClassCastException cce) {
                        throw new IllegalStateException(e);
                    }
                }
            }

            @Override
            public void onFailure(Request request, CallException e) {
                F fail = converter.convertFail(e);
                deferred.reject(fail);
            }
        });

        deferred.setCancelHandler(new CancelHandler() {
            @Override
            public void onCancel() {
                cancelable.cancel();
            }
        });
        return deferred.promise();
    }

    @SuppressWarnings("unchecked")
    public <F extends Exception> Promise<Void, F, Void> call(String path, FConverter<F> converter) {
        return call(path, null, (DFConverter<Void, F>) converter);
    }

    @SuppressWarnings("unchecked")
    public <D, F extends Exception> Promise<D, F, Void>
    call(String path, Param param, FConverter<F> converter) {
        return call(path, param, (DFConverter<D, F>) converter);
    }

    public <D, F extends Exception, P> Promise<D, F, P>
    callStickily(String path, final DFPConverter<D, F, P> converter) {
        return callStickily(path, null, converter);
    }

    public <D, F extends Exception, P> Promise<D, F, P>
    callStickily(String path, Param param, final DFPConverter<D, F, P> converter) {
        if (!(mCallable instanceof ConvenientStickyCallable)) {
            throw new IllegalStateException("The callable is NOT a ConvenientStickyCallable instance.");
        }

        ConvenientStickyCallable stickyCallable = (ConvenientStickyCallable) mCallable;
        final Deferred<D, F, P> deferred = new Deferred<>(mHandler);

        final Cancelable[] cancelable = new Cancelable[1];
        final Semaphore semaphore = new Semaphore(1);
        acquirePermit(semaphore);

        cancelable[0] = stickyCallable.callStickily(
                path,
                param,
                new StickyResponseCallback() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onResponseStickily(Request request, Response response) {
                        try {
                            P progress = converter.convertProgress(response.getParam());
                            deferred.notify(progress);
                        } catch (Exception e) {
                            acquirePermit(semaphore);
                            cancelable[0].cancel();

                            try {
                                F fail = (F) e;
                                deferred.reject(fail);
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
                            deferred.resolve(done);
                        } catch (Exception e) {
                            try {
                                F fail = (F) e;
                                deferred.reject(fail);
                            } catch (ClassCastException cce) {
                                throw new IllegalStateException(e);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Request request, CallException e) {
                        F fail = converter.convertFail(e);
                        deferred.reject(fail);
                    }
                }
        );
        semaphore.release();

        deferred.setCancelHandler(new CancelHandler() {
            @Override
            public void onCancel() {
                cancelable[0].cancel();
            }
        });

        return deferred.promise();
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