package com.ubtrobot.sensor.ipc;

import com.ubtrobot.device.ipc.DeviceConverters;
import com.ubtrobot.device.ipc.DeviceProto;
import com.ubtrobot.sensor.SensorDevice;
import com.ubtrobot.sensor.SensorEvent;

import java.util.List;

public class SensorConverters {

    private SensorConverters() {
    }

    public static DeviceProto.DeviceList toSensorDeviceListProto(List<SensorDevice> deviceList) {
        DeviceProto.DeviceList.Builder builder = DeviceProto.DeviceList.newBuilder();
        for (SensorDevice sensorDevice : deviceList) {
            builder.addDevice(DeviceConverters.toDeviceProto(sensorDevice));
        }

        return builder.build();
    }

    public static SensorDevice toSensorDevicePojo(DeviceProto.Device deviceProto) {
        return new SensorDevice.Builder(deviceProto.getId(), deviceProto.getName())
                .setDescription(deviceProto.getDescription()).build();
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