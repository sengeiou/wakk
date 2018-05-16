package com.ubtrobot.light;

import android.os.Handler;

import com.ubtrobot.async.Promise;
import com.ubtrobot.light.ipc.LightConstants;
import com.ubtrobot.light.ipc.LightConverters;
import com.ubtrobot.master.adapter.CallAdapter;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.competition.CompetitionSession;
import com.ubtrobot.transport.message.CallException;

import java.util.Collections;
import java.util.List;

public class EffectDisplayer {

    public static final DisplayOption DEFAULT_OPTION =
            new DisplayOption.Builder().setLoops(1).build();

    private final LightList mLightList;
    private final LightingEffectList mEffectList;
    private final Handler mHandler;

    public EffectDisplayer(LightList lightList, LightingEffectList effectList, Handler handler) {
        mLightList = lightList;
        mEffectList = effectList;
        mHandler = handler;
    }

    public Promise<Void, DisplayException> display(
            CompetitionSession session,
            List<String> lightIds,
            String effectId,
            DisplayOption option) {
        if (session == null) {
            throw new IllegalArgumentException("Argument session is null.");
        }

        if (lightIds == null || lightIds.isEmpty()) {
            throw new IllegalArgumentException("Argument lightIds is null or an empty list.");
        }

        mEffectList.get(effectId);// 检查灯效是否存在

        for (String lightId : lightIds) {
            Light light = mLightList.get(lightId);// 检查灯是否存在
            checkSession(session, light);
        }

        // TODO 处理不同类型的 DisplayOption （子类）

        return doDisplay(session, lightIds, effectId, option == null ? DEFAULT_OPTION : option);
    }

    private Promise<Void, DisplayException> doDisplay(
            CompetitionSession session,
            List<String> lightIds,
            String effectId,
            DisplayOption option) {
        ProtoCallAdapter lightService = new ProtoCallAdapter(
                session.createSystemServiceProxy(LightConstants.SERVICE_NAME), mHandler);
        return lightService.call(
                LightConstants.CALL_PATH_DISPLAY_LIGHTING_EFFECT,
                LightConverters.toDisplayOptionProto(lightIds, effectId, option),
                new CallAdapter.FConverter<DisplayException>() {
                    @Override
                    public DisplayException convertFail(CallException e) {
                        return new DisplayException.Factory().from(e);
                    }
                }
        );
    }

    private void checkSession(CompetitionSession session, Light light) {
        if (session == null) {
            throw new IllegalArgumentException("Argument session is null.");
        }

        if (!session.containsCompeting(light)) {
            throw new IllegalArgumentException("The competition session does NOT contain the light.");
        }
    }

    public Promise<Void, DisplayException> display(
            CompetitionSession session,
            List<String> lightIds,
            String effectId) {
        return display(session, lightIds, effectId, DEFAULT_OPTION);
    }

    public Promise<Void, DisplayException> display(
            CompetitionSession session,
            String lightId,
            String effectId,
            DisplayOption option) {
        return display(session, Collections.singletonList(lightId), effectId, option);
    }

    public Promise<Void, DisplayException> display(
            CompetitionSession session,
            String lightId,
            String effectId) {
        return display(session, Collections.singletonList(lightId), effectId, DEFAULT_OPTION);
    }
}
