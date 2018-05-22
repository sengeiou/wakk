package com.ubtrobot.master.adapter;

import android.os.Handler;

import com.ubtrobot.async.DoneCallback;
import com.ubtrobot.async.FailCallback;
import com.ubtrobot.async.ProgressCallback;
import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.transport.message.CallCancelListener;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Param;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;

public class CallProcessAdapter {

    private final Handler mHandler;

    public CallProcessAdapter(Handler handler) {
        mHandler = handler;
    }

    public <D, F extends Throwable, P> void onCall(
            final Responder responder,
            final ProgressiveCallable<D, F, P> callable,
            final DFPConverter<D, F, P> converter) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    ProgressivePromise<D, F, P> promise = callable.call();
                    promise.progress(new ProgressCallback<P>() {
                        @Override
                        public void onProgress(P progress) {
                            Param param = converter.convertProgress(progress);
                            if (param != null) {
                                responder.respondStickily(param);
                            }
                        }
                    });

                    onCall(responder, promise, converter);
                } catch (CallException e) {
                    responder.respondFailure(e.getCode(), e.getMessage());
                }
            }
        });
    }

    private <D, F extends Throwable> void onCall(
            final Responder responder,
            final Promise<D, F> promise,
            final DFConverter<D, F> converter) {
        promise.done(new DoneCallback<D>() {
            @Override
            public void onDone(D done) {
                responder.respondSuccess(converter.convertDone(done));
            }
        }).fail(new FailCallback<F>() {
            @Override
            public void onFail(F fail) {
                CallException ce = converter.convertFail(fail);
                responder.respondFailure(ce.getCode(), ce.getMessage());
            }
        });

        responder.setCallCancelListener(new CallCancelListener() {
            @Override
            public void onCancel(Request request) {
                promise.cancel();
            }
        });
    }

    public <D, F extends Throwable> void onCall(
            final Responder responder,
            final Callable<D, F> callable,
            final DFConverter<D, F> converter) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Promise<D, F> promise = callable.call();
                    onCall(responder, promise, converter);
                } catch (CallException e) {
                    responder.respondFailure(e.getCode(), e.getMessage());
                }
            }
        });
    }

    public <F extends Throwable> void onCall(
            final Responder responder,
            final Callable<Void, F> callable,
            final FConverter<F> converter) {
        onCall(responder, callable, (DFConverter<Void, F>) converter);
    }

    public interface Callable<D, F extends Throwable> {

        Promise<D, F> call() throws CallException;
    }

    public interface ProgressiveCallable<D, F extends Throwable, P> {

        ProgressivePromise<D, F, P> call() throws CallException;
    }

    public static abstract class FConverter<F extends Throwable> implements DFConverter<Void, F> {

        @Override
        public Param convertDone(Void done) {
            return null;
        }
    }

    public interface DFConverter<D, F> {

        Param convertDone(D done);

        CallException convertFail(F fail);
    }

    public interface DFPConverter<D, F, P> extends DFConverter<D, F> {

        Param convertProgress(P progress);
    }
}