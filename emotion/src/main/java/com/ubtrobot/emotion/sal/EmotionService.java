package com.ubtrobot.emotion.sal;

import com.ubtrobot.async.Promise;
import com.ubtrobot.emotion.Emotion;
import com.ubtrobot.emotion.EmotionException;

import java.util.List;

public interface EmotionService {

    Promise<List<Emotion>, EmotionException, Void> getEmotionList();
}