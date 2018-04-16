package com.ubtrobot.light;

import com.ubtrobot.device.Device;

public class LightDevice extends Device {

    private LightDevice(Builder builder) {
        super(builder);
    }

    @Override
    public String toString() {
        return "LightDevice{" +
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
        public LightDevice build() {
            return new LightDevice(this);
        }
    }
}