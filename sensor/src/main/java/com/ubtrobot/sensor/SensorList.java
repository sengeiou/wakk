package com.ubtrobot.sensor;

import com.ubtrobot.cache.CachedField;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.sensor.ipc.SensorConstants;
import com.ubtrobot.sensor.ipc.SensorConverters;
import com.ubtrobot.sensor.ipc.SensorProto;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SensorList {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("SensorList");

    private final CachedField<List<Sensor>> mSensors;
    private final CachedField<Map<String, Sensor>> mSensorMap;
    private final CachedField<Map<String, List<Sensor>>> mTypeSensorMap;

    SensorList(final MasterContext masterContext, final ProtoCallAdapter sensorService) {
        mSensors = new CachedField<>(new CachedField.FieldGetter<List<Sensor>>() {
            @Override
            public List<Sensor> get() {
                try {
                    SensorProto.SensorDeviceList deviceList = sensorService.syncCall(
                            SensorConstants.CALL_PATH_GET_SENSOR_LIST, SensorProto.SensorDeviceList.class);
                    LinkedList<Sensor> sensors = new LinkedList<>();
                    for (SensorProto.SensorDevice device : deviceList.getSensorDeviceList()) {
                        sensors.add(new Sensor(masterContext, sensorService,
                                SensorConverters.toSensorDevicePojo(device)));
                    }

                    return Collections.unmodifiableList(sensors);
                } catch (CallException e) {
                    LOGGER.e(e, "Framework error when getting the sensor list.");
                    return null;
                }
            }
        });
        mSensorMap = new CachedField<>(new CachedField.FieldGetter<Map<String, Sensor>>() {
            @Override
            public Map<String, Sensor> get() {
                List<Sensor> sensors = mSensors.get();
                if (sensors == null) {
                    return null;
                }

                HashMap<String, Sensor> sensorMap = new HashMap<>();
                for (Sensor sensor : sensors) {
                    sensorMap.put(sensor.getId(), sensor);
                }

                return sensorMap;
            }
        });
        mTypeSensorMap = new CachedField<>(new CachedField.FieldGetter<Map<String, List<Sensor>>>() {
            @Override
            public Map<String, List<Sensor>> get() {
                List<Sensor> sensors = mSensors.get();
                if (sensors == null) {
                    return null;
                }

                HashMap<String, List<Sensor>> sensorMap = new HashMap<>();
                for (Sensor sensor : sensors) {
                    String type = sensor.getDevice().getType();

                    List<Sensor> sensorList = sensorMap.get(type);
                    if (sensorList == null) {
                        sensorList = new LinkedList<>();
                        sensorMap.put(type, sensorList);
                    }

                    sensorList.add(sensor);
                }

                return sensorMap;
            }
        });
    }

    public List<Sensor> all() {
        List<Sensor> sensors = mSensors.get();
        return sensors == null ? Collections.<Sensor>emptyList() : sensors;
    }

    public Sensor get(String sensorId) {
        Map<String, Sensor> sensorMap = mSensorMap.get();
        Sensor sensor = sensorMap == null ? null : sensorMap.get(sensorId);

        if (sensor == null) {
            throw new SensorNotFoundException();
        }
        return sensor;
    }

    public List<Sensor> getSensorList(String type) {
        return mTypeSensorMap.get().get(type);
    }

    public static class SensorNotFoundException extends RuntimeException {
    }
}