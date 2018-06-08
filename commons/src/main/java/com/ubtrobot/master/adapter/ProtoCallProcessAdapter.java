package com.ubtrobot.master.adapter;

import android.os.Handler;

import com.google.protobuf.Message;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Param;
import com.ubtrobot.transport.message.Responder;

public class ProtoCallProcessAdapter {

    private CallProcessAdapter mCallProcessAdapter;

    public ProtoCallProcessAdapter(Handler handler) {
        mCallProcessAdapter = new CallProcessAdapter(handler);
    }

    public <D, F extends Throwable, P> void onCall(
            final Responder responder,
            final CallProcessAdapter.ProgressiveCallable<D, F, P> callable,
            final DFPConverter<D, F, P> converter) {
        mCallProcessAdapter.onCall(
                responder,
                callable,
                new CallProcessAdapter.DFPConverter<D, F, P>() {
                    @Override
                    public Param convertDone(D done) {
                        Message message = converter.convertDone(done);
                        if (message == null) {
                            return null;
                        }

                        return ProtoParam.create(converter.convertDone(done));
                    }

                    @Override
                    public CallException convertFail(F e) {
                        return converter.convertFail(e);
                    }

                    @Override
                    public Param convertProgress(P progress) {
                        Message message = converter.convertProgress(progress);
                        if (message == null) {
                            return null;
                        }

                        return ProtoParam.create(converter.convertProgress(progress));
                    }
                }
        );
    }

    public <F extends Throwable> void onCall(
            final Responder responder,
            final CallProcessAdapter.Callable<Void, F> callable,
            final FConverter<F> converter) {
        onCall(responder, callable, (DFConverter<Void, F>) converter);
    }

    public <D, F extends Throwable> void onCall(
            final Responder responder,
            final CallProcessAdapter.Callable<D, F> callable,
            final DFConverter<D, F> converter) {
        mCallProcessAdapter.onCall(
                responder,
                callable,
                new CallProcessAdapter.DFConverter<D, F>() {
                    @Override
                    public Param convertDone(D done) {
                        Message message = converter.convertDone(done);
                        if (message == null) {
                            return null;
                        }

                        return ProtoParam.create(converter.convertDone(done));
                    }

                    @Override
                    public CallException convertFail(F e) {
                        return converter.convertFail(e);
                    }
                }
        );
    }

    public static abstract class FConverter<F extends Throwable> implements DFConverter<Void, F> {

        @Override
        public Message convertDone(Void done) {
            return null;
        }
    }

    public interface DFConverter<D, F> {

        Message convertDone(D done);

        CallException convertFail(F fail);
    }

    public interface DFPConverter<D, F, P> extends DFConverter<D, F> {

        Message convertProgress(P progress);
    }
}
