package com.ubtrobot.light.sal;

import com.ubtrobot.async.Promise;
import com.ubtrobot.light.LightDevice;
import com.ubtrobot.light.LightException;

import java.util.List;

public interface LightService {

    Promise<List<LightDevice>, LightException, Void> getLightList();
}
