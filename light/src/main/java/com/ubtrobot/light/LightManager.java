package com.ubtrobot.light;

import android.os.Handler;
import android.os.Looper;
import android.util.Pair;

import com.ubtrobot.async.Promise;
import com.ubtrobot.light.ipc.LightConstants;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.competition.ActivateException;
import com.ubtrobot.master.competition.Competing;
import com.ubtrobot.master.competition.CompetingItem;
import com.ubtrobot.master.competition.CompetitionSession;
import com.ubtrobot.master.competition.CompetitionSessionExt;
import com.ubtrobot.master.context.MasterContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static com.ubtrobot.light.EffectDisplayer.DEFAULT_OPTION;

public class LightManager {

    private final MasterContext mMasterContext;

    private final LightList mLightList;

    private final LightingEffectList mEffectList;
    private final EffectDisplayer mEffectDisplayer;

    private final HashMap<String, Pair<LightCluster, CompetitionSessionExt<LightCluster>>>
            mLightClusterSessions = new HashMap<>();

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
        mEffectList = new LightingEffectList(lightService);
        mEffectDisplayer = new EffectDisplayer(mLightList, mEffectList, handler);
    }

    public List<Light> getLightList() {
        return mLightList.all();
    }

    public Light getLight(String lightId) {
        return mLightList.get(lightId);
    }

    public Promise<Void, LightException>
    turnOn(String lightId, final int argb) {
        Pair<LightCluster, CompetitionSessionExt<LightCluster>> pair = lightSession(lightId);
        return pair.second.execute(
                pair.first,
                new CompetitionSessionExt.SessionCallable<
                        Void, LightException, LightCluster>() {
                    @Override
                    public Promise<Void, LightException>
                    call(CompetitionSession session, LightCluster lightCluster) {
                        return lightCluster.lights.get(0).turnOn(session, argb);
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

    private Pair<LightCluster, CompetitionSessionExt<LightCluster>> lightSession(String lightId) {
        return lightsSession(Collections.singletonList(lightId));
    }

    private Pair<LightCluster, CompetitionSessionExt<LightCluster>>
    lightsSession(List<String> lightIds) {
        synchronized (mLightClusterSessions) {
            StringBuilder builder = new StringBuilder();
            for (String lightId : lightIds) {
                builder.append(lightId);
                builder.append("|");
            }
            String lightsIdSeq = builder.toString();

            Pair<LightCluster, CompetitionSessionExt<LightCluster>> pair =
                    mLightClusterSessions.get(lightsIdSeq);
            if (pair != null) {
                return pair;
            }

            LinkedList<Light> lights = new LinkedList<>();
            for (String lightId : lightIds) {
                lights.add(mLightList.get(lightId));
            }
            LightCluster cluster = new LightCluster(lights);

            pair = new Pair<>(cluster, new CompetitionSessionExt<LightCluster>(
                    mMasterContext.openCompetitionSession().addCompeting(cluster)));
            mLightClusterSessions.put(lightsIdSeq, pair);

            return pair;
        }
    }

    public boolean isTurnOn(String lightId) {
        return getLight(lightId).isTurnOn();
    }

    public Promise<Void, LightException> changeColor(String lightId, final int argb) {
        Pair<LightCluster, CompetitionSessionExt<LightCluster>> pair = lightSession(lightId);
        return pair.second.execute(
                pair.first,
                new CompetitionSessionExt.SessionCallable<
                        Void, LightException, LightCluster>() {
                    @Override
                    public Promise<Void, LightException>
                    call(CompetitionSession session, LightCluster lightCluster) {
                        return lightCluster.lights.get(0).changeColor(session, argb);
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

    public List<LightingEffect> getEffectList() {
        return mEffectList.all();
    }

    public LightingEffect getEffect(String effectId) {
        return mEffectList.get(effectId);
    }

    public Promise<Void, DisplayException> display(
            List<String> lightIds,
            final String effectId,
            final DisplayOption option) {
        if (lightIds == null || lightIds.isEmpty()) {
            throw new IllegalArgumentException("Argument lightIds is null or empty.");
        }

        final List<String> normalIds = normalizeLightIds(lightIds);
        Pair<LightCluster, CompetitionSessionExt<LightCluster>> pair = lightsSession(normalIds);
        return pair.second.execute(
                pair.first,
                new CompetitionSessionExt.SessionCallable<
                        Void, DisplayException, LightCluster>() {
                    @Override
                    public Promise<Void, DisplayException>
                    call(CompetitionSession session, LightCluster competing) {
                        return mEffectDisplayer.display(session, normalIds, effectId, option);
                    }
                },
                new CompetitionSessionExt.Converter<DisplayException>() {
                    @Override
                    public DisplayException convert(ActivateException e) {
                        return new DisplayException.Factory().occupied(e);
                    }
                }
        );
    }

    private List<String> normalizeLightIds(List<String> lightIds) {
        HashSet<String> idSet = new HashSet<>();
        LinkedList<String> normalIds = new LinkedList<>();
        for (String lightId : lightIds) {
            if (idSet.contains(lightId)) {
                continue;
            }

            idSet.add(lightId);
            normalIds.add(lightId);
        }

        Collections.sort(normalIds);
        return normalIds;
    }

    public Promise<Void, DisplayException> display(
            List<String> lightIds,
            String effectId) {
        return display(lightIds, effectId, DEFAULT_OPTION);
    }

    public Promise<Void, DisplayException> display(
            String lightId,
            String effectId,
            DisplayOption option) {
        return display(Collections.singletonList(lightId), effectId, option);
    }

    public EffectDisplayer effectDisplayer() {
        return mEffectDisplayer;
    }

    public Promise<Void, DisplayException> display(
            String lightId,
            String effectId) {
        return display(Collections.singletonList(lightId), effectId, DEFAULT_OPTION);
    }

    public Promise<Void, LightException> turnOff(String lightId) {
        Pair<LightCluster, CompetitionSessionExt<LightCluster>> pair = lightSession(lightId);
        return pair.second.execute(
                pair.first,
                new CompetitionSessionExt.SessionCallable<
                        Void, LightException, LightCluster>() {
                    @Override
                    public Promise<Void, LightException>
                    call(CompetitionSession session, LightCluster lightCluster) {
                        return lightCluster.lights.get(0).turnOff(session);
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

    private static class LightCluster implements Competing {

        final List<Light> lights;

        public LightCluster(List<Light> lights) {
            this.lights = lights;
        }

        @Override
        public List<CompetingItem> getCompetingItems() {
            LinkedList<CompetingItem> items = new LinkedList<>();
            for (Light light : lights) {
                items.addAll(light.getCompetingItems());
            }

            return items;
        }
    }
}