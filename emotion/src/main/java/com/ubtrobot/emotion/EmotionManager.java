package com.ubtrobot.emotion;

import android.os.Handler;
import android.os.Looper;

import com.ubtrobot.emotion.ipc.EmotionConstants;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.context.MasterContext;

import java.util.List;

/**
 * 情绪服务
 */
public class EmotionManager {

    private final EmotionList mEmotionList;

    public EmotionManager(MasterContext masterContext) {
        if (masterContext == null) {
            throw new IllegalArgumentException("Argument masterContext is null.");
        }

        ProtoCallAdapter emotionService = new ProtoCallAdapter(
                masterContext.createSystemServiceProxy(EmotionConstants.SERVICE_NAME),
                new Handler(Looper.getMainLooper())
        );
        mEmotionList = new EmotionList(emotionService);
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
}