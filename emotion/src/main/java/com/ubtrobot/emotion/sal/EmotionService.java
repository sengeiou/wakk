package com.ubtrobot.emotion.sal;

import com.ubtrobot.async.Promise;
import com.ubtrobot.emotion.Emotion;
import com.ubtrobot.emotion.EmotionException;
import com.ubtrobot.emotion.ExpressException;
import com.ubtrobot.emotion.ExpressOption;

import java.util.List;

public interface EmotionService {

    Promise<List<Emotion>, EmotionException, Void> getEmotionList();

    Promise<Void, ExpressException, Void> express(String emotionId, ExpressOption option);

    Promise<Void, ExpressException, Void> dismiss();
}