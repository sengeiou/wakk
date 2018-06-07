package com.ubtrobot.sensor.ipc.master;

import android.app.Application;
import android.os.Handler;

import com.google.protobuf.Message;
import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.adapter.CallProcessAdapter;
import com.ubtrobot.master.adapter.ProtoCallProcessAdapter;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.service.MasterSystemService;
import com.ubtrobot.sensor.SensorDevice;
import com.ubtrobot.sensor.ipc.SensorConstants;
import com.ubtrobot.sensor.ipc.SensorConverters;
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
}
