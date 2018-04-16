package com.ubtrobot.light.ipc.master;

import android.app.Application;
import android.os.Handler;

import com.google.protobuf.Message;
import com.ubtrobot.async.Promise;
import com.ubtrobot.light.LightDevice;
import com.ubtrobot.light.LightException;
import com.ubtrobot.light.ipc.LightConstants;
import com.ubtrobot.light.ipc.LightConverters;
import com.ubtrobot.light.sal.AbstractLightService;
import com.ubtrobot.light.sal.LightFactory;
import com.ubtrobot.light.sal.LightService;
import com.ubtrobot.master.adapter.CallProcessAdapter;
import com.ubtrobot.master.adapter.ProtoCallProcessAdapter;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.service.MasterSystemService;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;

import java.util.List;

public class LightSystemService extends MasterSystemService {

    private LightService mService;
    private ProtoCallProcessAdapter mCallProcessor;

    @Override
    protected void onServiceCreate() {
        Application application = getApplication();
        if (!(application instanceof LightFactory)) {
            throw new IllegalStateException(
                    "Your application should implement LightFactory interface.");
        }

        mService = ((LightFactory) application).createLightService();
        if (mService == null || !(mService instanceof AbstractLightService)) {
            throw new IllegalStateException("Your application 's createLightService returns null" +
                    " or does not return a instance of AbstractLightService.");
        }

        Handler handler = new Handler(getMainLooper());
        mCallProcessor = new ProtoCallProcessAdapter(handler);
    }

    @Call(path = LightConstants.CALL_PATH_GET_LIGHT_LIST)
    public void onGetLightList(Request request, Responder responder) {
        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<List<LightDevice>, LightException, Void>() {
                    @Override
                    public Promise<List<LightDevice>, LightException, Void>
                    call() throws CallException {
                        return mService.getLightList();
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<List<LightDevice>, LightException>() {
                    @Override
                    public Message convertDone(List<LightDevice> deviceList) {
                        return LightConverters.toLightDeviceListProto(deviceList);
                    }

                    @Override
                    public CallException convertFail(LightException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }
}
