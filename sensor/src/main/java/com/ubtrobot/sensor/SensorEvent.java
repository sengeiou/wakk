package com.ubtrobot.sensor;

import java.util.Arrays;

public class SensorEvent {

    private String sensorId;
    private long timestamp;
    private float[] values;

    private SensorEvent(String sensorId) {
        this.sensorId = sensorId;
    }

    public String getSensorId() {
        return sensorId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public float[] getValues() {
        return values;
    }

    @Override
    public String toString() {
        return "SensorEvent{" +
                "sensorId='" + sensorId + '\'' +
                ", timestamp=" + timestamp +
                ", values=" + Arrays.toString(values) +
                '}';
    }

    public static class Builder {

        private String sensorId;
        private long timestamp;
        private float[] values;

        public Builder(String sensorId) {
            this.sensorId = sensorId;
        }

        public Builder setTimestamp(long timestamp) {
            if (timestamp < 0) {
                throw new IllegalArgumentException("Argument timestamp < 0.");
            }

            this.timestamp = timestamp;
            return this;
        }

        public Builder setValues(float[] values) {
            this.values = values;
            return this;
        }

        public SensorEvent build() {
            SensorEvent event = new SensorEvent(sensorId == null ? "" : sensorId);
            event.timestamp = timestamp <= 0 ? System.currentTimeMillis() : timestamp;
            event.values = values == null ? new float[0] : values;
            return event;
        }
    }
}