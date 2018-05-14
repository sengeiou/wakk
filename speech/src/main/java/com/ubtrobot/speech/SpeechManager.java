package com.ubtrobot.speech;


import android.os.Handler;
import android.os.Looper;

import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.adapter.CallAdapter;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.competition.ActivateException;
import com.ubtrobot.master.competition.CompetitionSession;
import com.ubtrobot.master.competition.CompetitionSessionExt;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.speech.ipc.SpeechConstant;
import com.ubtrobot.speech.ipc.SpeechConverters;
import com.ubtrobot.speech.ipc.SpeechProto;
import com.ubtrobot.speech.understand.UnderstandResult;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.util.List;

public class SpeechManager {

    private Logger LOGGER = FwLoggerFactory.getLogger("SpeechManager");

    private final MasterContext mMasterContext;
    private final Synthesizer mSynthesizer;
    private final Recognizer mRecognizer;
    private final Understander mUnderstander;

    private final SpeakerList mSpeakerList;

    private volatile CompetitionSessionExt mSynthesizerSession;
    private volatile CompetitionSessionExt<Recognizer> mRecognizeSession;

    private ProtoCallAdapter mSpeechService;
    private Handler mHandler;

    public SpeechManager(MasterContext masterContext) {
        if (masterContext == null) {
            throw new IllegalArgumentException("Argument masterContext is null.");
        }

        mMasterContext = masterContext;

        mHandler = new Handler(Looper.getMainLooper());
        mSpeechService = new ProtoCallAdapter(
                mMasterContext.createSystemServiceProxy(SpeechConstant.SERVICE_NAME),
                mHandler
        );

        mSynthesizer = new Synthesizer(mSpeechService, mHandler);
        mRecognizer = new Recognizer(mSpeechService, mHandler);
        mUnderstander = new Understander(mSpeechService, mHandler);
        mSpeakerList = new SpeakerList(mSpeechService);
    }

    private CompetitionSessionExt<Synthesizer> synthesizerSession() {
        if (mSynthesizerSession != null) {
            return mSynthesizerSession;
        }

        synchronized (this) {
            if (mSynthesizerSession != null) {
                return mSynthesizerSession;
            }

            mSynthesizerSession = new CompetitionSessionExt<>(mMasterContext.openCompetitionSession()
                    .addCompeting(mSynthesizer));

            return mSynthesizerSession;
        }
    }

    public List<Speaker> getSpeakerList() {
        return mSpeakerList.all();
    }

    public ProgressivePromise<Void, SynthesizeException, Synthesizer.SynthesizingProgress> synthesize(
            final String sentence, final SynthesizeOption option) {
        LOGGER.i("Speech Manager synthesize");
        return synthesizerSession().execute(mSynthesizer, new CompetitionSessionExt.SessionProgressiveCallable<
                Void, SynthesizeException, Synthesizer.SynthesizingProgress, Synthesizer>() {
            @Override
            public ProgressivePromise<Void, SynthesizeException, Synthesizer.SynthesizingProgress>
            call(CompetitionSession session, Synthesizer competing) {
                return mSynthesizer.synthesize(session, sentence, option);
            }
        }, new CompetitionSessionExt.Converter<SynthesizeException>() {
            @Override
            public SynthesizeException convert(ActivateException e) {
                return new SynthesizeException.Factory().occupied(e);
            }
        });
    }

    public ProgressivePromise<Void, SynthesizeException, Synthesizer.SynthesizingProgress> synthesize(
            final String sentence) {
        return synthesize(sentence, SynthesizeOption.DEFAULT);
    }

    public Synthesizer synthesizer() {
        return mSynthesizer;
    }

    private CompetitionSessionExt<Recognizer> recognizerSession() {
        if (mRecognizeSession != null) {
            return mRecognizeSession;
        }

        synchronized (this) {
            if (mRecognizeSession != null) {
                return mRecognizeSession;
            }

            mRecognizeSession = new CompetitionSessionExt<>(
                    mMasterContext.openCompetitionSession().addCompeting(mRecognizer));
            return mRecognizeSession;
        }
    }

    public Recognizer recognizer() {
        return mRecognizer;
    }

    public ProgressivePromise<Recognizer.RecognizeResult, RecognizeException, Recognizer.RecognizingProgress> recognize(
            final RecognizeOption option) {
        return recognizerSession().execute(mRecognizer, new CompetitionSessionExt.SessionProgressiveCallable<
                Recognizer.RecognizeResult, RecognizeException, Recognizer.RecognizingProgress, Recognizer>() {
            @Override
            public ProgressivePromise<Recognizer.RecognizeResult, RecognizeException, Recognizer.RecognizingProgress>
            call(CompetitionSession session, Recognizer competing) {
                return mRecognizer.recognize(session, option);
            }
        }, new CompetitionSessionExt.Converter<RecognizeException>() {
            @Override
            public RecognizeException convert(ActivateException e) {
                return new RecognizeException.Factory().occupied(e);
            }
        });
    }

    public ProgressivePromise<
            Recognizer.RecognizeResult, RecognizeException, Recognizer.RecognizingProgress>
    recognize() {
        return recognize(RecognizeOption.DEFAULT);
    }

    public Promise<UnderstandResult, UnderstandException> understand(String question, UnderstandOption option) {
        return mUnderstander.understand(question, option);
    }

    public Promise<UnderstandResult, UnderstandException> understand(String question) {
        return understand(question, UnderstandOption.DEFAULT);
    }

    public Configuration getConfiguration() {
        try {
            SpeechProto.Configuration configuration = mSpeechService.syncCall(SpeechConstant.CALL_PATH_GET_CONFIG, SpeechProto.Configuration.class);
            return SpeechConverters.toConfigurationPojo(configuration);
        } catch (CallException e) {
            LOGGER.e(e, "Framework error when getConfiguration");
        }

        return new Configuration.Builder().build();
    }

    public void setConfiguration(Configuration configuration) {
        //todo 这只是设置一个配置参数过去，用的同步还是异步
        mSpeechService.call(SpeechConstant.CALL_PATH_SET_CONFIG,
                SpeechConverters.toConfigurationProto(configuration), new CallAdapter.FConverter<AccessServiceException>() {
                    @Override
                    public AccessServiceException convertFail(CallException e) {
                        return new AccessServiceException.Factory().from(e);
                    }
                });
    }
}
