package com.ubtrobot.wakeup.ipc;

import com.ubtrobot.wakeup.WakeupEvent;


public class WakeupConverter {

    public static WakeupProto.WakeupEvent toWakeUpEventProto(WakeupEvent event) {
        return WakeupProto.WakeupEvent.newBuilder()
                .setType(event.getType())
                .setAngle(event.getAngle())
                .build();
    }

    public static WakeupEvent toWakeUpEventPojo(WakeupProto.WakeupEvent event) {
        return new WakeupEvent.Builder(event.getType())
                .setAngle(event.getAngle())
                .setDistance(event.getDistacne())
                .build();
    }
}
