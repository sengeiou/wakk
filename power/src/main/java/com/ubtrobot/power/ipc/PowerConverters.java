package com.ubtrobot.power.ipc;

import com.ubtrobot.power.BatteryProperties;
import com.ubtrobot.power.ConnectOption;
import com.ubtrobot.power.ShutdownOption;

public class PowerConverters {

    private PowerConverters() {
    }

    public static PowerProto.ShutdownOption toShutdownOptionProto(ShutdownOption option) {
        return PowerProto.ShutdownOption.newBuilder()
                .setWaitSecondsToStartup(option.getWaitSecondsToStartup()).build();
    }

    public static ShutdownOption toShutdownOptionPojo(PowerProto.ShutdownOption option) {
        return new ShutdownOption.Builder()
                .setWaitSecondsToStartup(option.getWaitSecondsToStartup()).build();
    }

    public static BatteryProperties
    toBatteryPropertiesPojo(PowerProto.BatteryProperties properties) {
        return new BatteryProperties.Builder().setLevel(properties.getLevel())
                .setChargerAcOnline(properties.getChargerAcOnline())
                .setChargingStationOnline(properties.getChargingStationOnline())
                .setChargingVoltage(properties.getChargingVoltage())
                .setFull(properties.getFull())
                .setTemperature(properties.getTemperature())
                .build();
    }

    public static PowerProto.BatteryProperties toBatteryPropertiesProto(BatteryProperties properties) {
        return PowerProto.BatteryProperties.newBuilder().setLevel(properties.getLevel())
                .setChargerAcOnline(properties.isChargerAcOnline())
                .setChargingStationOnline(properties.isChargingStationOnline())
                .setChargingVoltage(properties.getChargingVoltage())
                .setFull(properties.isFull())
                .setTemperature(properties.getTemperature())
                .build();
    }

    public static PowerProto.ConnectOption toConnectOptionProto(ConnectOption option) {
        return PowerProto.ConnectOption.newBuilder().setTimeout(option.getTimeout()).build();
    }

    public static ConnectOption toConnectOptionPojo(PowerProto.ConnectOption option) {
        return new ConnectOption.Builder().setTimeout(option.getTimeout()).build();
    }
}