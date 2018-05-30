package com.ubtrobot.speech.ipc.master;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Message;
import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.adapter.CallProcessAdapter;
import com.ubtrobot.master.adapter.ProtoCallProcessAdapter;
import com.ubtrobot.master.adapter.ProtoParamParser;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.competition.CompetingCallDelegate;
import com.ubtrobot.master.competition.CompetingItemDetail;
import com.ubtrobot.master.competition.CompetitionSessionInfo;
import com.ubtrobot.master.competition.ProtoCompetingCallDelegate;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.master.service.MasterSystemService;
import com.ubtrobot.speech.Configuration;
import com.ubtrobot.speech.RecognizeException;
import com.ubtrobot.speech.RecognizeOption;
import com.ubtrobot.speech.Recognizer;
import com.ubtrobot.speech.Speaker;
import com.ubtrobot.speech.SynthesizeException;
import com.ubtrobot.speech.Synthesizer;
import com.ubtrobot.speech.UnderstandException;
import com.ubtrobot.speech.Understander;
import com.ubtrobot.speech.ipc.SpeechConstant;
import com.ubtrobot.speech.ipc.SpeechConverters;
import com.ubtrobot.speech.ipc.SpeechProto;
import com.ubtrobot.speech.sal.AbstractSpeechService;
import com.ubtrobot.speech.sal.SpeechFactory;
import com.ubtrobot.speech.sal.SpeechService;
import com.ubtrobot.speech.understand.LegacyUnderstandResult;
import com.ubtrobot.speech.understand.UnderstandResult;
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

    private ProtoCallProcessAdapter mCallProcessor;
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

        Handler handler = new Handler(Looper.getMainLooper());
        mSpeechService.registerRecognizeListener(mRecognitionListener);

        mCompetingCallDelegate = new ProtoCompetingCallDelegate(this, handler);
        mCallProcessor = new ProtoCallProcessAdapter(handler);
    }

    @Override
    protected List<CompetingItemDetail> getCompetingItems() {
        List<CompetingItemDetail> list = new LinkedList<>();
        list.add(new CompetingItemDetail.Builder(SpeechConstant.SERVICE_NAME,
                SpeechConstant.COMPETING_ITEM_SYNTHESIZER)
                .setDescription("the synthesize competing item")
                .addCallPath(SpeechConstant.CALL_PATH_SYNTHESIZE)
                .build());

        list.add(new CompetingItemDetail.Builder(SpeechConstant.SERVICE_NAME,
                SpeechConstant.COMPETING_ITEM_RECOGNIZER)
                .setDescription("the recognize competing item")
                .addCallPath(SpeechConstant.CALL_PATH_RECOGNIZE)
                .build());

        return list;

    }

    @Call(path = SpeechConstant.CALL_PATH_SYNTHESIZE)
    public void synthesize(final Request request, final Responder responder) {
        LOGGER.i("SpeechSystemService synthesize");
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
                new CompetingCallDelegate.SessionProgressiveCallable<
                        Void, SynthesizeException, Synthesizer.SynthesizingProgress>() {
                    @Override
                    public ProgressivePromise<Void, SynthesizeException, Synthesizer
                            .SynthesizingProgress>
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
                new CompetingCallDelegate.SessionProgressiveCallable<
                        Recognizer.RecognizeResult, RecognizeException, Recognizer
                        .RecognizingProgress>() {
                    @Override
                    public ProgressivePromise<Recognizer.RecognizeResult, RecognizeException,
                            Recognizer.RecognizingProgress>
                    call() throws CallException {
                        return mSpeechService.recognize(option);
                    }
                },
                new ProtoCompetingCallDelegate.DFPConverter<
                        Recognizer.RecognizeResult, RecognizeException, Recognizer
                        .RecognizingProgress>() {
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

    @Call(path = SpeechConstant.CALL_PATH_UNDERSTAND)
    public void understand(Request request, final Responder responder) {
        LOGGER.i("call ervice understand receive began");
        final SpeechProto.UnderstandOption understandOption = ProtoParamParser.parseParam(request,
                SpeechProto.UnderstandOption.class, responder);
        if (understandOption == null) {
            LOGGER.w("Service understand receive null option");
            return;
        }

        final String question = understandOption.getQuestion();

        mCallProcessor.onCall(responder, new CallProcessAdapter.Callable<
                        LegacyUnderstandResult, UnderstandException>() {
                    @Override
                    public Promise<LegacyUnderstandResult, UnderstandException>
                    call() throws CallException {
                        return mSpeechService.understand(question,
                                SpeechConverters.toUnderstandOptionPojo(understandOption));
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<LegacyUnderstandResult,
                        UnderstandException>() {
                    @Override
                    public Message convertDone(LegacyUnderstandResult result) {
                        return SpeechConverters.toUnderstandResultProto(result);

                    }

                    @Override
                    public CallException convertFail(UnderstandException fail) {
                        return new CallException(fail.getCode(), fail.getMessage());
                    }
                });
    }

    @Call(path = SpeechConstant.CALL_PATH_SPEAKER_LIST)
    public void getSpeakerList(Request request, Responder responder) {
        mCallProcessor.onCall(responder, new CallProcessAdapter.Callable<
                List<Speaker>, AccessServiceException>() {
            @Override
            public Promise<List<Speaker>, AccessServiceException> call() throws CallException {
                return mSpeechService.getSpeakerList();
            }
        }, new ProtoCallProcessAdapter.DFConverter<List<Speaker>, AccessServiceException>() {
            @Override
            public Message convertDone(List<Speaker> speakers) {
                return SpeechConverters.toSpeakersProto(speakers);
            }

            @Override
            public CallException convertFail(AccessServiceException fail) {
                return new CallException(fail.getCode(), fail.getMessage());
            }
        });
    }

    @Call(path = SpeechConstant.CALL_PATH_GET_CONFIG)
    public void getConfiguration(Request request, Responder responder) {
        mCallProcessor.onCall(responder,
                new CallProcessAdapter.Callable<Configuration, AccessServiceException>() {
                    @Override
                    public Promise<Configuration, AccessServiceException> call()
                            throws CallException {
                        return mSpeechService.getConfiguration();
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<Configuration, AccessServiceException>() {
                    @Override
                    public Message convertDone(Configuration configuration) {
                        return SpeechConverters.toConfigurationProto(configuration);
                    }

                    @Override
                    public CallException convertFail(AccessServiceException fail) {
                        return new CallException(fail.getCode(), fail.getMessage());
                    }
                });
    }

    @Call(path = SpeechConstant.CALL_PATH_SET_CONFIG)
    public void setConfiguration(Request request, Responder responder) {
        final SpeechProto.Configuration configuration = ProtoParamParser.parseParam(request,
                SpeechProto.Configuration.class, responder);

        if (configuration == null) {
            return;
        }

        mCallProcessor.onCall(responder,
                new CallProcessAdapter.Callable<Void, AccessServiceException>() {
                    @Override
                    public Promise<Void, AccessServiceException> call() throws CallException {
                        return mSpeechService.setConfiguration(
                                SpeechConverters.toConfigurationPojo(configuration));
                    }
                }, new ProtoCallProcessAdapter.FConverter<AccessServiceException>() {
                    @Override
                    public CallException convertFail(AccessServiceException fail) {
                        return new CallException(fail.getCode(), fail.getMessage());
                    }
                });
    }

    @Override
    protected void onCompetitionSessionInactive(CompetitionSessionInfo sessionInfo) {
        mCompetingCallDelegate.onCompetitionSessionInactive(sessionInfo);
    }

    @Override
    protected void onServiceDestroy() {
        super.onServiceDestroy();
        mSpeechService.unregisterRecognizeListener(mRecognitionListener);
    }

    private RecognizeListener mRecognitionListener = new RecognizeListener() {

        @Override
        public void onRecognizeBegin() {

        }

        @Override
        public void onRecognizing(Recognizer.RecognizingProgress progress) {
            LOGGER.i("Publish recognize event.");
            publishCarefully(
                    SpeechConstant.ACTION_RECOGNIZING,
                    ProtoParam.create(SpeechConverters.toRecognizingProgressProto(progress)));
        }

        @Override
        public void onRecognizeEnd() {

        }

        @Override
        public void onRecognizeComplete(Recognizer.RecognizeResult result) {
            publishCarefully(
                    SpeechConstant.ACTION_RECOGNIZE_RESULT,
                    ProtoParam.create(SpeechConverters.toRecognizeResultProto(result)));
        }

        @Override
        public void OnRecognizeError(RecognizeException e) {
            publishCarefully(
                    SpeechConstant.ACTION_RECOGNIZE_ERROR,
                    ProtoParam.create(SpeechProto.Error.newBuilder().setCode(e.getCode())
                            .setDetail(e.getDetail().toString())
                            .setMessage(e.getMessage())
                            .build()));
        }
    };
}
