package com.ubtrobot.light.sal;

import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.light.LightDevice;
import com.ubtrobot.light.LightException;
import com.ubtrobot.light.LightingEffect;

import java.util.List;

public interface LightService {

    Promise<List<LightDevice>, AccessServiceException, Void> getLightList();

    Promise<Void, LightException, Void> turnOn(String lightId, final int argb);

    boolean isOn(String lightId);

    Promise<Void, LightException, Void> changeColor(String lightId, int argb);

    int getColor(String lightId);

    Promise<Void, LightException, Void> turnOff(String lightId);

    Promise<List<LightingEffect>, AccessServiceException, Void> getEffectList();
}