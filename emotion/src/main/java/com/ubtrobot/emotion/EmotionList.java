package com.ubtrobot.emotion;

import com.ubtrobot.cache.CachedField;
import com.ubtrobot.emotion.ipc.EmotionConstants;
import com.ubtrobot.emotion.ipc.EmotionConverters;
import com.ubtrobot.emotion.ipc.EmotionProto;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.util.Collections;
import java.util.List;

/**
 * 情绪列表
 */
public class EmotionList {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("EmotionList");

    private final ProtoCallAdapter mEmotionService;

    private CachedField<List<Emotion>> mEmotions;

    EmotionList(ProtoCallAdapter emotionService) {
        mEmotionService = emotionService;

        mEmotions = new CachedField<>(new CachedField.FieldGetter<List<Emotion>>() {
            @Override
            public List<Emotion> get() {
                try {
                    EmotionProto.EmotionList emotionList = mEmotionService.syncCall(
                            EmotionConstants.CALL_PATH_EMOTION_LIST, EmotionProto.EmotionList.class);
                    return Collections.unmodifiableList(
                            EmotionConverters.toEmotionListPojo(emotionList));
                } catch (CallException e) {
                    LOGGER.e(e, "Framework error when getting the emotion list.");
                    return null;
                }
            }
        });
    }

    /**
     * 获取情绪列表
     *
     * @return 情绪列表
     */
    public List<Emotion> all() {
        List<Emotion> emotions = mEmotions.get();
        return emotions == null ? Collections.<Emotion>emptyList() : emotions;
    }

    /**
     * 获取情绪
     *
     * @param emotionId 情绪 id
     * @return 情绪
     * @throws EmotionNotFoundException 无法找到运行时异常
     */
    public Emotion get(String emotionId) {
        for (Emotion emotion : all()) {
            if (emotion.getId().equals(emotionId)) {
                return emotion;
            }
        }

        throw new EmotionNotFoundException();
    }

    public static class EmotionNotFoundException extends RuntimeException {
    }
}