package com.ubtrobot.master.competition;

import android.os.Handler;

import com.google.protobuf.Message;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.master.service.MasterService;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Param;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;

import java.util.Collection;
import java.util.Collections;

public class ProtoCompetingCallDelegate {

    private final CompetingCallDelegate mCallDelegate;

    public ProtoCompetingCallDelegate(MasterService masterService, Handler mainLoopHandler) {
        mCallDelegate = new CompetingCallDelegate(masterService, mainLoopHandler);
    }

    public <D, F extends Throwable, P> void onCall(
            final Request request,
            Collection<String> competingItemIds,
            final Responder responder,
            final CompetingCallDelegate.SessionProgressiveCallable<D, F, P> callable,
            final DFPConverter<D, F, P> converter) {
        mCallDelegate.onCall(
                request,
                competingItemIds,
                responder,
                callable,
                new DFPConverterAdapter<>(converter)
        );
    }

    public <D, F extends Throwable> void onCall(
            final Request request,
            Collection<String> competingItemIds,
            final Responder responder,
            final CompetingCallDelegate.SessionCallable<D, F> callable,
            final DFConverter<D, F> converter) {
        mCallDelegate.onCall(
                request,
                competingItemIds,
                responder,
                callable,
                new DFConverterAdapter<>(converter)
        );
    }

    public <F extends Throwable> void onCall(
            final Request request,
            Collection<String> competingItemIds,
            final Responder responder,
            final CompetingCallDelegate.SessionCallable<Void, F> callable,
            final FConverter<F> converter) {
        onCall(request, competingItemIds, responder, callable, (DFConverter<Void, F>) converter);
    }

    public <D, F extends Throwable, P> void onCall(
            final Request request,
            String competingItemId,
            final Responder responder,
            final CompetingCallDelegate.SessionProgressiveCallable<D, F, P> callable,
            final DFPConverter<D, F, P> converter) {
        onCall(request, Collections.singleton(competingItemId), responder, callable, converter);
    }

    public <D, F extends Throwable> void onCall(
            final Request request,
            String competingItemId,
            final Responder responder,
            final CompetingCallDelegate.SessionCallable<D, F> callable,
            final DFConverter<D, F> converter) {
        onCall(request, Collections.singleton(competingItemId), responder, callable, converter);
    }

    public <F extends Throwable> void onCall(
            final Request request,
            String competingItemId,
            final Responder responder,
            final CompetingCallDelegate.SessionCallable<Void, F> callable,
            final FConverter<F> converter) {
        onCall(request, competingItemId, responder, callable, (DFConverter<Void, F>) converter);
    }

    public void onCompetitionSessionInactive(final CompetitionSessionInfo sessionInfo) {
        mCallDelegate.onCompetitionSessionInactive(sessionInfo);
    }

    public interface DFPConverter<D, F, P> extends DFConverter<D, F> {

        Message convertProgress(P progress);
    }

    public interface DFConverter<D, F> {

        Message convertDone(D done);

        CallException convertFail(F fail);
    }

    public static abstract class FConverter<F> implements DFConverter<Void, F> {

        @Override
        public Message convertDone(Void done) {
            return null;
        }
    }

    private static class DFConverterAdapter<D, F> implements CompetingCallDelegate.DFConverter<D, F> {

        DFConverter<D, F> mConverter;

        public DFConverterAdapter(DFConverter<D, F> converter) {
            mConverter = converter;
        }

        @Override
        public Param convertDone(D done) {
            Message message = mConverter.convertDone(done);
            if (message == null) {
                return null;
            }

            return ProtoParam.create(message);
        }

        @Override
        public CallException convertFail(F fail) {
            return mConverter.convertFail(fail);
        }
    }

    private static class DFPConverterAdapter<D, F, P> extends DFConverterAdapter<D, F>
            implements CompetingCallDelegate.DFPConverter<D, F, P> {

        public DFPConverterAdapter(DFPConverter<D, F, P> converter) {
            super(converter);
        }

        @Override
        public Param convertProgress(P progress) {
            Message message = ((DFPConverter<D, F, P>) mConverter).convertProgress(progress);
            if (message == null) {
                return null;
            }

            return ProtoParam.create(message);
        }
    }
}
