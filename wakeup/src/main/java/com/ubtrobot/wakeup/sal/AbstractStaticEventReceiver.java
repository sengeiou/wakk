package com.ubtrobot.wakeup.sal;

import com.ubtrobot.master.adapter.StaticProtoEventReceiver;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;
import com.ubtrobot.wakeup.WakeupEvent;
import com.ubtrobot.wakeup.ipc.WakeupConverter;
import com.ubtrobot.wakeup.ipc.WakeupProto;

public abstract class AbstractStaticEventReceiver extends StaticProtoEventReceiver<WakeupProto.WakeupEvent> {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("AbstractStaticEventReceiver");

    @Override
    public final void onReceive(MasterContext masterContext, String action, WakeupProto.WakeupEvent event) {
        onWakeup(WakeupConverter.toWakeUpEventPojo(event));
    }

    public abstract void onWakeup(WakeupEvent event);
}
