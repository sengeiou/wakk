package com.ubtrobot.light.ipc;

import com.ubtrobot.device.ipc.DeviceConverters;
import com.ubtrobot.device.ipc.DeviceProto;
import com.ubtrobot.light.DisplayOption;
import com.ubtrobot.light.LightDevice;
import com.ubtrobot.light.LightingEffect;

import java.util.LinkedList;
import java.util.List;

public class LightConverters {

    private LightConverters() {
    }

    public static LightDevice toLightDevicePojo(DeviceProto.Device deviceProto) {
        return new LightDevice.Builder(deviceProto.getId(), deviceProto.getName()).
                setDescription(deviceProto.getDescription()).
                build();
    }

    public static DeviceProto.DeviceList toLightDeviceListProto(List<LightDevice> deviceList) {
        DeviceProto.DeviceList.Builder builder = DeviceProto.DeviceList.newBuilder();
        for (LightDevice lightDevice : deviceList) {
            builder.addDevice(DeviceConverters.toDeviceProto(lightDevice));
        }

        return builder.build();
    }

    public static LightProto.LightColor toLightColorProto(String lightId, int argb) {
        return LightProto.LightColor.newBuilder().setLightId(lightId).setColor(argb).build();
    }

    public static List<LightingEffect>
    toLightingEffectListPojo(LightProto.LightingEffectList effectList) throws ClassNotFoundException {
        LinkedList<LightingEffect> effects = new LinkedList<>();
        for (LightProto.LightingEffect lightingEffect : effectList.getEffectList()) {
            effects.add(toLightingEffectPojo(lightingEffect));
        }

        return effects;
    }

    @SuppressWarnings("unchecked")
    public static LightingEffect
    toLightingEffectPojo(LightProto.LightingEffect effect) throws ClassNotFoundException {
        return new LightingEffect.Builder(effect.getId()).
                setName(effect.getName()).
                setDescription(effect.getDescription()).
                setOptionClass((Class<? extends DisplayOption>) Class.forName(
                        effect.getOptionClass())).
                build();
    }

    public static LightProto.LightingEffect
    toLightingEffectProto(LightingEffect effect) {
        return LightProto.LightingEffect.newBuilder().
                setId(effect.getId()).
                setName(effect.getName()).
                setDescription(effect.getDescription()).
                setOptionClass(effect.getOptionClass().getName()).
                build();
    }

    public static LightProto.LightingEffectList
    toLightingEffectListProto(List<LightingEffect> effectList) {
        LightProto.LightingEffectList.Builder builder = LightProto.LightingEffectList.newBuilder();
        for (LightingEffect effect : effectList) {
            builder.addEffect(toLightingEffectProto(effect));
        }

        return builder.build();
    }
}
