package com.ubtrobot.master.competition;

import android.os.Handler;

import com.ubtrobot.async.DoneCallback;
import com.ubtrobot.async.FailCallback;
import com.ubtrobot.async.ProgressCallback;
import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceCompetingItemException;
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

    public <D, F extends Throwable, P> void onCall(
            final Request request,
            Collection<String> competingItemIds,
            final Responder responder,
            final SessionProgressiveCallable<D, F, P> callable,
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
                final ProgressivePromise<D, F, P> promise;
                try {
                    promise = callable.call();
                } catch (CallException e) {
                    responder.respondFailure(e);
                    return;
                }

                onCall(request, sessionInfo, responder, promise, converter);
                promise.progress(new ProgressCallback<P>() {
                    @Override
                    public void onProgress(P progress) {
                        synchronized (mCallEnvs) {
                            if (mCallEnvs.containsKey(request.getId()) && progress != null) {
                                responder.respondStickily(converter.convertProgress(progress));
                            }
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

    private <D, F extends Throwable> void onCall(
            final Request request,
            CompetitionSessionInfo sessionInfo,
            final Responder responder,
            final Promise<D, F> promise,
            final DFConverter<D, F> converter) {
        synchronized (mCallEnvs) {
            mCallEnvs.put(request.getId(), new CallEnv(promise, responder, sessionInfo));

            responder.setCallCancelListener(new CallCancelListener() {
                @Override
                public void onCancel(Request request) {
                    synchronized (mCallEnvs) {
                        if (mCallEnvs.remove(request.getId()) != null) {
                            promise.cancel();
                        }
                    }
                }
            });

            promise.done(new DoneCallback<D>() {
                @Override
                public void onDone(D done) {
                    synchronized (mCallEnvs) {
                        if (mCallEnvs.remove(request.getId()) != null) {
                            responder.respondSuccess(converter.convertDone(done));
                        }
                    }
                }
            }).fail(new FailCallback<F>() {
                @Override
                public void onFail(F result) {
                    synchronized (mCallEnvs) {
                        if (mCallEnvs.remove(request.getId()) != null) {
                            CallException e = converter.convertFail(result);
                            responder.respondFailure(e);
                        }
                    }
                }
            });
        }
    }

    public <D, F extends Throwable> void onCall(
            final Request request,
            Collection<String> competingItemIds,
            final Responder responder,
            final SessionCallable<D, F> callable,
            final DFConverter<D, F> converter) {
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
                final Promise<D, F> promise;
                try {
                    promise = callable.call();
                } catch (CallException e) {
                    responder.respondFailure(e);
                    return;
                }

                onCall(request, sessionInfo, responder, promise, converter);
            }
        });
    }

    public <F extends Throwable> void onCall(
            final Request request,
            Collection<String> competingItemIds,
            final Responder responder,
            final SessionCallable<Void, F> callable,
            final FConverter<F> converter) {
        onCall(request, competingItemIds, responder, callable, (DFConverter<Void, F>) converter);
    }

    public <D, F extends Throwable, P> void onCall(
            final Request request,
            String competingItemId,
            final Responder responder,
            final SessionProgressiveCallable<D, F, P> callable,
            final DFPConverter<D, F, P> converter) {
        onCall(request, Collections.singleton(competingItemId), responder, callable, converter);
    }

    public <D, F extends Throwable> void onCall(
            final Request request,
            String competingItemId,
            final Responder responder,
            final SessionCallable<D, F> callable,
            final DFConverter<D, F> converter) {
        onCall(request, Collections.singleton(competingItemId), responder, callable, converter);
    }

    public <F extends Throwable> void onCall(
            final Request request,
            String competingItemId,
            final Responder responder,
            final SessionCallable<Void, F> callable,
            final FConverter<F> converter) {
        onCall(request, competingItemId, responder, callable, (DFConverter<Void, F>) converter);
    }

    public void onCompetitionSessionInactive(final CompetitionSessionInfo sessionInfo) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (mCallEnvs) {
                    Iterator<Map.Entry<String, CallEnv>> iterator = mCallEnvs.entrySet().iterator();
                    while (iterator.hasNext()) {
                        CallEnv callEnv = iterator.next().getValue();
                        if (!callEnv.sessionInfo.getSessionId().
                                equals(sessionInfo.getSessionId())) {
                            continue;
                        }

                        iterator.remove();

                        callEnv.promise.cancel();
                        AccessServiceCompetingItemException asce =
                                new AccessServiceCompetingItemException.Factory().interrupted(
                                        "Interrupted. Competition session inactive.");
                        callEnv.responder.respondFailure(asce.getCode(), asce.getMessage());
                    }
                }
            }
        });
    }

    public interface SessionCallable<D, F extends Throwable> {

        Promise<D, F> call() throws CallException;
    }

    public interface SessionProgressiveCallable<D, F extends Throwable, P> extends SessionCallable<D, F> {

        ProgressivePromise<D, F, P> call() throws CallException;
    }

    public interface DFPConverter<D, F, P> extends DFConverter<D, F> {

        Param convertProgress(P progress);
    }

    public interface DFConverter<D, F> {

        Param convertDone(D done);

        CallException convertFail(F fail);
    }

    public abstract static class FConverter<F> implements DFConverter<Void, F> {

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
