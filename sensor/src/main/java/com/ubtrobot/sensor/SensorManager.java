package com.ubtrobot.sensor;

import android.os.Handler;
import android.os.Looper;

import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.sensor.ipc.SensorConstants;

import java.util.List;

public class SensorManager {

    private final SensorList mSensorList;

    public SensorManager(MasterContext masterContext) {
        ProtoCallAdapter mSensorService = new ProtoCallAdapter(
                masterContext.createSystemServiceProxy(SensorConstants.SERVICE_NAME),
                new Handler(Looper.getMainLooper())
        );
        mSensorList = new SensorList(masterContext, mSensorService);
    }

    public List<Sensor> getSensorList() {
        return mSensorList.all();
    }

    public Sensor getSensor(String sensorId) {
        return mSensorList.get(sensorId);
    }

    public void registerSensorListener(String sensorId, SensorListener listener) {
        mSensorList.get(sensorId).registerSensorListener(listener);
    }

    public void unregisterSensorListener(String sensorId, SensorListener listener) {
        mSensorList.get(sensorId).unregisterSensorListener(listener);
    }
}
