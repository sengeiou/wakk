package com.ubtrobot.speech;

import android.os.Handler;
import android.text.TextUtils;

import com.ubtrobot.async.Promise;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.speech.ipc.SpeechConstant;
import com.ubtrobot.speech.ipc.SpeechConverters;
import com.ubtrobot.speech.ipc.SpeechProto;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

public class UnderstanderProxy implements Understander {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("UnderstanderProxy");

    private final ProtoCallAdapter mSpeechService;
    private Handler mHandler;

    public UnderstanderProxy(ProtoCallAdapter mSpeechService, Handler mHandler) {
        this.mSpeechService = mSpeechService;
        this.mHandler = mHandler;
    }

    public Promise<UnderstandResult, UnderstandException> understand(String question,
            UnderstandOption option) {
        if (TextUtils.isEmpty(question)) {
            throw new IllegalArgumentException("Question to be understand must not be null.");
        }

        return mSpeechService.call(
                SpeechConstant.CALL_PATH_UNDERSTAND,
                SpeechConverters.toUnderstandOptionProto(option, question),
                new ProtoCallAdapter.DFProtoConverter<
                        UnderstandResult, SpeechProto.UnderstandResult, UnderstandException>() {
                    @Override
                    public Class<SpeechProto.UnderstandResult> doneProtoClass() {
                        return SpeechProto.UnderstandResult.class;
                    }

                    @Override
                    public UnderstandResult convertDone(SpeechProto.UnderstandResult result) {
                        return SpeechConverters.toUnderstandResultPojo(result);
                    }

                    @Override
                    public UnderstandException convertFail(CallException e) {
                        return new UnderstandException.Factory().from(e);
                    }
                });
    }
}
