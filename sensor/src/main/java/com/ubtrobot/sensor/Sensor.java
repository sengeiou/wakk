package com.ubtrobot.sensor;

import android.os.Handler;
import android.os.Looper;

import com.google.protobuf.BoolValue;
import com.google.protobuf.StringValue;
import com.ubtrobot.async.Consumer;
import com.ubtrobot.async.ListenerList;
import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.adapter.ProtoEventReceiver;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.sensor.ipc.SensorConstants;
import com.ubtrobot.sensor.ipc.SensorConverters;
import com.ubtrobot.sensor.ipc.SensorProto;
import com.ubtrobot.transport.message.CallException;

public class Sensor {

    private final MasterContext mMasterContext;
    private final ProtoCallAdapter mSensorService;

    private final SensorDevice mDevice;

    private final ListenerList<SensorListener> mListenerList;
    private final SensorEventReceiver mSensorEventReceiver;

    Sensor(MasterContext masterContext, ProtoCallAdapter sensorService, SensorDevice device) {
        mMasterContext = masterContext;
        mSensorService = sensorService;

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

    public Promise<Boolean, SensorException> enable() {
        return enableOrDisableSensor(SensorConstants.CALL_PATH_ENABLE_SENSOR);
    }

    private Promise<Boolean, SensorException> enableOrDisableSensor(String path) {
        return mSensorService.call(
                path,
                StringValue.newBuilder().setValue(getId()).build(),
                new ProtoCallAdapter.DFProtoConverter<Boolean, BoolValue, SensorException>() {
                    @Override
                    public Class<BoolValue> doneProtoClass() {
                        return BoolValue.class;
                    }

                    @Override
                    public Boolean convertDone(BoolValue ret) throws Exception {
                        return ret.getValue();
                    }

                    @Override
                    public SensorException convertFail(CallException e) {
                        return new SensorException.Factory().from(e);
                    }
                }
        );
    }

    public Promise<Boolean, AccessServiceException> isEnable() {
        return mSensorService.call(
                SensorConstants.CALL_PATH_QUERY_SENSOR_IS_ENABLE,
                StringValue.newBuilder().setValue(getId()).build(),
                new ProtoCallAdapter.DFProtoConverter<Boolean, BoolValue, AccessServiceException>() {
                    @Override
                    public Class<BoolValue> doneProtoClass() {
                        return BoolValue.class;
                    }

                    @Override
                    public Boolean convertDone(BoolValue enable) throws Exception {
                        return enable.getValue();
                    }

                    @Override
                    public AccessServiceException convertFail(CallException e) {
                        return new AccessServiceException.Factory().from(e);
                    }
                }
        );
    }

    public Promise<Boolean, SensorException> disable() {
        return enableOrDisableSensor(SensorConstants.CALL_PATH_DISABLE_SENSOR);
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
                final SensorEvent sensorEvent = SensorConverters.toSensorEventPojo(event);

                mListenerList.forEach(new Consumer<SensorListener>() {
                    @Override
                    public void accept(SensorListener listener) {
                        listener.onSensorChanged(Sensor.this, sensorEvent);
                    }
                });
            }
        }
    }
}