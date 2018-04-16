package com.ubtrobot.motion;

import com.ubtrobot.device.Device;

public class JointDevice extends Device {

    private float minAngle;
    private float maxAngle;
    private float minSpeed;
    private float maxSpeed;
    private float defaultSpeed;

    private JointDevice(Builder builder) {
        super(builder);
    }

    public float getMinAngle() {
        return minAngle;
    }

    public float getMaxAngle() {
        return maxAngle;
    }

    public float getMinSpeed() {
        return minSpeed;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public float getDefaultSpeed() {
        return defaultSpeed;
    }

    @Override
    public String toString() {
        return "JointDevice{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", minAngle='" + minAngle + '\'' +
                ", maxAngle='" + maxAngle + '\'' +
                ", minSpeed='" + minSpeed + '\'' +
                ", maxSpeed='" + maxSpeed + '\'' +
                ", defaultSpeed='" + defaultSpeed + '\'' +
                '}';
    }

    public static class Builder extends Device.GenericBuilder<Builder> {

        private float minAngle;
        private float maxAngle;
        private float minSpeed;
        private float maxSpeed;
        private float defaultSpeed;

        public Builder(String id, String name) {
            super(id, name);
        }

        public Builder setMinAngle(float minAngle) {
            this.minAngle = minAngle;
            return this;
        }

        public Builder setMaxAngle(float maxAngle) {
            this.maxAngle = maxAngle;
            return this;
        }

        public Builder setMinSpeed(float minSpeed) {
            this.minSpeed = minSpeed;
            return this;
        }

        public Builder setMaxSpeed(float maxSpeed) {
            this.maxSpeed = maxSpeed;
            return this;
        }

        public Builder setDefaultSpeed(float defaultSpeed) {
            this.defaultSpeed = defaultSpeed;
            return this;
        }

        @Override
        public JointDevice build() {
            JointDevice jointDevice = new JointDevice(this);
            jointDevice.minAngle = minAngle;
            jointDevice.maxAngle = maxAngle;
            jointDevice.minSpeed = minSpeed;
            jointDevice.maxSpeed = maxSpeed;
            jointDevice.defaultSpeed = defaultSpeed;

            return jointDevice;
        }
    }
}