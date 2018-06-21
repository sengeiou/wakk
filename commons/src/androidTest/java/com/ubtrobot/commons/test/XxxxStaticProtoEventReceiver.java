package com.ubtrobot.commons.test;

import com.google.protobuf.Message;
import com.ubtrobot.master.adapter.StaticProtoEventReceiver;
import com.ubtrobot.master.context.MasterContext;

/**
 * Created by cxdan on 2018/5/2.
 */

public class XxxxStaticProtoEventReceiver<E, T extends Message> extends StaticProtoEventReceiver<T> {

    protected XxxxStaticProtoEventReceiver() {
       // getClass().equals(XxxxStaticProtoEventReceiver.class)
    }


    @Override
    public void onReceive(MasterContext masterContext, String action, T param) {

    }

    @Override
    protected Class<T> protoClass() {
        return null;
    }
}
