package com.ubtrobot.emotion.ipc.master;

import android.app.Application;
import android.os.Handler;

import com.google.protobuf.Message;
import com.ubtrobot.async.Promise;
import com.ubtrobot.emotion.Emotion;
import com.ubtrobot.emotion.ExpressException;
import com.ubtrobot.emotion.ipc.EmotionConstants;
import com.ubtrobot.emotion.ipc.EmotionConverters;
import com.ubtrobot.emotion.ipc.EmotionProto;
import com.ubtrobot.emotion.sal.AbstractEmotionService;
import com.ubtrobot.emotion.sal.EmotionFactory;
import com.ubtrobot.emotion.sal.EmotionService;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.adapter.CallProcessAdapter;
import com.ubtrobot.master.adapter.ProtoCallProcessAdapter;
import com.ubtrobot.master.adapter.ProtoParamParser;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.competition.CompetingCallDelegate;
import com.ubtrobot.master.competition.CompetingItemDetail;
import com.ubtrobot.master.competition.CompetitionSessionInfo;
import com.ubtrobot.master.competition.ProtoCompetingCallDelegate;
import com.ubtrobot.master.service.MasterSystemService;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;

import java.util.Collections;
import java.util.List;

public class EmotionSystemService extends MasterSystemService {

    private EmotionService mService;
    private ProtoCallProcessAdapter mCallProcessor;
    private ProtoCompetingCallDelegate mCompetingCallDelegate;

    @Override
    protected void onServiceCreate() {
        Application application = getApplication();
        if (!(application instanceof EmotionFactory)) {
            throw new IllegalStateException(
                    "Your application should implement EmotionFactory interface.");
        }

        mService = ((EmotionFactory) application).createEmotionService();
        if (mService == null || !(mService instanceof AbstractEmotionService)) {
            throw new IllegalStateException("Your application 's createEmotionService returns null" +
                    " or does not return a instance of AbstractEmotionService.");
        }

        Handler handler = new Handler(getMainLooper());
        mCallProcessor = new ProtoCallProcessAdapter(handler);
        mCompetingCallDelegate = new ProtoCompetingCallDelegate(this, handler);
    }

    @Override
    protected List<CompetingItemDetail> getCompetingItems() {
        return Collections.singletonList(new CompetingItemDetail.Builder(
                getName(), EmotionConstants.COMPETING_ITEM_EXPRESSER).
                setDescription("Competing item for expressing emotion.").
                addCallPath(EmotionConstants.CALL_PATH_EXPRESS_EMOTION).
                addCallPath(EmotionConstants.CALL_PATH_DISMISS_EMOTION).
                build()
        );
    }

    @Call(path = EmotionConstants.CALL_PATH_EMOTION_LIST)
    public void onGetEmotionList(Request request, Responder responder) {
        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<List<Emotion>, AccessServiceException>() {
                    @Override
                    public Promise<List<Emotion>, AccessServiceException>
                    call() throws CallException {
                        return mService.getEmotionList();
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<List<Emotion>, AccessServiceException>() {
                    @Override
                    public Message convertDone(List<Emotion> emotions) {
                        return EmotionConverters.toEmotionListProto(emotions);
                    }

                    @Override
                    public CallException convertFail(AccessServiceException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Call(path = EmotionConstants.CALL_PATH_EXPRESS_EMOTION)
    public void onExpressEmotion(Request request, Responder responder) {
        final EmotionProto.ExpressOption option = ProtoParamParser.parseParam(request,
                EmotionProto.ExpressOption.class, responder);
        if (option == null) {
            return;
        }

        mCompetingCallDelegate.onCall(
                request,
                EmotionConstants.COMPETING_ITEM_EXPRESSER,
                responder,
                new CompetingCallDelegate.SessionCallable<Void, ExpressException>() {
                    @Override
                    public Promise<Void, ExpressException> call() throws CallException {
                        return mService.express(option.getEmotionId(),
                                EmotionConverters.toExpressOptionPojo(option));
                    }
                },
                new ProtoCompetingCallDelegate.FConverter<ExpressException>() {
                    @Override
                    public CallException convertFail(ExpressException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Call(path = EmotionConstants.CALL_PATH_DISMISS_EMOTION)
    public void onDissmissEmotion(Request request, Responder responder) {
        mCompetingCallDelegate.onCall(
                request,
                EmotionConstants.COMPETING_ITEM_EXPRESSER,
                responder,
                new CompetingCallDelegate.SessionCallable<Void, ExpressException>() {
                    @Override
                    public Promise<Void, ExpressException>
                    call() throws CallException {
                        return mService.dismiss();
                    }
                },
                new ProtoCompetingCallDelegate.FConverter<ExpressException>() {
                    @Override
                    public CallException convertFail(ExpressException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Override
    protected void onCompetitionSessionInactive(CompetitionSessionInfo sessionInfo) {
        mCompetingCallDelegate.onCompetitionSessionInactive(sessionInfo);
    }
}