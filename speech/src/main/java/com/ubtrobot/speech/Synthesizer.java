package com.ubtrobot.speech;

import android.os.Handler;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Message;
import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.competition.Competing;
import com.ubtrobot.master.competition.CompetingItem;
import com.ubtrobot.master.competition.CompetitionSession;
import com.ubtrobot.speech.ipc.SpeechConstant;
import com.ubtrobot.speech.ipc.SpeechConverters;
import com.ubtrobot.speech.ipc.SpeechProto;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.util.Collections;
import java.util.List;

public class Synthesizer implements Competing {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("Synthesizer");

    private final ProtoCallAdapter mSpeechService;

    private Handler mHandler;

    public Synthesizer(ProtoCallAdapter speechService, Handler handler) {
        mSpeechService = speechService;
        mHandler = handler;
    }

    @Override
    public List<CompetingItem> getCompetingItems() {
        return Collections.singletonList(new CompetingItem
                (SpeechConstant.SERVICE_NAME, SpeechConstant.COMPETING_ITEM_SYNTHESIZER));
    }

    public ProgressivePromise<Void, SynthesizeException, SynthesizingProgress> synthesize(
            CompetitionSession session, String sentence, final SynthesizeOption option) {
        checkSession(session);

        LOGGER.i("call synthesize");
        if (option == null) {
            throw new IllegalArgumentException("Argument destination or option is null.");
        }

        ProtoCallAdapter synthesizeService = new ProtoCallAdapter(
                session.createSystemServiceProxy(SpeechConstant.SERVICE_NAME),
                mHandler
        );

        return synthesizeService.callStickily(SpeechConstant.CALL_PATH_SYNTHESIZE,
                SpeechConverters.toSynthesizeOptionProto(option, sentence),
                new ProtoCallAdapter.DFPProtoConverter<
                        Void, Message,
                        SynthesizeException,
                        SynthesizingProgress,
                        SpeechProto.SynthesizingProgress>() {
                    @Override
                    public Class<Message> doneProtoClass() {
                        return Message.class;
                    }

                    @Override
                    public Void convertDone(Message protoParam) {
                        return null;
                    }

                    @Override
                    public SynthesizeException convertFail(CallException e) {
                        return new SynthesizeException.Factory().from(e);
                    }

                    @Override
                    public Class<SpeechProto.SynthesizingProgress> progressProtoClass() {
                        return SpeechProto.SynthesizingProgress.class;
                    }

                    @Override
                    public SynthesizingProgress convertProgress(
                            SpeechProto.SynthesizingProgress progress) {
                        return SpeechConverters.toSynthesizingProgressPojo(progress);
                    }
                });
    }

    public boolean isSynthesizing() {
        try {
            return mSpeechService.syncCall(SpeechConstant.CALL_PATH_SYNTHESIZING, BoolValue.class).
                    getValue();
        } catch (CallException e) {
            LOGGER.e(e, "Framework error when querying if in the synthesizing");
            e.printStackTrace();
            return false;
        }
    }

    private void checkSession(CompetitionSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Argument session is null.");
        }

        if (!session.containsCompeting(this)) {
            throw new IllegalArgumentException("The competition session does NOT contain the " +
                    "synthesizer.");
        }
    }

    public static class SynthesizingProgress {

        public static final int STATE_BEGAN = 0;
        public static final int STATE_PROGRESS = 1;
        public static final int STATE_ENDED = 2;

        private final int state;
        private float progress;

        public SynthesizingProgress(int state, float progress) {
            this.state = state;
            this.progress = progress;
        }

        public boolean isBegan() {
            return state == STATE_BEGAN;
        }

        public boolean isEnded() {
            return state == STATE_ENDED;
        }

        public boolean isProgress() {
            return state == STATE_PROGRESS;
        }

        public int getState() {
            return state;
        }

        public float getProgress() {
            return progress;
        }
    }
}
