package com.ubtrobot.light.sal;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.async.Promise;
import com.ubtrobot.light.LightDevice;
import com.ubtrobot.light.LightException;

import java.util.List;

public abstract class AbstractLightService implements LightService {

    public AbstractLightService() {
    }

    @Override
    public Promise<List<LightDevice>, LightException, Void> getLightList() {
        AsyncTask<List<LightDevice>, LightException, Void> task = createGetLightListTask();
        if (task == null) {
            throw new IllegalStateException("createGetLightListTask returns null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<List<LightDevice>, LightException, Void>
    createGetLightListTask();
}
