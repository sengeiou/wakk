package com.ubtrobot.sensor.sal;

import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.sensor.SensorDevice;

import java.util.List;

public interface SensorService {

    Promise<List<SensorDevice>, AccessServiceException> getSensorList();

    Promise<Boolean, AccessServiceException> enableSensor(String sensorId);

    Promise<Boolean, AccessServiceException> isSensorEnable(String sensorId);

    Promise<Boolean, AccessServiceException> disableSensor(String sensorId);
}
