package com.ubtrobot.master.adapter;

import com.google.protobuf.Message;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.master.event.EventReceiver;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.transport.message.Event;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

public abstract class ProtoEventReceiver<M extends Message> implements EventReceiver {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("ProtoEventReceiver");

    @Override
    public void onReceive(MasterContext masterContext, Event event) {
        if (event.getParam().isEmpty()) {
            onReceive(masterContext, event.getAction(), null);
            return;
        }

        try {
            onReceive(masterContext, event.getAction(),
                    ProtoParam.from(event.getParam(), protoClass()).getProtoMessage());
        } catch (ProtoParam.InvalidProtoParamException e) {
            LOGGER.e(e, "Framework error when parse event param. event=%s", event);
        }
    }

    protected abstract Class<M> protoClass();

    public abstract void onReceive(MasterContext masterContext, String action, M param);
}
