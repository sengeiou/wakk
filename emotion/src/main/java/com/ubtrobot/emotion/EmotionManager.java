package com.ubtrobot.emotion;

import android.os.Handler;
import android.os.Looper;

import com.ubtrobot.async.Promise;
import com.ubtrobot.emotion.ipc.EmotionConstants;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.competition.ActivateException;
import com.ubtrobot.master.competition.CompetitionSession;
import com.ubtrobot.master.competition.CompetitionSessionExt;
import com.ubtrobot.master.context.MasterContext;

import java.util.List;

/**
 * 情绪服务
 */
public class EmotionManager {

    private final MasterContext mMasterContext;

    private final EmotionList mEmotionList;
    private final Expresser mExpresser;
    private volatile CompetitionSessionExt<Expresser> mSession;

    public EmotionManager(MasterContext masterContext) {
        if (masterContext == null) {
            throw new IllegalArgumentException("Argument masterContext is null.");
        }

        mMasterContext = masterContext;

        Handler handler = new Handler(Looper.getMainLooper());
        ProtoCallAdapter emotionService = new ProtoCallAdapter(
                masterContext.createSystemServiceProxy(EmotionConstants.SERVICE_NAME),
                handler
        );
        mEmotionList = new EmotionList(emotionService);
        mExpresser = new Expresser(mEmotionList, handler);
    }

    /**
     * 获取情绪列表
     *
     * @return 情绪列表
     */
    public List<Emotion> getEmotionList() {
        return mEmotionList.all();
    }

    /**
     * 获取情绪
     *
     * @param emotionId 情绪 id
     * @return 情绪
     * @throws EmotionList.EmotionNotFoundException 无法找到情绪的运行时异常
     */
    public Emotion getEmotion(String emotionId) {
        return mEmotionList.get(emotionId);
    }

    public Expresser expresser() {
        return mExpresser;
    }

    private CompetitionSessionExt<Expresser> expresserSession() {
        if (mSession != null) {
            return mSession;
        }

        synchronized (this) {
            if (mSession != null) {
                return mSession;
            }

            mSession = new CompetitionSessionExt<>(mMasterContext.openCompetitionSession().
                    addCompeting(mExpresser));
            return mSession;
        }
    }

    public Promise<Void, ExpressException, Void>
    express(final String emotionId, final ExpressOption option) {
        return expresserSession().execute(
                mExpresser,
                new CompetitionSessionExt.SessionCallable<Void, ExpressException, Void, Expresser>() {
                    @Override
                    public Promise<Void, ExpressException, Void>
                    call(CompetitionSession session, Expresser expresser) {
                        return expresser.express(session, emotionId, option);
                    }
                },
                new CompetitionSessionExt.Converter<ExpressException>() {
                    @Override
                    public ExpressException convert(ActivateException e) {
                        return new ExpressException.Factory().occupied(e);
                    }
                }
        );
    }

    public Promise<Void, ExpressException, Void>
    express(String emotionId) {
        return express(emotionId, ExpressOption.DEFAULT);
    }

    public Promise<Void, ExpressException, Void> dismiss() {
        return expresserSession().execute(
                mExpresser,
                new CompetitionSessionExt.SessionCallable<
                        Void, ExpressException, Void, Expresser>() {
                    @Override
                    public Promise<Void, ExpressException, Void>
                    call(CompetitionSession session, Expresser expresser) {
                        return expresser.dismiss(session);
                    }
                },
                new CompetitionSessionExt.Converter<ExpressException>() {
                    @Override
                    public ExpressException convert(ActivateException e) {
                        return new ExpressException.Factory().occupied(e);
                    }
                }
        );
    }
}