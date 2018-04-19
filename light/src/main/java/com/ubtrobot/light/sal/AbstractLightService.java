package com.ubtrobot.light.sal;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.async.InterruptibleAsyncTask;
import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.light.LightDevice;
import com.ubtrobot.light.LightException;
import com.ubtrobot.light.LightingEffect;
import com.ubtrobot.master.competition.InterruptibleTaskHelper;

import java.util.List;

public abstract class AbstractLightService implements LightService {

    private static final String TASK_RECEIVER_LIGHT_PREFIX = "light-";
    private static final String TASK_NAME_TURN_ON = "turn-on";
    private static final String TASK_NAME_CHANGE_COLOR = "change-color";
    private static final String TASK_NAME_TURN_OFF = "turn-off";

    private final InterruptibleTaskHelper mInterruptibleTaskHelper;

    public AbstractLightService() {
        mInterruptibleTaskHelper = new InterruptibleTaskHelper();
    }

    @Override
    public Promise<List<LightDevice>, AccessServiceException, Void> getLightList() {
        AsyncTask<List<LightDevice>, AccessServiceException, Void> task = createGetLightListTask();
        if (task == null) {
            throw new IllegalStateException("createGetLightListTask returns null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<List<LightDevice>, AccessServiceException, Void>
    createGetLightListTask();

    @Override
    public Promise<Void, LightException, Void> turnOn(final String lightId, int argb) {
        return mInterruptibleTaskHelper.start(
                TASK_RECEIVER_LIGHT_PREFIX + lightId,
                TASK_NAME_TURN_ON,
                new InterruptibleAsyncTask<Void, LightException, Void>() {
                    @Override
                    protected void onStart() {
                        doStartTurningOn(lightId);
                    }

                    @Override
                    protected void onCancel() {
                        // Ignore
                    }
                },
                new InterruptibleTaskHelper.InterruptedExceptionCreator<LightException>() {
                    @Override
                    public LightException createInterruptedException(String interrupter) {
                        return new LightException.Factory().interrupted("Interrupt the " +
                                interrupter + " task.");
                    }
                }
        );
    }

    protected abstract void doStartTurningOn(String lightId);

    public void resolveTurningOn(String lightId) {
        mInterruptibleTaskHelper.resolve(TASK_RECEIVER_LIGHT_PREFIX + lightId, TASK_NAME_TURN_ON, null);
    }

    public void rejectTurningOn(String lightId, LightException e) {
        if (e == null) {
            throw new IllegalArgumentException("Argument e is null.");
        }

        mInterruptibleTaskHelper.reject(TASK_RECEIVER_LIGHT_PREFIX + lightId, TASK_NAME_TURN_ON, e);
    }

    @Override
    public boolean isOn(String lightId) {
        return doGetIsOn(lightId);
    }

    protected abstract boolean doGetIsOn(String light);

    @Override
    public Promise<Void, LightException, Void> changeColor(final String lightId, final int argb) {
        return mInterruptibleTaskHelper.start(
                TASK_RECEIVER_LIGHT_PREFIX + lightId,
                TASK_NAME_CHANGE_COLOR,
                new InterruptibleAsyncTask<Void, LightException, Void>() {
                    @Override
                    protected void onStart() {
                        doStartChangingColor(lightId, argb);
                    }

                    @Override
                    protected void onCancel() {
                        // Ignore
                    }
                },
                new InterruptibleTaskHelper.InterruptedExceptionCreator<LightException>() {
                    @Override
                    public LightException createInterruptedException(String interrupter) {
                        return new LightException.Factory().interrupted("Interrupt the " +
                                interrupter + " task.");
                    }
                }
        );
    }

    protected abstract void doStartChangingColor(String lightId, int argb);

    public void resolveChangingColor(String lightId) {
        mInterruptibleTaskHelper.resolve(TASK_RECEIVER_LIGHT_PREFIX + lightId,
                TASK_NAME_CHANGE_COLOR, null);
    }

    public void rejectChangingColor(String lightId, LightException e) {
        if (e == null) {
            throw new IllegalArgumentException("Argument e is null.");
        }

        mInterruptibleTaskHelper.reject(TASK_RECEIVER_LIGHT_PREFIX + lightId,
                TASK_NAME_CHANGE_COLOR, e);
    }

    @Override
    public int getColor(String lightId) {
        return doGetColor(lightId);
    }

    protected abstract int doGetColor(String lightId);

    @Override
    public Promise<List<LightingEffect>, AccessServiceException, Void> getEffectList() {
        AsyncTask<List<LightingEffect>, AccessServiceException, Void> task = createGetEffectList();
        if (task == null) {
            throw new IllegalStateException("createGetEffectList return null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<List<LightingEffect>, AccessServiceException, Void>
    createGetEffectList();

    @Override
    public Promise<Void, LightException, Void> turnOff(final String lightId) {
        return mInterruptibleTaskHelper.start(
                TASK_RECEIVER_LIGHT_PREFIX + lightId,
                TASK_NAME_TURN_OFF,
                new InterruptibleAsyncTask<Void, LightException, Void>() {
                    @Override
                    protected void onStart() {
                        doStartTurningOff(lightId);
                    }

                    @Override
                    protected void onCancel() {
                        // Ignore
                    }
                },
                new InterruptibleTaskHelper.InterruptedExceptionCreator<LightException>() {
                    @Override
                    public LightException createInterruptedException(String interrupter) {
                        return new LightException.Factory().interrupted("Interrupt the " +
                                interrupter + " task.");
                    }
                }
        );
    }

    protected abstract void doStartTurningOff(String lightId);

    public void resolveTurningOff(String lightId) {
        mInterruptibleTaskHelper.resolve(TASK_RECEIVER_LIGHT_PREFIX + lightId,
                TASK_NAME_TURN_OFF, null);
    }

    public void rejectTurningOff(String lightId, LightException e) {
        if (e == null) {
            throw new IllegalArgumentException("Argument e is null.");
        }

        mInterruptibleTaskHelper.reject(TASK_RECEIVER_LIGHT_PREFIX + lightId, TASK_NAME_TURN_OFF, e);
    }
}
