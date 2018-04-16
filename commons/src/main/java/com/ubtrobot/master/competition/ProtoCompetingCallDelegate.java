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

    public <D, F, P> void onCall(
            final Request request,
            Collection<String> competingItemIds,
            final Responder responder,
            final CompetingCallDelegate.SessionCallable<D, F, P> callable,
            final DFPConverter<D, F, P> converter) {
        mCallDelegate.onCall(
                request,
                competingItemIds,
                responder,
                callable,
                new CompetingCallDelegate.DFPConverter<D, F, P>() {
                    @Override
                    public Param convertDone(D done) {
                        Message message = converter.convertDone(done);
                        if (message == null) {
                            return null;
                        }

                        return ProtoParam.create(message);
                    }

                    @Override
                    public CallException convertFail(F fail) {
                        return converter.convertFail(fail);
                    }

                    @Override
                    public Param convertProgress(P progress) {
                        Message message = converter.convertProgress(progress);
                        if (message == null) {
                            return null;
                        }

                        return ProtoParam.create(message);
                    }
                }
        );
    }

    public <D, F> void onCall(
            final Request request,
            Collection<String> competingItemIds,
            final Responder responder,
            final CompetingCallDelegate.SessionCallable<D, F, Void> callable,
            final DFConverter<D, F> converter) {
        onCall(request, competingItemIds, responder, callable, (DFPConverter<D, F, Void>) converter);
    }

    public <F> void onCall(
            final Request request,
            Collection<String> competingItemIds,
            final Responder responder,
            final CompetingCallDelegate.SessionCallable<Void, F, Void> callable,
            final FConverter<F> converter) {
        onCall(request, competingItemIds, responder, callable, (DFPConverter<Void, F, Void>) converter);
    }

    public <D, F, P> void onCall(
            final Request request,
            String competingItemId,
            final Responder responder,
            final CompetingCallDelegate.SessionCallable<D, F, P> callable,
            final DFPConverter<D, F, P> converter) {
        onCall(request, Collections.singleton(competingItemId), responder, callable, converter);
    }

    public <D, F> void onCall(
            final Request request,
            String competingItemId,
            final Responder responder,
            final CompetingCallDelegate.SessionCallable<D, F, Void> callable,
            final DFConverter<D, F> converter) {
        onCall(request, competingItemId, responder, callable, (DFPConverter<D, F, Void>) converter);
    }

    public <F> void onCall(
            final Request request,
            String competingItemId,
            final Responder responder,
            final CompetingCallDelegate.SessionCallable<Void, F, Void> callable,
            final FConverter<F> converter) {
        onCall(request, competingItemId, responder, callable, (DFPConverter<Void, F, Void>) converter);
    }

    public void onCompetitionSessionInactive(final CompetitionSessionInfo sessionInfo) {
        mCallDelegate.onCompetitionSessionInactive(sessionInfo);
    }

    public interface DFPConverter<D, F, P> {

        Message convertDone(D done);

        CallException convertFail(F fail);

        Message convertProgress(P progress);
    }

    public static abstract class DFConverter<D, F> implements DFPConverter<D, F, Void> {

        @Override
        public Message convertProgress(Void progress) {
            return null;
        }
    }

    public static abstract class FConverter<F> extends DFConverter<Void, F> {

        @Override
        public Message convertDone(Void done) {
            return null;
        }
    }
}
