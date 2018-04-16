package com.ubtrobot.light;

import android.os.Handler;
import android.os.Looper;

import com.ubtrobot.light.ipc.LightConstants;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.context.MasterContext;

import java.util.List;

public class LightManager {

    private final LightList mLightList;

    public LightManager(MasterContext masterContext) {
        if (masterContext == null) {
            throw new IllegalArgumentException("Argument masterContext is null.");
        }

        ProtoCallAdapter lightService = new ProtoCallAdapter(
                masterContext.createSystemServiceProxy(LightConstants.SERVICE_NAME),
                new Handler(Looper.getMainLooper())
        );
        mLightList = new LightList(lightService);
    }

    public List<Light> getLightList() {
        return mLightList.all();
    }

    public Light getLight(String lightId) {
        return mLightList.get(lightId);
    }
}