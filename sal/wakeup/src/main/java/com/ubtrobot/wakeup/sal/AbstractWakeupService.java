package com.ubtrobot.wakeup.sal;

import android.os.Handler;
import android.os.Looper;

import com.ubtrobot.async.Consumer;
import com.ubtrobot.async.ListenerList;
import com.ubtrobot.wakeup.WakeupEvent;
import com.ubtrobot.wakeup.WakeupListener;

public class AbstractWakeupService implements WakeupService {

    private ListenerList<WakeupListener> mWakeupListeners;

    public AbstractWakeupService() {
        mWakeupListeners = new ListenerList<>(new Handler(Looper.getMainLooper()));
    }

    @Override
    public void registerWakeupListener(WakeupListener listener) {
        mWakeupListeners.register(listener);
    }

    @Override
    public void unregisterWakeupListener(WakeupListener listener) {
        mWakeupListeners.unregister(listener);
    }

    protected void notifyWakeup(final WakeupEvent event) {
        if (null == event) {
            throw new IllegalArgumentException("Event must not be null");
        }

        mWakeupListeners.forEach(new Consumer<WakeupListener>() {
            @Override
            public void accept(WakeupListener listener) {
                listener.onWakeup(event);
            }
        });
    }

}
