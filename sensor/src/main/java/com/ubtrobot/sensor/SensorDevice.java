package com.ubtrobot.sensor;

import com.ubtrobot.device.Device;

public class SensorDevice extends Device {

    private SensorDevice(Builder builder) {
        super(builder);
    }

    @Override
    public String toString() {
        return "SensorDevice{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                '}';
    }

    public static class Builder extends Device.GenericBuilder<Builder> {

        public Builder(String id, String name) {
            super(id, name);
        }

        @Override
        public SensorDevice build() {
            return new SensorDevice(this);
        }
    }
}
