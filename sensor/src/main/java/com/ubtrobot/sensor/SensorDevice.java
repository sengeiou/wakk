package com.ubtrobot.sensor;

import com.ubtrobot.device.Device;

public class SensorDevice extends Device {

    private String type;

    private SensorDevice(Builder builder) {
        super(builder);

        this.type=builder.type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "SensorDevice{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", type='" + getType() + '\'' +
                ", description='" + getDescription() + '\'' +
                '}';
    }

    public static class Builder extends Device.GenericBuilder<Builder> {

        private String type;

        public Builder(String id, String name) {
            super(id, name);
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        @Override
        public SensorDevice build() {
            return new SensorDevice(this);
        }
    }
}
