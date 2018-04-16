package com.ubtrobot.motion.ipc.master;

import android.app.Application;
import android.os.Handler;

import com.google.protobuf.Message;
import com.ubtrobot.async.Promise;
import com.ubtrobot.master.adapter.CallProcessAdapter;
import com.ubtrobot.master.adapter.ProtoCallProcessAdapter;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.service.MasterSystemService;
import com.ubtrobot.motion.JointDevice;
import com.ubtrobot.motion.JointDeviceException;
import com.ubtrobot.motion.ipc.MotionConstants;
import com.ubtrobot.motion.ipc.MotionConverters;
import com.ubtrobot.motion.sal.AbstractMotionService;
import com.ubtrobot.motion.sal.MotionFactory;
import com.ubtrobot.motion.sal.MotionService;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;

import java.util.List;

public class MotionSystemService extends MasterSystemService {

    private MotionService mService;
    private ProtoCallProcessAdapter mCallProcessor;

    @Override
    protected void onServiceCreate() {
        Application application = getApplication();
        if (!(application instanceof MotionFactory)) {
            throw new IllegalStateException(
                    "Your application should implement MotionFactory interface.");
        }

        mService = ((MotionFactory) application).createMotionService();
        if (mService == null || !(mService instanceof AbstractMotionService)) {
            throw new IllegalStateException("Your application 's createMotionService returns null" +
                    " or does not return a instance of createMotionService.");
        }

        mCallProcessor = new ProtoCallProcessAdapter(new Handler(getMainLooper()));
    }


    @Call(path = MotionConstants.CALL_PATH_GET_JOINT_LIST)
    public void onGetJointList(Request request, Responder responder) {
        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<List<JointDevice>, JointDeviceException, Void>() {
                    @Override
                    public Promise<List<JointDevice>, JointDeviceException, Void>
                    call() throws CallException {
                        return mService.getJointList();
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<List<JointDevice>, JointDeviceException>() {
                    @Override
                    public Message convertDone(List<JointDevice> jointDevices) {
                        return MotionConverters.toJointDeviceListProto(jointDevices);
                    }

                    @Override
                    public CallException convertFail(JointDeviceException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }
}