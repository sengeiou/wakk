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

    public <D, F, P> void onCall(
            final Responder responder,
            final CallProcessAdapter.Callable<D, F, P> callable,
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

    public static abstract class FConverter<F> extends DFConverter<Void, F> {

        @Override
        public Message convertDone(Void done) {
            return null;
        }
    }

    public static abstract class DFConverter<D, F> implements DFPConverter<D, F, Void> {

        @Override
        public Message convertProgress(Void progress) {
            return null;
        }
    }

    public interface DFPConverter<D, F, P> {

        Message convertDone(D done);

        CallException convertFail(F fail);

        Message convertProgress(P progress);
    }
}
