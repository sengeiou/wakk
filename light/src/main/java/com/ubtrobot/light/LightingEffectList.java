package com.ubtrobot.light;

import com.ubtrobot.cache.CachedField;
import com.ubtrobot.light.ipc.LightConstants;
import com.ubtrobot.light.ipc.LightConverters;
import com.ubtrobot.light.ipc.LightProto;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.util.Collections;
import java.util.List;

public class LightingEffectList {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("LightingEffectList");

    private CachedField<List<LightingEffect>> mEffects;

    public LightingEffectList(final ProtoCallAdapter lightService) {
        mEffects = new CachedField<>(new CachedField.FieldGetter<List<LightingEffect>>() {
            @Override
            public List<LightingEffect> get() {
                try {
                    List<LightingEffect> effects = LightConverters.toLightingEffectListPojo(
                            lightService.syncCall(LightConstants.CALL_PATH_GET_LIGHTING_EFFECT_LIST,
                                    LightProto.LightingEffectList.class));
                    return Collections.unmodifiableList(effects);
                } catch (CallException e) {
                    LOGGER.e(e, "Framework error when getting the light effect list.");
                } catch (ClassNotFoundException e) {
                    LOGGER.e(e, "Framework error when getting the light effect list.");
                }
                return null;
            }
        });
    }

    public List<LightingEffect> all() {
        List<LightingEffect> effects = mEffects.get();
        return effects == null ? Collections.<LightingEffect>emptyList() : effects;
    }

    public LightingEffect get(String effectId) {
        for (LightingEffect effect : all()) {
            if (effect.getId().equals(effectId)) {
                return effect;
            }
        }

        throw new LightingEffectNotFoundException();
    }

    public static class LightingEffectNotFoundException extends RuntimeException {
    }
}
