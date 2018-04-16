package com.ubtrobot.emotion.sal;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.async.Promise;
import com.ubtrobot.emotion.Emotion;
import com.ubtrobot.emotion.EmotionException;

import java.util.List;

public abstract class AbstractEmotionService implements EmotionService {

    @Override
    public Promise<List<Emotion>, EmotionException, Void> getEmotionList() {
        AsyncTask<List<Emotion>, EmotionException, Void> task = createGetEmotionListTask();
        if (task == null) {
            throw new IllegalArgumentException("Argument task is null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<List<Emotion>, EmotionException, Void>
    createGetEmotionListTask();
}
