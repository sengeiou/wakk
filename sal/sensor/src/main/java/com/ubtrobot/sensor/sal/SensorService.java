package com.ubtrobot.sensor.sal;

import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.sensor.SensorDevice;
import com.ubtrobot.sensor.SensorException;

import java.util.List;
import java.util.Map;

public interface SensorService {

    Promise<List<SensorDevice>, AccessServiceException> getSensorList();

    Promise<Boolean, SensorException> enableSensor(String sensorId);

    Promise<Boolean, AccessServiceException> isSensorEnable(String sensorId);

    Promise<Void, SensorException> control(String sensorId, String command, Map<String, String> options);

    Promise<Boolean, SensorException> disableSensor(String sensorId);
}