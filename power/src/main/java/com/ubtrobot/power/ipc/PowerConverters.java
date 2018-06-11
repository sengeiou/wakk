package com.ubtrobot.power.ipc;

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
}