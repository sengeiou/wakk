package com.ubtrobot.sensor.ipc;

import com.ubtrobot.sensor.SensorDevice;
import com.ubtrobot.sensor.SensorEvent;

import java.util.List;

public class SensorConverters {

    private SensorConverters() {
    }

    public static SensorProto.SensorDeviceList toSensorDeviceListProto(List<SensorDevice> deviceList) {
        SensorProto.SensorDeviceList.Builder builder = SensorProto.SensorDeviceList.newBuilder();
        for (SensorDevice sensorDevice : deviceList) {
            builder.addSensorDevice(toSensorDeviceProto(sensorDevice));
        }

        return builder.build();
    }

    public static SensorDevice toSensorDevicePojo(SensorProto.SensorDevice sensorDevice) {
        return new SensorDevice.Builder(sensorDevice.getId(), sensorDevice.getName())
                .setType(sensorDevice.getType()).setDescription(sensorDevice.getDescription()).build();
    }

    public static SensorProto.SensorDevice toSensorDeviceProto(SensorDevice sensorDevice) {
        return SensorProto.SensorDevice.newBuilder()
                .setId(sensorDevice.getId())
                .setName(sensorDevice.getName())
                .setType(sensorDevice.getType())
                .setDescription(sensorDevice.getDescription())
                .build();
    }

    public static SensorEvent toSensorEventPojo(SensorProto.SensorEvent event) {
        float[] values = new float[event.getValueCount()];
        int index = 0;
        for (Float value : event.getValueList()) {
            values[index++] = value;
        }

        return new SensorEvent.Builder(event.getSensorId()).setTimestamp(event.getTimestamp())
                .setValues(values).build();
    }

    public static SensorProto.SensorEvent toSensorEventProto(SensorEvent sensorEvent) {
        SensorProto.SensorEvent.Builder builder = SensorProto.SensorEvent.newBuilder()
                .setSensorId(sensorEvent.getSensorId()).setTimestamp(sensorEvent.getTimestamp());
        for (float value : sensorEvent.getValues()) {
            builder.addValue(value);
        }

        return builder.build();
    }
}