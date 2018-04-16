package com.ubtrobot.light;

import android.os.Handler;

import com.ubtrobot.cache.CachedField;
import com.ubtrobot.device.ipc.DeviceProto;
import com.ubtrobot.light.ipc.LightConstants;
import com.ubtrobot.light.ipc.LightConverters;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class LightList {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("LightList");

    private CachedField<List<Light>> mLights;

    public LightList(final ProtoCallAdapter lightService, final Handler handler) {
        mLights = new CachedField<>(new CachedField.FieldGetter<List<Light>>() {
            @Override
            public List<Light> get() {
                try {
                    DeviceProto.DeviceList deviceList = lightService.syncCall(
                            LightConstants.CALL_PATH_GET_LIGHT_LIST, DeviceProto.DeviceList.class);
                    LinkedList<Light> lights = new LinkedList<>();
                    for (DeviceProto.Device device : deviceList.getDeviceList()) {
                        lights.add(new Light(lightService, LightList.this,
                                LightConverters.toLightDevicePojo(device), handler));
                    }

                    return Collections.unmodifiableList(lights);
                } catch (CallException e) {
                    LOGGER.e(e, "Framework error when getting the light list.");
                    return null;
                }
            }
        });
    }

    public List<Light> all() {
        List<Light> lights = mLights.get();
        return lights == null ? Collections.<Light>emptyList() : lights;
    }

    public Light get(String lightId) {
        for (Light light : all()) {
            if (light.getId().equals(lightId)) {
                return light;
            }
        }

        throw new LightNotFoundException();
    }

    public static class LightNotFoundException extends RuntimeException {
    }
}