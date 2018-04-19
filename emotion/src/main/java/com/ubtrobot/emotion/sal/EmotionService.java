package com.ubtrobot.emotion.sal;

import com.ubtrobot.async.Promise;
import com.ubtrobot.emotion.Emotion;
import com.ubtrobot.emotion.ExpressException;
import com.ubtrobot.emotion.ExpressOption;
import com.ubtrobot.exception.AccessServiceException;

import java.util.List;

public interface EmotionService {

    Promise<List<Emotion>, AccessServiceException, Void> getEmotionList();

    Promise<Void, ExpressException, Void> express(String emotionId, ExpressOption option);

    Promise<Void, ExpressException, Void> dismiss();
}