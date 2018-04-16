package com.ubtrobot.emotion.ipc.master;

import android.app.Application;
import android.os.Handler;

import com.google.protobuf.Message;
import com.ubtrobot.async.Promise;
import com.ubtrobot.emotion.Emotion;
import com.ubtrobot.emotion.EmotionException;
import com.ubtrobot.emotion.ipc.EmotionConstants;
import com.ubtrobot.emotion.ipc.EmotionConverters;
import com.ubtrobot.emotion.sal.AbstractEmotionService;
import com.ubtrobot.emotion.sal.EmotionFactory;
import com.ubtrobot.emotion.sal.EmotionService;
import com.ubtrobot.master.adapter.CallProcessAdapter;
import com.ubtrobot.master.adapter.ProtoCallProcessAdapter;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.service.MasterSystemService;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;

import java.util.List;

public class EmotionSystemService extends MasterSystemService {

    private EmotionService mService;
    private ProtoCallProcessAdapter mCallProcessor;

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

        mCallProcessor = new ProtoCallProcessAdapter(new Handler(getMainLooper()));
    }

    @Call(path = EmotionConstants.CALL_PATH_EMOTION_LIST)
    public void onGetEmotionList(Request request, Responder responder) {
        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<List<Emotion>, EmotionException, Void>() {
                    @Override
                    public Promise<List<Emotion>, EmotionException, Void> call() throws CallException {
                        return mService.getEmotionList();
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<List<Emotion>, EmotionException>() {
                    @Override
                    public Message convertDone(List<Emotion> emotions) {
                        return EmotionConverters.toEmotionListProto(emotions);
                    }

                    @Override
                    public CallException convertFail(EmotionException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }
}
