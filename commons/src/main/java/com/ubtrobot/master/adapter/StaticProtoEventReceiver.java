package com.ubtrobot.master.adapter;

import com.google.protobuf.Message;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.master.event.EventReceiver;
import com.ubtrobot.master.event.StaticEventReceiver;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.transport.message.Event;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.lang.reflect.ParameterizedType;

public abstract class StaticProtoEventReceiver<M extends Message> extends StaticEventReceiver {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("StaticProtoEventReceiver");

    private final Class<M> mGenericClass;

    public StaticProtoEventReceiver() {
        mGenericClass = (Class) ((ParameterizedType) (getClass().getGenericSuperclass()))
                .getActualTypeArguments()[0];
    }

    @Override
    public final void onReceive(MasterContext masterContext, Event event) {
        if (event.getParam().isEmpty()) {
            onReceive(masterContext, event.getAction(), null);
            return;
        }

        try {
            onReceive(masterContext, event.getAction(),
                    ProtoParam.from(event.getParam(), mGenericClass).getProtoMessage());
        } catch (ProtoParam.InvalidProtoParamException e) {
            LOGGER.e(e, "Framework error when parse event param. event=%s", event);
        }
    }

    public abstract void onReceive(MasterContext masterContext, String action, M param);
}
