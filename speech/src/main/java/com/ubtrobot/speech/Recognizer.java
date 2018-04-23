package com.ubtrobot.speech;

import android.os.Handler;
import android.text.TextUtils;

import com.google.protobuf.BoolValue;
import com.ubtrobot.async.Promise;
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

public class Recognizer implements Competing {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("Recognizer");

    private final ProtoCallAdapter mSpeechService;

    private Handler mHandler;

    public Recognizer(ProtoCallAdapter speechService, Handler handler) {
        mSpeechService = speechService;
        mHandler = handler;
    }

    @Override
    public List<CompetingItem> getCompetingItems() {
        return Collections.singletonList(new CompetingItem
                (SpeechConstant.SERVICE_NAME, SpeechConstant.COMPETING_ITEM_RECOGNIZER));
    }

    public Promise<RecognizeResult, RecognizeException, RecognizingProgress> recognize(
            CompetitionSession session, RecognizeOption option) {
        checkSession(session);
        ProtoCallAdapter recognizeService = new ProtoCallAdapter(
                session.createSystemServiceProxy(SpeechConstant.SERVICE_NAME), mHandler);
        return recognizeService.callStickily(
                SpeechConstant.CALL_PATH_RECOGNIZE,
                SpeechConverters.toRecognizeOptionProto(option),
                new ProtoCallAdapter.DFPProtoConverter<
                        RecognizeResult,
                        SpeechProto.RecognizeResult,
                        RecognizeException,
                        RecognizingProgress,
                        SpeechProto.RecognizingProgress>() {
                    @Override
                    public Class<SpeechProto.RecognizeResult> doneProtoClass() {
                        return SpeechProto.RecognizeResult.class;
                    }

                    @Override
                    public RecognizeResult convertDone(SpeechProto.RecognizeResult done) {
                        return SpeechConverters.toRecognizeResultPojo(done);
                    }

                    @Override
                    public RecognizeException convertFail(CallException e) {
                        return new RecognizeException.Factory().from(e);
                    }

                    @Override
                    public Class<SpeechProto.RecognizingProgress> progressProtoClass() {
                        return SpeechProto.RecognizingProgress.class;
                    }

                    @Override
                    public RecognizingProgress convertProgress(SpeechProto.RecognizingProgress progress) {
                        return SpeechConverters.toRecognizingProgressPojo(progress);
                    }
                });
    }

    public boolean isRecognizing() {
        try {
            return mSpeechService.syncCall(SpeechConstant.CALL_PATH_RECOGNIZING, BoolValue.class).
                    getValue();
        } catch (CallException e) {
            LOGGER.e(e, "Framework error when querying if in the recognizing");
            return false;
        }
    }

    private void checkSession(CompetitionSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Argument session is null.");
        }

        if (!session.containsCompeting(this)) {
            throw new IllegalArgumentException("The competition session does NOT contain the " +
                    "recognize.");
        }
    }

    public static class RecognizingProgress {

        public static final int VOLUME_MAX = 100;
        public static final int VOLUME_MIN = 0;

        public final static int STATE_BEGAN = 0;
        public final static int STATE_RECOGNIZING = 1;
        public final static int STATE_ENDED = 2;
        public final static int STATE_RESULT = 3;

        private int state;
        private int volume;

        private RecognizeResult result;

        private RecognizingProgress(int state) {
            this.state = state;
        }

        public RecognizeResult getResult() {
            return result;
        }

        public int getState() {
            return state;
        }

        public int getVolume() {
            return volume;
        }


        public static class Builder {

            private int state;
            private int volume;

            private RecognizeResult result;

            public Builder(int state) {
                this.state = state;
            }

            public Builder setVolume(int volume) {
                checkVolume(volume);
                this.volume = volume;
                return this;
            }

            private void checkVolume(int volume) {
                if (volume < VOLUME_MIN || volume > VOLUME_MAX) {
                    throw new IllegalArgumentException("Invalid volume value, verify for [0,100].");
                }
            }

            public Builder setResult(RecognizeResult result) {
                checkResult(result);
                this.result = result;
                return this;
            }

            private void checkResult(RecognizeResult result) {
                if (null == result) {
                    throw new IllegalArgumentException("RecognizingProgress.Builder refuse null RecognizeResult.");
                }
            }

            public RecognizingProgress build() {
                RecognizingProgress progress = new RecognizingProgress(state);
                progress.volume = volume;
                progress.result = result;
                return progress;
            }
        }
    }

    public static class RecognizeResult {

        private String text;

        private RecognizeResult(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public static class Builder {
            private String text;

            public Builder(String text) {
                checkText(text);
                this.text = text;
            }

            private void checkText(String text) {
                if (TextUtils.isEmpty(text)) {
                    throw new IllegalArgumentException("RecognizeResult.Builder refuse null text.");
                }
            }

            public RecognizeResult build() {
                return new RecognizeResult(text);
            }
        }
    }
}
