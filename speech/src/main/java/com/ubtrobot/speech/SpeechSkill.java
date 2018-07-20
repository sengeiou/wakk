package com.ubtrobot.speech;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ubtrobot.master.skill.MasterSkill;
import com.ubtrobot.master.skill.SkillStopCause;
import com.ubtrobot.master.transport.message.parcel.ParcelableParam;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class SpeechSkill extends MasterSkill {

    private Logger LOGGER = FwLoggerFactory.getLogger("SpeechSkill");
    private Map<String, Method> mIntents = new HashMap<>();
    private Handler mHandler;

    public SpeechSkill() {
        mHandler = new Handler(Looper.getMainLooper());
        mIntents = IntentAnnotationsLoader.loadIntentMethods(
                this.getClass());
    }

    @Override
    protected void onSkillCreate() {

    }

    @Override
    protected void onSkillStart() {

    }

    @Override
    protected void onSkillStop(SkillStopCause skillStopCause) {

    }

    protected void onIntent(SpeechInteraction intent) {

    }

    @Override
    public final void onCall(Request request, final Responder responder) {
        try {
            if (request.getParam().isEmpty()) {
                SpeechInteraction.Builder speechInteractionBuilder =
                        new SpeechInteraction.Builder();
                SpeechInteraction speechInteraction = speechInteractionBuilder.build();
                postMainThread(speechInteraction, request.getPath());
            } else {
                SpeechInteraction parcelable = ParcelableParam.from(request.getParam(),
                        SpeechInteraction.class).getParcelable();
                postMainThread(parcelable, request.getPath());
            }
            responder.respondSuccess();
        } catch (ParcelableParam.InvalidParcelableParamException e) {
            LOGGER.e("parse SpeechInteraction error");
            e.printStackTrace();
            responder.respondFailure(new CallException(-1, e.getMessage()));
        }
        Log.i("SpeechSkill", mIntents.toString());
    }

    private void postMainThread(final SpeechInteraction interaction, final String path) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                handRequest(interaction, path);
            }
        });
    }

    private void handRequest(final SpeechInteraction intent, String path) {
        Method method = mIntents.get(path);
        if (method == null) {
            onIntent(intent);
            return;
        }

        try {
            method.invoke(SpeechSkill.this, intent);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }
}
