package com.ubtrobot.speech.ipc.master;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Message;
import com.ubtrobot.async.Promise;
import com.ubtrobot.master.adapter.ProtoParamParser;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.competition.CompetingCallDelegate;
import com.ubtrobot.master.competition.CompetingItemDetail;
import com.ubtrobot.master.competition.CompetitionSessionInfo;
import com.ubtrobot.master.competition.ProtoCompetingCallDelegate;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.master.service.MasterSystemService;
import com.ubtrobot.speech.RecognizeException;
import com.ubtrobot.speech.RecognizeOption;
import com.ubtrobot.speech.Recognizer;
import com.ubtrobot.speech.SynthesizeException;
import com.ubtrobot.speech.Synthesizer;
import com.ubtrobot.speech.ipc.SpeechConstant;
import com.ubtrobot.speech.ipc.SpeechConverters;
import com.ubtrobot.speech.ipc.SpeechProto;
import com.ubtrobot.speech.sal.AbstractSpeechService;
import com.ubtrobot.speech.sal.SpeechFactory;
import com.ubtrobot.speech.sal.SpeechService;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.util.LinkedList;
import java.util.List;

public class SpeechSystemService extends MasterSystemService {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("SpeechSystemService");

    private SpeechService mSpeechService;

    private ProtoCompetingCallDelegate mCompetingCallDelegate;

    @Override
    protected void onServiceCreate() {
        Application application = getApplication();
        if (!(application instanceof SpeechFactory)) {
            throw new RuntimeException("Application must implement SpeechFactory");
        }

        mSpeechService = ((SpeechFactory) application).createSpeechService();

        if (mSpeechService == null) {
            throw new RuntimeException("Application must return service by createService()");
        }

        if (!(mSpeechService instanceof AbstractSpeechService)) {
            throw new IllegalStateException(
                    "Application must return a AbstractSpeechService instance");
        }

        mCompetingCallDelegate = new ProtoCompetingCallDelegate(this, new Handler(Looper.getMainLooper()));
    }

    @Override
    protected List<CompetingItemDetail> getCompetingItems() {
        List<CompetingItemDetail> list = new LinkedList<>();
        list.add(new CompetingItemDetail.Builder(SpeechConstant.SERVICE_NAME, SpeechConstant.COMPETING_ITEM_SYNTHESIZER)
                .setDescription("the synthesize competing item")
                .addCallPath(SpeechConstant.CALL_PATH_SYNTHESIZE)
                .build());

        list.add(new CompetingItemDetail.Builder(SpeechConstant.SERVICE_NAME, SpeechConstant.COMPETING_ITEM_RECOGNIZER)
                .setDescription("the recognize competing item")
                .addCallPath(SpeechConstant.CALL_PATH_RECOGNIZE)
                .build());

        return list;

    }

    @Call(path = SpeechConstant.CALL_PATH_SYNTHESIZE)
    public void synthesize(final Request request, final Responder responder) {
        final SpeechProto.SynthesizeOption option = ProtoParamParser.parseParam(
                request,
                SpeechProto.SynthesizeOption.class,
                responder
        );

        if (option == null) {
            LOGGER.w("synthesize option == null");
            return;
        }

        mCompetingCallDelegate.onCall(request,
                SpeechConstant.COMPETING_ITEM_SYNTHESIZER, responder,
                new CompetingCallDelegate.SessionCallable<
                        Void, SynthesizeException, Synthesizer.SynthesizingProgress>() {
                    @Override
                    public Promise<Void, SynthesizeException, Synthesizer.SynthesizingProgress>
                    call() throws CallException {
                        return mSpeechService.synthesize(
                                option.getSentence(),
                                SpeechConverters.toSynthesizeOptionPojo(option));
                    }
                },
                new ProtoCompetingCallDelegate.DFPConverter<
                        Void, SynthesizeException, Synthesizer.SynthesizingProgress>() {
                    @Override
                    public Message convertDone(Void done) {
                        return null;
                    }

                    @Override
                    public CallException convertFail(SynthesizeException fail) {
                        return new CallException(fail.getCode(), fail.getMessage());
                    }

                    @Override
                    public Message convertProgress(Synthesizer.SynthesizingProgress progress) {
                        return SpeechConverters.toSynthesizingProgressProto(progress);
                    }
                });
    }

    @Call(path = SpeechConstant.CALL_PATH_SYNTHESIZING)
    public void isSynthesizing(Request request, Responder responder) {
        responder.respondSuccess(ProtoParam.create(BoolValue.newBuilder()
                .setValue(mSpeechService.isSynthesizing())
                .build()));
    }

    @Call(path = SpeechConstant.CALL_PATH_RECOGNIZE)
    public void recognize(Request request, Responder responder) {
        final SpeechProto.RecognizeOption protoOption = ProtoParamParser.parseParam(
                request,
                SpeechProto.RecognizeOption.class,
                responder
        );

        if (protoOption == null) {
            LOGGER.w("recognize option == null");
            return;
        }

        final RecognizeOption option = SpeechConverters.toRecognizeOptionPojo(protoOption);
        mCompetingCallDelegate.onCall(
                request,
                SpeechConstant.COMPETING_ITEM_RECOGNIZER,
                responder,
                new CompetingCallDelegate.SessionCallable<
                        Recognizer.RecognizeResult, RecognizeException, Recognizer.RecognizingProgress>() {
                    @Override
                    public Promise<Recognizer.RecognizeResult, RecognizeException, Recognizer.RecognizingProgress>
                    call() throws CallException {
                        return mSpeechService.recognize(option);
                    }
                },
                new ProtoCompetingCallDelegate.DFPConverter<
                        Recognizer.RecognizeResult, RecognizeException, Recognizer.RecognizingProgress>() {
                    @Override
                    public Message convertDone(Recognizer.RecognizeResult done) {
                        return SpeechConverters.toRecognizeResultProto(done);
                    }

                    @Override
                    public CallException convertFail(RecognizeException fail) {
                        return new CallException(fail.getCode(), fail.getMessage());
                    }

                    @Override
                    public Message convertProgress(Recognizer.RecognizingProgress progress) {
                        return SpeechConverters.toRecognizingProgressProto(progress);
                    }
                });
    }

    @Call(path = SpeechConstant.CALL_PATH_RECOGNIZING)
    public void isRecognizing(Request request, Responder responder) {
        responder.respondSuccess(ProtoParam.create(BoolValue.newBuilder()
                .setValue(mSpeechService.isRecognizing())
                .build()));
    }

    @Override
    protected void onCompetitionSessionInactive(CompetitionSessionInfo sessionInfo) {
        mCompetingCallDelegate.onCompetitionSessionInactive(sessionInfo);
    }
}
