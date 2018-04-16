package com.ubtrobot.light;

import android.os.Handler;
import android.os.Looper;

import com.ubtrobot.async.Promise;
import com.ubtrobot.light.ipc.LightConstants;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.competition.ActivateException;
import com.ubtrobot.master.competition.CompetitionSession;
import com.ubtrobot.master.competition.CompetitionSessionExt;
import com.ubtrobot.master.context.MasterContext;

import java.util.HashMap;
import java.util.List;

public class LightManager {

    private final MasterContext mMasterContext;

    private final LightList mLightList;

    private final HashMap<String, CompetitionSessionExt<Light>> mLightSessions = new HashMap<>();

    public LightManager(MasterContext masterContext) {
        if (masterContext == null) {
            throw new IllegalArgumentException("Argument masterContext is null.");
        }

        mMasterContext = masterContext;
        Handler handler = new Handler(Looper.getMainLooper());
        ProtoCallAdapter lightService = new ProtoCallAdapter(
                mMasterContext.createSystemServiceProxy(LightConstants.SERVICE_NAME),
                handler
        );
        mLightList = new LightList(lightService, handler);
    }

    public List<Light> getLightList() {
        return mLightList.all();
    }

    public Light getLight(String lightId) {
        return mLightList.get(lightId);
    }

    public Promise<Void, LightException, Void>
    turnOn(String lightId, final int argb) {
        final Light light = getLight(lightId);
        return lightSession(light).execute(
                light,
                new CompetitionSessionExt.SessionCallable<Void, LightException, Void, Light>() {
                    @Override
                    public Promise<Void, LightException, Void>
                    call(CompetitionSession session, Light light) {
                        return light.turnOn(session, argb);
                    }
                },
                new CompetitionSessionExt.Converter<LightException>() {
                    @Override
                    public LightException convert(ActivateException e) {
                        return new LightException.Factory().occupied(e);
                    }
                }
        );
    }

    private CompetitionSessionExt<Light> lightSession(Light light) {
        synchronized (mLightSessions) {
            CompetitionSessionExt<Light> sessionExt = mLightSessions.get(light.getId());
            if (sessionExt != null) {
                return sessionExt;
            }

            sessionExt = new CompetitionSessionExt<>(mMasterContext.openCompetitionSession().
                    addCompeting(light));
            mLightSessions.put(light.getId(), sessionExt);

            return sessionExt;
        }
    }

    public boolean isTurnOn(String lightId) {
        return getLight(lightId).isTurnOn();
    }

    public Promise<Void, LightException, Void> changeColor(String lightId, final int argb) {
        final Light light = getLight(lightId);
        return lightSession(light).execute(
                light,
                new CompetitionSessionExt.SessionCallable<Void, LightException, Void, Light>() {
                    @Override
                    public Promise<Void, LightException, Void>
                    call(CompetitionSession session, Light light) {
                        return light.changeColor(session, argb);
                    }
                },
                new CompetitionSessionExt.Converter<LightException>() {
                    @Override
                    public LightException convert(ActivateException e) {
                        return new LightException.Factory().occupied(e);
                    }
                }
        );
    }

    public int getColor(String lightId) {
        return getLight(lightId).getColor();
    }

    public Promise<Void, LightException, Void> turnOff(String lightId) {
        final Light light = getLight(lightId);
        return lightSession(light).execute(
                light,
                new CompetitionSessionExt.SessionCallable<Void, LightException, Void, Light>() {
                    @Override
                    public Promise<Void, LightException, Void>
                    call(CompetitionSession session, Light light) {
                        return light.turnOff(session);
                    }
                },
                new CompetitionSessionExt.Converter<LightException>() {
                    @Override
                    public LightException convert(ActivateException e) {
                        return new LightException.Factory().occupied(e);
                    }
                }
        );
    }
}