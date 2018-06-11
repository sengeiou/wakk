package com.ubtrobot.sensor;

import android.os.Handler;
import android.os.Looper;

import com.ubtrobot.async.Consumer;
import com.ubtrobot.async.ListenerList;
import com.ubtrobot.master.adapter.ProtoEventReceiver;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.sensor.ipc.SensorConstants;
import com.ubtrobot.sensor.ipc.SensorConverters;
import com.ubtrobot.sensor.ipc.SensorProto;

public class Sensor {

    private final MasterContext mMasterContext;
    private final SensorDevice mDevice;

    private final ListenerList<SensorListener> mListenerList;
    private final SensorEventReceiver mSensorEventReceiver;

    Sensor(MasterContext masterContext, SensorDevice device) {
        mMasterContext = masterContext;
        mDevice = device;

        mListenerList = new ListenerList<>(new Handler(Looper.getMainLooper()));
        mSensorEventReceiver = new SensorEventReceiver();
    }

    public String getId() {
        return mDevice.getId();
    }

    public SensorDevice getDevice() {
        return mDevice;
    }

    public void registerSensorListener(SensorListener listener) {
        synchronized (mListenerList) {
            boolean subscribed = !mListenerList.isEmpty();
            mListenerList.register(listener);

            if (!subscribed) {
                mMasterContext.subscribe(mSensorEventReceiver,
                        SensorConstants.ACTION_SENSOR_CHANGE + getId());
            }
        }
    }

    public void unregisterSensorListener(SensorListener listener) {
        synchronized (mListenerList) {
            mListenerList.unregister(listener);

            if (mListenerList.isEmpty()) {
                mMasterContext.unsubscribe(mSensorEventReceiver);
            }
        }
    }

    private class SensorEventReceiver extends ProtoEventReceiver<SensorProto.SensorEvent> {

        @Override
        protected Class<SensorProto.SensorEvent> protoClass() {
            return SensorProto.SensorEvent.class;
        }

        @Override
        public void
        onReceive(MasterContext context, String action, final SensorProto.SensorEvent event) {
            synchronized (mListenerList) {
                mListenerList.forEach(new Consumer<SensorListener>() {
                    @Override
                    public void accept(SensorListener listener) {
                        listener.onSensorChanged(Sensor.this,
                                SensorConverters.toSensorEventPojo(event));
                    }
                });
            }
        }
    }
}