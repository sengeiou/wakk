package com.ubtrobot.sensor.ipc.master;

import android.app.Application;
import android.os.Handler;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Message;
import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.adapter.CallProcessAdapter;
import com.ubtrobot.master.adapter.ProtoCallProcessAdapter;
import com.ubtrobot.master.adapter.ProtoParamParser;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.service.MasterSystemService;
import com.ubtrobot.sensor.SensorDevice;
import com.ubtrobot.sensor.SensorException;
import com.ubtrobot.sensor.ipc.SensorConstants;
import com.ubtrobot.sensor.ipc.SensorConverters;
import com.ubtrobot.sensor.ipc.SensorProto;
import com.ubtrobot.sensor.sal.AbstractSensorService;
import com.ubtrobot.sensor.sal.SensorFactory;
import com.ubtrobot.sensor.sal.SensorService;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;

import java.util.List;

public class SensorSystemService extends MasterSystemService {

    private SensorService mService;
    private ProtoCallProcessAdapter mCallProcessor;

    @Override
    protected void onServiceCreate() {
        Application application = getApplication();
        if (!(application instanceof SensorFactory)) {
            throw new IllegalStateException(
                    "Your application should implement SensorFactory interface.");
        }

        mService = ((SensorFactory) application).createSensorService();
        if (mService == null || !(mService instanceof AbstractSensorService)) {
            throw new IllegalStateException("Your application 's createSensorService returns null" +
                    " or does not return a instance of AbstractSensorService.");
        }

        Handler handler = new Handler(getMainLooper());
        mCallProcessor = new ProtoCallProcessAdapter(handler);
    }

    @Call(path = SensorConstants.CALL_PATH_GET_SENSOR_LIST)
    public void onGetSensorList(Request request, Responder responder) {
        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<List<SensorDevice>, AccessServiceException>() {
                    @Override
                    public Promise<List<SensorDevice>, AccessServiceException>
                    call() throws CallException {
                        return mService.getSensorList();
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<List<SensorDevice>, AccessServiceException>() {
                    @Override
                    public Message convertDone(List<SensorDevice> devices) {
                        return SensorConverters.toSensorDeviceListProto(devices);
                    }

                    @Override
                    public CallException convertFail(AccessServiceException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Call(path = SensorConstants.CALL_PATH_ENABLE_SENSOR)
    public void onEnableSensor(Request request, Responder responder) {
        final String sensorId = ProtoParamParser.parseStringParam(request, responder);
        if (sensorId == null) {
            return;
        }

        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<Boolean, SensorException>() {
                    @Override
                    public Promise<Boolean, SensorException> call() throws CallException {
                        return mService.enableSensor(sensorId);
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<Boolean, SensorException>() {
                    @Override
                    public Message convertDone(Boolean disablePrevious) {
                        return BoolValue.newBuilder().setValue(disablePrevious).build();
                    }

                    @Override
                    public CallException convertFail(SensorException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Call(path = SensorConstants.CALL_PATH_QUERY_SENSOR_IS_ENABLE)
    public void onGetSensorIsEnable(Request request, Responder responder) {
        final String sensorId = ProtoParamParser.parseStringParam(request, responder);
        if (sensorId == null) {
            return;
        }

        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<Boolean, AccessServiceException>() {
                    @Override
                    public Promise<Boolean, AccessServiceException> call() throws CallException {
                        return mService.isSensorEnable(sensorId);
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<Boolean, AccessServiceException>() {
                    @Override
                    public Message convertDone(Boolean enable) {
                        return BoolValue.newBuilder().setValue(enable).build();
                    }

                    @Override
                    public CallException convertFail(AccessServiceException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Call(path = SensorConstants.CALL_PATH_CONTROL_SENSOR)
    public void onControlSensor(Request request, Responder responder) {
        final SensorProto.ControlOptions options = ProtoParamParser.parseParam(request,
                SensorProto.ControlOptions.class, responder);
        if (options == null) {
            return;
        }

        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<Void, SensorException>() {
                    @Override
                    public Promise<Void, SensorException> call() throws CallException {
                        return mService.control(options.getSensorId(), options.getCommand(),
                                options.getOptionMap());
                    }
                },
                new ProtoCallProcessAdapter.FConverter<SensorException>() {
                    @Override
                    public CallException convertFail(SensorException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Call(path = SensorConstants.CALL_PATH_DISABLE_SENSOR)
    public void onDisableSensor(Request request, Responder responder) {
        final String sensorId = ProtoParamParser.parseStringParam(request, responder);
        if (sensorId == null) {
            return;
        }

        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<Boolean, SensorException>() {
                    @Override
                    public Promise<Boolean, SensorException> call() throws CallException {
                        return mService.disableSensor(sensorId);
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<Boolean, SensorException>() {
                    @Override
                    public Message convertDone(Boolean enablePrevious) {
                        return BoolValue.newBuilder().setValue(enablePrevious).build();
                    }

                    @Override
                    public CallException convertFail(SensorException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }
}
