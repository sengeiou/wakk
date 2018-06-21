package com.ubtrobot.speech;

import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Int32Value;
import com.ubtrobot.async.Consumer;
import com.ubtrobot.async.ListenerList;
import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.adapter.ProtoEventReceiver;
import com.ubtrobot.master.competition.Competing;
import com.ubtrobot.master.competition.CompetingItem;
import com.ubtrobot.master.competition.CompetitionSession;
import com.ubtrobot.master.context.MasterContext;
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
    private final ListenerList<RecognizeListener> mListener;
    private Handler mHandler;
    private final MasterContext mMasterContext;
    private boolean mHasSubscribed = false;
    private final byte[] mSubscribeLock = new byte[0];

    public Recognizer(ProtoCallAdapter speechService, Handler handler,
            MasterContext masterContext) {
        mSpeechService = speechService;
        mHandler = handler;
        mMasterContext = masterContext;
        mListener = new ListenerList<>(new Handler(Looper.getMainLooper()));
    }

    @Override
    public List<CompetingItem> getCompetingItems() {
        return Collections.singletonList(new CompetingItem
                (SpeechConstant.SERVICE_NAME, SpeechConstant.COMPETING_ITEM_RECOGNIZER));
    }

    public ProgressivePromise<RecognizeResult, RecognizeException, RecognizingProgress> recognize(
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
                    public RecognizingProgress convertProgress(
                            SpeechProto.RecognizingProgress progress) {
                        return SpeechConverters.toRecognizingProgressPojo(progress);
                    }
                });
    }

    public void registerRecognizeListener(RecognizeListener listener) {
        mListener.register(listener);
        synchronized (mSubscribeLock) {
            if (!mListener.isEmpty() && !mHasSubscribed) {
                mHasSubscribed = true;
                subscribeEventReceiver();
            }
        }

    }


    public void unregisterRecognizeListener(RecognizeListener listener) {
        mListener.unregister(listener);
        synchronized (mSubscribeLock) {
            if (mListener.isEmpty() && mHasSubscribed) {
                mHasSubscribed = false;
                unSubscribeEventReceiver();
            }
        }
    }

    private void subscribeEventReceiver() {
        mMasterContext.subscribe(mProgressEventReceiver, SpeechConstant.ACTION_RECOGNIZING);
        mMasterContext.subscribe(mResultEventReceiver, SpeechConstant.ACTION_RECOGNIZE_RESULT);
        mMasterContext.subscribe(mExceptionEventReceiver, SpeechConstant.ACTION_RECOGNIZE_ERROR);
    }

    private void unSubscribeEventReceiver() {
        mMasterContext.unsubscribe(mProgressEventReceiver);
        mMasterContext.unsubscribe(mResultEventReceiver);
        mMasterContext.unsubscribe(mExceptionEventReceiver);

    }

    private final ProtoEventReceiver<SpeechProto.RecognizeResult> mResultEventReceiver =
            new ProtoEventReceiver<SpeechProto.RecognizeResult>() {

                @Override
                public void onReceive(MasterContext masterContext, String action,
                        SpeechProto.RecognizeResult event) {
                    notifyRecognzieComplete(SpeechConverters.toRecognizeResultPojo(event));
                }

                @Override
                protected Class<SpeechProto.RecognizeResult> protoClass() {
                    return SpeechProto.RecognizeResult.class;

                }
            };

    private final ProtoEventReceiver<SpeechProto.RecognizingProgress> mProgressEventReceiver =
            new ProtoEventReceiver<SpeechProto.RecognizingProgress>() {

                @Override
                public void onReceive(MasterContext masterContext, String action,
                        SpeechProto.RecognizingProgress event) {
                    RecognizingProgress progress = SpeechConverters.toRecognizingProgressPojo(
                            event);
                    switch (progress.getState()) {
                        case RecognizingProgress.STATE_BEGAN:
                            notifyRecognzieBegin();
                            break;
                        case RecognizingProgress.STATE_RECOGNIZING:
                            notifyRecognzing(progress);
                            break;
                        case RecognizingProgress.STATE_ENDED:
                            notifyRecognzieEnd();
                            break;
                        case RecognizingProgress.STATE_RESULT:
                            //里面带有识别的中间数据
                            notifyRecognzing(progress);
                            break;
                    }
                }

                @Override
                protected Class<SpeechProto.RecognizingProgress> protoClass() {
                    return SpeechProto.RecognizingProgress.class;
                }
            };
    private final ProtoEventReceiver<SpeechProto.Error> mExceptionEventReceiver =
            new ProtoEventReceiver<SpeechProto.Error>() {

                @Override
                public void onReceive(MasterContext masterContext, String action,
                        SpeechProto.Error event) {

                }

                @Override
                protected Class<SpeechProto.Error> protoClass() {
                    return SpeechProto.Error.class;

                }
            };

    private void notifyRecognzieBegin() {
        mListener.forEach(new Consumer<RecognizeListener>() {
            @Override
            public void accept(RecognizeListener listener) {
                listener.onRecognizeBegin();
            }
        });
    }

    private void notifyRecognzing(final RecognizingProgress progress) {
        mListener.forEach(new Consumer<RecognizeListener>() {
            @Override
            public void accept(RecognizeListener listener) {
                listener.onRecognizing(progress);
            }
        });
    }

    private void notifyRecognzieEnd() {
        mListener.forEach(new Consumer<RecognizeListener>() {
            @Override
            public void accept(RecognizeListener listener) {
                listener.onRecognizeEnd();
            }
        });
    }

    private void notifyRecognzieError(final RecognizeException e) {
        mListener.forEach(new Consumer<RecognizeListener>() {
            @Override
            public void accept(RecognizeListener listener) {
                listener.OnRecognizeError(e);
            }
        });
    }

    private void notifyRecognzieComplete(final RecognizeResult result) {
        mListener.forEach(new Consumer<RecognizeListener>() {
            @Override
            public void accept(RecognizeListener listener) {
                listener.onRecognizeComplete(result);
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
                    throw new IllegalArgumentException(
                            "RecognizingProgress.Builder refuse null RecognizeResult.");
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

    public static class RecognizeResult implements Parcelable {
        public static final RecognizeResult NULL = new RecognizeResult.Builder("").build();

        private String text = "";

        private RecognizeResult(String text) {
            this.text = text;
        }

        private RecognizeResult(Parcel in) {
            readFromParcel(in);
        }

        public String getText() {
            return text;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(text);
        }

        @SuppressWarnings("unchecked")
        private void readFromParcel(Parcel in) {
            text = in.readString();
        }

        public static final Parcelable.Creator<RecognizeResult> CREATOR = new Parcelable
                .Creator<RecognizeResult>() {
            @Override
            public RecognizeResult createFromParcel(Parcel source) {
                return new RecognizeResult(source);
            }

            @Override
            public RecognizeResult[] newArray(int size) {
                return new RecognizeResult[size];
            }
        };

        public static class Builder {
            private String text;

            public Builder(String text) {
                //checkText(text);
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
