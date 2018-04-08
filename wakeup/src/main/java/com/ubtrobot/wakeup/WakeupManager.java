package com.ubtrobot.wakeup;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.view.menu.MenuWrapperFactory;

import com.google.protobuf.Message;
import com.ubtrobot.async.Consumer;
import com.ubtrobot.async.ListenerList;
import com.ubtrobot.master.adapter.ProtoEventReceiver;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.master.event.EventReceiver;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.transport.message.Event;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;
import com.ubtrobot.wakeup.ipc.WakeupConstants;
import com.ubtrobot.wakeup.ipc.WakeupConverter;
import com.ubtrobot.wakeup.ipc.WakeupProto;

public class WakeupManager {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("WakeupManager");

    private final MasterContext mMasterContext;

    private final ListenerList<WakeupListener> mListener;

    private boolean mHasSubscribed = false;
    private final byte[] mSubscribeLock = new byte[0];

    public WakeupManager(MasterContext mMasterContext) {
        this.mMasterContext = mMasterContext;
        mListener = new ListenerList<>(new Handler(Looper.getMainLooper()));
    }

    public void registerWakeupListener(WakeupListener listener) {
        synchronized (mSubscribeLock) {
            if (!mHasSubscribed) {
                mHasSubscribed = true;
                subscribeEventReceiver();
            }
        }

        mListener.register(listener);
    }


    public void unregisterWakeupListener(WakeupListener listener) {
        mListener.unregister(listener);

        synchronized (mSubscribeLock) {
            if (mListener.isEmpty() && mHasSubscribed) {
                mHasSubscribed = false;
                unSubscribeEventReceiver();
            }
        }
    }

    private void subscribeEventReceiver() {
        mMasterContext.subscribe(mEventReceiver, WakeupConstants.ACTION_WAKEUP);
    }

    private void unSubscribeEventReceiver() {
        mMasterContext.unsubscribe(mEventReceiver);

    }

    private final ProtoEventReceiver<WakeupProto.WakeupEvent> mEventReceiver = new ProtoEventReceiver<WakeupProto.WakeupEvent>() {

        @Override
        public void onReceive(MasterContext masterContext, String action, WakeupProto.WakeupEvent event) {
            WakeupEvent wakeupEvent = WakeupConverter.toWakeUpEventPojo(event);
            notifyListener(wakeupEvent);
        }
    };

    private void notifyListener(final WakeupEvent event) {
        mListener.forEach(new Consumer<WakeupListener>() {
            @Override
            public void accept(WakeupListener listener) {
                listener.onWakeup(event);
            }
        });
    }
}
