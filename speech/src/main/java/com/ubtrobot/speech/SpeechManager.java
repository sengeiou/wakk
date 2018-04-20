package com.ubtrobot.speech;


import android.os.Handler;
import android.os.Looper;

import com.ubtrobot.async.Promise;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.competition.ActivateException;
import com.ubtrobot.master.competition.CompetitionSession;
import com.ubtrobot.master.competition.CompetitionSessionExt;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.speech.ipc.SpeechConstant;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

public class SpeechManager {

    private Logger LOGGER = FwLoggerFactory.getLogger("SpeechManager");

    private final MasterContext mMasterContext;
    private final Synthesizer mSynthesizer;
    private volatile CompetitionSessionExt mSynthesizerSession;

    private Handler mHandler;

    public SpeechManager(MasterContext masterContext) {
        if (masterContext == null) {
            throw new IllegalArgumentException("Argument masterContext is null.");
        }

        mMasterContext = masterContext;

        mHandler = new Handler(Looper.getMainLooper());
        ProtoCallAdapter mSpeechService = new ProtoCallAdapter(
                mMasterContext.createSystemServiceProxy(SpeechConstant.SERVICE_NAME),
                mHandler
        );

        mSynthesizer = new Synthesizer(mSpeechService, mHandler);
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

    public Promise<Void, SynthesizeException, Synthesizer.SynthesizingProgress> synthesize(
            final String sentence, final SynthesizeOption option) {
        return synthesizerSession().execute(mSynthesizer, new CompetitionSessionExt.SessionCallable<
                Void, SynthesizeException, Synthesizer.SynthesizingProgress, Synthesizer>() {
            @Override
            public Promise<Void, SynthesizeException, Synthesizer.SynthesizingProgress>
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

    public Promise<Void, SynthesizeException, Synthesizer.SynthesizingProgress> synthesize(
            final String sentence) {
        return synthesize(sentence, SynthesizeOption.DEFAULT);
    }

    public Synthesizer synthesizer() {
        return mSynthesizer;
    }
}
