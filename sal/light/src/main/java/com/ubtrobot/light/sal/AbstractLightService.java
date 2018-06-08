package com.ubtrobot.light.sal;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.async.InterruptibleAsyncTask;
import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.light.DisplayException;
import com.ubtrobot.light.DisplayOption;
import com.ubtrobot.light.LightDevice;
import com.ubtrobot.light.LightException;
import com.ubtrobot.light.LightingEffect;
import com.ubtrobot.master.competition.InterruptibleTaskHelper;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public abstract class AbstractLightService implements LightService {

    private static final String TASK_RECEIVER_LIGHT_PREFIX = "light-";
    private static final String TASK_NAME_TURN_ON = "turn-on";
    private static final String TASK_NAME_CHANGE_COLOR = "change-color";
    private static final String TASK_NAME_DISPLAY_EFFECT = "display-effect";
    private static final String TASK_NAME_TURN_OFF = "turn-off";

    private final InterruptibleTaskHelper mInterruptibleTaskHelper;

    public AbstractLightService() {
        mInterruptibleTaskHelper = new InterruptibleTaskHelper();
    }

    @Override
    public Promise<List<LightDevice>, AccessServiceException> getLightList() {
        AsyncTask<List<LightDevice>, AccessServiceException> task = createGetLightListTask();
        if (task == null) {
            throw new IllegalStateException("createGetLightListTask returns null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<List<LightDevice>, AccessServiceException>
    createGetLightListTask();

    @Override
    public Promise<Void, LightException> turnOn(final String lightId, int argb) {
        return mInterruptibleTaskHelper.start(
                TASK_RECEIVER_LIGHT_PREFIX + lightId,
                TASK_NAME_TURN_ON,
                new InterruptibleAsyncTask<Void, LightException>() {
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
                    public LightException
                    createInterruptedException(Set<String> interrupters) {
                        return new LightException.Factory().interrupted(
                                "Interrupted by " + interrupters);
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
    public Promise<Void, LightException> changeColor(final String lightId, final int argb) {
        return mInterruptibleTaskHelper.start(
                TASK_RECEIVER_LIGHT_PREFIX + lightId,
                TASK_NAME_CHANGE_COLOR,
                new InterruptibleAsyncTask<Void, LightException>() {
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
                    public LightException
                    createInterruptedException(Set<String> interrupters) {
                        return new LightException.Factory().interrupted(
                                "Interrupted by " + interrupters);
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
    public Promise<List<LightingEffect>, AccessServiceException> getEffectList() {
        AsyncTask<List<LightingEffect>, AccessServiceException> task = createGetEffectList();
        if (task == null) {
            throw new IllegalStateException("createGetEffectList return null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<List<LightingEffect>, AccessServiceException>
    createGetEffectList();

    @Override
    public Promise<Void, DisplayException>
    display(final List<String> lightIds, final String effectId, final DisplayOption option) {
        return mInterruptibleTaskHelper.start(
                lightsReceivers(lightIds),
                TASK_NAME_DISPLAY_EFFECT,
                new InterruptibleTaskHelper.Session(),
                new InterruptibleAsyncTask<Void, DisplayException>() {
                    @Override
                    protected void onStart() {
                        startDisplayingEffect(lightIds, effectId, option);
                    }

                    @Override
                    protected void onCancel() {
                        // Ignore
                    }
                },
                new InterruptibleTaskHelper.InterruptedExceptionCreator<DisplayException>() {
                    @Override
                    public DisplayException createInterruptedException(Set<String> interrupters) {
                        return new DisplayException.Factory().interrupted(
                                "Interrupted by " + interrupters);
                    }
                }
        );
    }

    private List<String> lightsReceivers(List<String> lightIds) {
        LinkedList<String> receivers = new LinkedList<>();
        for (String lightId : lightIds) {
            receivers.add(TASK_RECEIVER_LIGHT_PREFIX + lightId);
        }
        return receivers;
    }

    protected abstract void
    startDisplayingEffect(List<String> lightIds, String effectId, DisplayOption options);

    public void resolveDisplayingEffect(List<String> lightIds) {
        if (lightIds == null || lightIds.isEmpty()) {
            throw new IllegalArgumentException("Argument lightIds is null or an empty list.");
        }

        mInterruptibleTaskHelper.resolve(lightsReceivers(lightIds), TASK_NAME_DISPLAY_EFFECT, null);
    }

    public void rejectDisplayingEffect(List<String> lightIds, DisplayException e) {
        if (lightIds == null || lightIds.isEmpty()) {
            throw new IllegalArgumentException("Argument lightIds is null or an empty list.");
        }
        if (e == null) {
            throw new IllegalArgumentException("Argument e is null.");
        }

        mInterruptibleTaskHelper.reject(lightsReceivers(lightIds), TASK_NAME_DISPLAY_EFFECT, e);

    }

    @Override
    public Promise<Void, LightException> turnOff(final String lightId) {
        return mInterruptibleTaskHelper.start(
                TASK_RECEIVER_LIGHT_PREFIX + lightId,
                TASK_NAME_TURN_OFF,
                new InterruptibleAsyncTask<Void, LightException>() {
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
                    public LightException
                    createInterruptedException(Set<String> interrupters) {
                        return new LightException.Factory().interrupted(
                                "Interrupted by " + interrupters);
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
