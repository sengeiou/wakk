package com.ubtrobot.motion;

import com.ubtrobot.device.Device;

public class LocomotorDevice extends Device {

    private float minTurningSpeed;
    private float maxTurningSpeed;
    private float defaultTurningSpeed;
    private float minMovingSpeed;
    private float maxMovingSpeed;
    private float defaultMovingSpeed;

    private LocomotorDevice(Builder builder) {
        super(builder);
    }

    public float getMinTurningSpeed() {
        return minTurningSpeed;
    }

    public float getMaxTurningSpeed() {
        return maxTurningSpeed;
    }

    public float getDefaultTurningSpeed() {
        return defaultTurningSpeed;
    }

    public float getMinMovingSpeed() {
        return minMovingSpeed;
    }

    public float getMaxMovingSpeed() {
        return maxMovingSpeed;
    }

    public float getDefaultMovingSpeed() {
        return defaultMovingSpeed;
    }

    @Override
    public String toString() {
        return "LocomotorDevice{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", minTurningSpeed='" + minTurningSpeed + '\'' +
                ", maxTurningSpeed='" + maxTurningSpeed + '\'' +
                ", defaultTurningSpeed='" + defaultTurningSpeed + '\'' +
                ", minMovingSpeed='" + minMovingSpeed + '\'' +
                ", maxMovingSpeed='" + maxMovingSpeed + '\'' +
                ", defaultMovingSpeed='" + defaultMovingSpeed + '\'' +
                '}';
    }

    public static class Builder extends Device.GenericBuilder<Builder> {

        private float minTurningSpeed;
        private float maxTurningSpeed;
        private float defaultTurningSpeed;
        private float minMovingSpeed;
        private float maxMovingSpeed;
        private float defaultMovingSpeed;

        public Builder(String id, String name) {
            super(id, name);
        }

        public Builder setMinTurningSpeed(float minTurningSpeed) {
            this.minTurningSpeed = minTurningSpeed;
            return this;
        }

        public Builder setMaxTurningSpeed(float maxRotateSpeed) {
            this.maxTurningSpeed = maxRotateSpeed;
            return this;
        }

        public Builder setDefaultTurningSpeed(float defaultTurningSpeed) {
            this.defaultTurningSpeed = defaultTurningSpeed;
            return this;
        }

        public Builder setMinMovingSpeed(float minMovingSpeed) {
            this.minMovingSpeed = minMovingSpeed;
            return this;
        }

        public Builder setMaxMovingSpeed(float maxMovingSpeed) {
            this.maxMovingSpeed = maxMovingSpeed;
            return this;
        }

        public Builder setDefaultMovingSpeed(float defaultMovingSpeed) {
            this.defaultMovingSpeed = defaultMovingSpeed;
            return this;
        }

        @Override
        public LocomotorDevice build() {
            LocomotorDevice locomotorDevice = new LocomotorDevice(this);
            locomotorDevice.minTurningSpeed = minTurningSpeed;
            locomotorDevice.maxTurningSpeed = maxTurningSpeed;
            locomotorDevice.defaultTurningSpeed = defaultTurningSpeed;
            locomotorDevice.minMovingSpeed = minMovingSpeed;
            locomotorDevice.maxMovingSpeed = maxMovingSpeed;
            locomotorDevice.defaultMovingSpeed = defaultMovingSpeed;

            return locomotorDevice;
        }
    }
}