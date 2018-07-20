package com.ubtrobot.emotion;

import android.os.Handler;

import com.ubtrobot.async.Consumer;
import com.ubtrobot.async.ListenerList;
import com.ubtrobot.async.Promise;
import com.ubtrobot.emotion.ipc.EmotionConstants;
import com.ubtrobot.emotion.ipc.EmotionConverters;
import com.ubtrobot.emotion.ipc.EmotionProto;
import com.ubtrobot.master.adapter.CallAdapter;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.adapter.ProtoEventReceiver;
import com.ubtrobot.master.competition.Competing;
import com.ubtrobot.master.competition.CompetingItem;
import com.ubtrobot.master.competition.CompetitionSession;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.transport.message.CallException;

import java.util.Collections;
import java.util.List;

/**
 * 情绪表达者
 */
public class Expresser implements Competing {

    private MasterContext mMasterContext;
    private final Handler mHandler;
    private final EmotionList mEmotionList;

    private final EmotionStateEventReceiver mEmotionStateEventReceiver;
    private final ListenerList<EmotionListener> mEmotionListeners;

    Expresser(MasterContext masterContext, EmotionList emotionList, Handler handler) {
        mMasterContext = masterContext;
        mEmotionList = emotionList;
        mHandler = handler;

        mEmotionStateEventReceiver = new EmotionStateEventReceiver();
        mEmotionListeners = new ListenerList<>(handler);
    }

    @Override
    public List<CompetingItem> getCompetingItems() {
        return Collections.singletonList(new CompetingItem(EmotionConstants.SERVICE_NAME,
                EmotionConstants.COMPETING_ITEM_EXPRESSER));
    }

    public Promise<Void, ExpressException>
    express(CompetitionSession session, String emotionId, ExpressOption option) {
        checkSession(session);

        mEmotionList.get(emotionId);
        if (option == null) {
            throw new IllegalArgumentException("Argument option is null.");
        }

        ProtoCallAdapter emotionService = new ProtoCallAdapter(
                session.createSystemServiceProxy(EmotionConstants.SERVICE_NAME),
                mHandler
        );
        return emotionService.call(
                EmotionConstants.CALL_PATH_EXPRESS_EMOTION,
                EmotionConverters.toExpressOptionProto(emotionId, option),
                new CallAdapter.FConverter<ExpressException>() {
                    @Override
                    public ExpressException convertFail(CallException e) {
                        return new ExpressException.Factory().from(e);
                    }
                }
        );
    }

    private void checkSession(CompetitionSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Argument session is null.");
        }

        if (!session.containsCompeting(this)) {
            throw new IllegalArgumentException("The competition session does NOT contain the expresser.");
        }
    }


    public Promise<Void, ExpressException>
    express(CompetitionSession session, String emotionId) {
        return express(session, emotionId, ExpressOption.DEFAULT);
    }

    public Promise<Void, ExpressException> dismiss(CompetitionSession session) {
        checkSession(session);

        ProtoCallAdapter emotionService = new ProtoCallAdapter(
                session.createSystemServiceProxy(EmotionConstants.SERVICE_NAME),
                mHandler
        );
        return emotionService.call(
                EmotionConstants.CALL_PATH_DISMISS_EMOTION,
                new CallAdapter.FConverter<ExpressException>() {
                    @Override
                    public ExpressException convertFail(CallException e) {
                        return new ExpressException.Factory().from(e);
                    }
                }
        );
    }

    public void registerEmotionListener(EmotionListener emotionListener){
        synchronized (mEmotionListeners) {
            boolean subscribed = !mEmotionListeners.isEmpty();
            mEmotionListeners.register(emotionListener);

            if (!subscribed) {
                mMasterContext.subscribe(mEmotionStateEventReceiver,
                        EmotionConstants.ACTION_EMOTION_EXPRESS_CHANGE);
            }
        }
    }

    public void unregisterLocationChangeListener(EmotionListener emotionListener){
        synchronized (mEmotionListeners) {
            mEmotionListeners.unregister(emotionListener);

            if (mEmotionListeners.isEmpty()) {
                mMasterContext.unsubscribe(mEmotionStateEventReceiver);
            }
        }
    }

    private class EmotionStateEventReceiver extends ProtoEventReceiver<EmotionProto.EmotionState> {
        @Override
        protected Class<EmotionProto.EmotionState> protoClass() {
            return EmotionProto.EmotionState.class;
        }

        @Override
        public void onReceive(
                MasterContext masterContext, String action, EmotionProto.EmotionState param) {
            synchronized (mEmotionListeners) {
                final boolean isEmotionExpresser = EmotionConverters.toEmotionStatePojo(param);

                mEmotionListeners.forEach(new Consumer<EmotionListener>() {
                    @Override
                    public void accept(EmotionListener listener) {
                        listener.onEmotionChanged(isEmotionExpresser);
                    }
                });
            }
        }
    }
}