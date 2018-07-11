package com.ubtrobot.sensor;

import android.os.Handler;
import android.os.Looper;

import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.sensor.ipc.SensorConstants;

import java.util.List;
import java.util.Map;

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

    public List<Sensor> getSensorList(String type) {
        return mSensorList.getSensorList(type);
    }

    public Promise<Boolean, SensorException> enableSensor(String sensorId) {
        return mSensorList.get(sensorId).enable();
    }

    public Promise<Boolean, AccessServiceException> isSensorEnable(String sensorId) {
        return mSensorList.get(sensorId).isEnable();
    }

    public Promise<Void, SensorException> controlSensor(String sensorId, String command) {
        return controlSensor(sensorId, command, null);
    }

    public Promise<Void, SensorException>
    controlSensor(String sensorId, String command, Map<String, String> options) {
        return mSensorList.get(sensorId).control(command, options);
    }

    public Promise<Boolean, SensorException> disableSensor(String sensorId) {
        return mSensorList.get(sensorId).disable();
    }

    public void registerSensorListener(String sensorId, SensorListener listener) {
        mSensorList.get(sensorId).registerSensorListener(listener);
    }

    public void unregisterSensorListener(String sensorId, SensorListener listener) {
        mSensorList.get(sensorId).unregisterSensorListener(listener);
    }
}
