package com.ubtrobot.master.competition;

import android.os.Handler;

import com.ubtrobot.async.DoneCallback;
import com.ubtrobot.async.FailCallback;
import com.ubtrobot.async.ProgressCallback;
import com.ubtrobot.async.Promise;
import com.ubtrobot.master.service.MasterService;
import com.ubtrobot.master.transport.message.CallGlobalCode;
import com.ubtrobot.transport.message.CallCancelListener;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Param;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CompetingCallDelegate {

    private final Handler mHandler;
    private final MasterService mMasterService;
    private final HashMap<String, CallEnv> mCallEnvs = new HashMap<>();

    public CompetingCallDelegate(MasterService masterService, Handler mainLoopHandler) {
        mHandler = mainLoopHandler;
        mMasterService = masterService;
    }

    public <D, F, P> void onCall(
            final Request request,
            Collection<String> competingItemIds,
            final Responder responder,
            final SessionCallable<D, F, P> callable,
            final DFPConverter<D, F, P> converter) {
        if (competingItemIds.isEmpty()) {
            throw new IllegalArgumentException("Argument competingItemIds is empty.");
        }

        final CompetitionSessionInfo sessionInfo = checkSessionInfo(responder, competingItemIds);
        if (sessionInfo == null) {
            return;
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final Promise<D, F, P> promise;
                try {
                    promise = callable.call();
                } catch (CallException e) {
                    responder.respondFailure(e);
                    return;
                }

                mCallEnvs.put(request.getId(), new CallEnv(promise, responder, sessionInfo));
                responder.setCallCancelListener(new CallCancelListener() {
                    @Override
                    public void onCancel(Request request) {
                        if (mCallEnvs.remove(request.getId()) != null) {
                            promise.cancel();
                        }
                    }
                });

                promise.progress(new ProgressCallback<P>() {
                    @Override
                    public void onProgress(P progress) {
                        if (mCallEnvs.containsKey(request.getId()) && progress != null) {
                            responder.respondStickily(converter.convertProgress(progress));
                        }
                    }
                }).done(new DoneCallback<D>() {
                    @Override
                    public void onDone(D done) {
                        if (mCallEnvs.remove(request.getId()) != null) {
                            responder.respondSuccess(converter.convertDone(done));
                        }
                    }
                }).fail(new FailCallback<F>() {
                    @Override
                    public void onFail(F result) {
                        if (mCallEnvs.remove(request.getId()) != null) {
                            CallException e = converter.convertFail(result);
                            responder.respondFailure(e);
                        }
                    }
                });
            }
        });
    }

    private CompetitionSessionInfo
    checkSessionInfo(Responder responder, Collection<String> competingItemIds) {
        try {
            return mMasterService.getActiveCompetitionSession(competingItemIds);
        } catch (MasterService.CompetitionSessionNotFoundException e) {
            responder.respondFailure(CallGlobalCode.INTERNAL_ERROR, "Internal error. " +
                    "Competition session info NOT found. competingItemIds=" + competingItemIds);
            return null;
        }
    }

    public <D, F> void onCall(
            final Request request,
            Collection<String> competingItemIds,
            final Responder responder,
            final SessionCallable<D, F, Void> callable,
            final DFConverter<D, F> converter) {
        onCall(request, competingItemIds, responder, callable, (DFPConverter<D, F, Void>) converter);
    }

    public <F> void onCall(
            final Request request,
            Collection<String> competingItemIds,
            final Responder responder,
            final SessionCallable<Void, F, Void> callable,
            final FConverter<F> converter) {
        onCall(request, competingItemIds, responder, callable, (DFPConverter<Void, F, Void>) converter);
    }

    public <D, F, P> void onCall(
            final Request request,
            String competingItemId,
            final Responder responder,
            final SessionCallable<D, F, P> callable,
            final DFPConverter<D, F, P> converter) {
        onCall(request, Collections.singleton(competingItemId), responder, callable, converter);
    }

    public <D, F> void onCall(
            final Request request,
            String competingItemId,
            final Responder responder,
            final SessionCallable<D, F, Void> callable,
            final DFConverter<D, F> converter) {
        onCall(request, competingItemId, responder, callable, (DFPConverter<D, F, Void>) converter);
    }

    public <F> void onCall(
            final Request request,
            String competingItemId,
            final Responder responder,
            final SessionCallable<Void, F, Void> callable,
            final FConverter<F> converter) {
        onCall(request, competingItemId, responder, callable, (DFPConverter<Void, F, Void>) converter);
    }

    public void onCompetitionSessionInactive(final CompetitionSessionInfo sessionInfo) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Iterator<Map.Entry<String, CallEnv>> iterator = mCallEnvs.entrySet().iterator();
                while (iterator.hasNext()) {
                    CallEnv callEnv = iterator.next().getValue();
                    if (!callEnv.sessionInfo.getSessionId().
                            equals(sessionInfo.getSessionId())) {
                        continue;
                    }

                    iterator.remove();

                    callEnv.promise.cancel();
                    callEnv.responder.respondFailure(1, ""); // TODO
                }
            }
        });
    }

    public interface SessionCallable<D, F, P> {

        Promise<D, F, P> call() throws CallException;
    }

    public interface DFPConverter<D, F, P> {

        Param convertDone(D done);

        CallException convertFail(F fail);

        Param convertProgress(P progress);
    }

    public static abstract class DFConverter<D, F> implements DFPConverter<D, F, Void> {

        @Override
        public Param convertProgress(Void progress) {
            return null;
        }
    }

    public static abstract class FConverter<F> extends DFConverter<Void, F> {

        @Override
        public Param convertDone(Void done) {
            return null;
        }
    }

    private static class CallEnv {

        Promise promise;
        Responder responder;
        CompetitionSessionInfo sessionInfo;

        public CallEnv(Promise promise, Responder responder, CompetitionSessionInfo sessionInfo) {
            this.promise = promise;
            this.responder = responder;
            this.sessionInfo = sessionInfo;
        }
    }
}
