package com.ubtrobot.light.sal;

import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.light.DisplayException;
import com.ubtrobot.light.DisplayOption;
import com.ubtrobot.light.LightDevice;
import com.ubtrobot.light.LightException;
import com.ubtrobot.light.LightingEffect;

import java.util.List;

public interface LightService {

    Promise<List<LightDevice>, AccessServiceException> getLightList();

    Promise<Void, LightException> turnOn(String lightId, final int argb);

    boolean isOn(String lightId);

    Promise<Void, LightException> changeColor(String lightId, int argb);

    int getColor(String lightId);

    Promise<Void, LightException> turnOff(String lightId);

    Promise<List<LightingEffect>, AccessServiceException> getEffectList();

    Promise<Void, DisplayException> display(
            List<String> lightIds, String effectId, DisplayOption option);
}