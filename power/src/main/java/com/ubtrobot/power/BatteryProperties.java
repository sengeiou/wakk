package com.ubtrobot.power;

public class BatteryProperties {

    private int level;
    private boolean chargerAcOnline;
    private boolean chargingStationOnline;
    private int chargingVoltage;
    private boolean full;
    private int temperature;

    private BatteryProperties() {
    }

    public int getLevel() {
        return level;
    }

    public boolean isChargerAcOnline() {
        return chargerAcOnline;
    }

    public boolean isChargingStationOnline() {
        return chargingStationOnline;
    }

    public int getChargingVoltage() {
        return chargingVoltage;
    }

    public boolean isFull() {
        return full;
    }

    public int getTemperature() {
        return temperature;
    }

    @Override
    public String toString() {
        return "BatteryProperties{" +
                "level=" + level +
                ", chargerAcOnline=" + chargerAcOnline +
                ", chargingStationOnline=" + chargingStationOnline +
                ", chargingVoltage=" + chargingVoltage +
                ", full=" + full +
                ", temperature=" + temperature +
                '}';
    }

    public static class Builder {

        private int level;
        private boolean chargerAcOnline;
        private boolean chargingStationOnline;
        private int chargingVoltage;
        private boolean full;
        private int temperature;

        public Builder() {
        }

        public Builder setLevel(int level) {
            if (level < 0) {
                throw new IllegalArgumentException("Argument level < 0.");
            }

            this.level = level;
            return this;
        }

        public Builder setChargerAcOnline(boolean chargerAcOnline) {
            this.chargerAcOnline = chargerAcOnline;
            return this;
        }

        public Builder setChargingStationOnline(boolean chargingStationOnline) {
            this.chargingStationOnline = chargingStationOnline;
            return this;
        }

        public Builder setChargingVoltage(int chargingVoltage) {
            this.chargingVoltage = chargingVoltage;
            return this;
        }

        public Builder setFull(boolean full) {
            this.full = full;
            return this;
        }

        public Builder setTemperature(int temperature) {
            this.temperature = temperature;
            return this;
        }

        public BatteryProperties build() {
            BatteryProperties properties = new BatteryProperties();
            properties.level = level;
            properties.chargerAcOnline = chargerAcOnline;
            properties.chargingStationOnline = chargingStationOnline;
            properties.chargingVoltage = chargingVoltage;
            properties.full = full;
            properties.temperature = temperature;
            return properties;
        }
    }
}
