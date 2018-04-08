package com.ubtrobot.master.adapter;

import android.os.Handler;

import com.ubtrobot.async.DoneCallback;
import com.ubtrobot.async.FailCallback;
import com.ubtrobot.async.ProgressCallback;
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

    public <D, F, P> void onCall(
            final Responder responder,
            final Callable<D, F, P> callable,
            final DFPConverter<D, F, P> converter) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    final Promise<D, F, P> promise = callable.call(
                    ).progress(new ProgressCallback<P>() {
                        @Override
                        public void onProgress(P progress) {
                            Param param = converter.convertProgress(progress);
                            if (param != null) {
                                responder.respondStickily(param);
                            }
                        }
                    }).done(new DoneCallback<D>() {
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
                } catch (CallException e) {
                    responder.respondFailure(e.getCode(), e.getMessage());
                }
            }
        });
    }

    public <D, F> void onCall(
            final Responder responder,
            final Callable<D, F, Void> callable,
            final DFConverter<D, F> converter) {
        onCall(responder, callable, (DFPConverter<D, F, Void>) converter);
    }

    public <F> void onCall(
            final Responder responder,
            final Callable<Void, F, Void> callable,
            final FConverter<F> converter) {
        onCall(responder, callable, (DFPConverter<Void, F, Void>) converter);
    }

    public interface Callable<D, F, P> {

        Promise<D, F, P> call() throws CallException;
    }

    public static abstract class FConverter<F> extends DFConverter<Void, F> {

        @Override
        public Param convertDone(Void done) {
            return null;
        }
    }

    public static abstract class DFConverter<D, F> implements DFPConverter<D, F, Void> {

        @Override
        public Param convertProgress(Void progress) {
            return null;
        }
    }

    public interface DFPConverter<D, F, P> {

        Param convertDone(D done);

        CallException convertFail(F fail);

        Param convertProgress(P progress);
    }
}