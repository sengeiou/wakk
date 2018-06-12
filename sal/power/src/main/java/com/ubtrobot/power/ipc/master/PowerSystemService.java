package com.ubtrobot.power.ipc.master;

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
import com.ubtrobot.power.BatteryProperties;
import com.ubtrobot.power.ipc.PowerConstants;
import com.ubtrobot.power.ipc.PowerConverters;
import com.ubtrobot.power.ipc.PowerProto;
import com.ubtrobot.power.sal.AbstractPowerService;
import com.ubtrobot.power.sal.PowerFactory;
import com.ubtrobot.power.sal.PowerService;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;

public class PowerSystemService extends MasterSystemService {

    private PowerService mService;
    private ProtoCallProcessAdapter mCallProcessor;

    @Override
    protected void onServiceCreate() {
        Application application = getApplication();
        if (!(application instanceof PowerFactory)) {
            throw new IllegalStateException(
                    "Your application should implement PowerFactory interface.");
        }

        mService = ((PowerFactory) application).createPowerService();
        if (mService == null || !(mService instanceof AbstractPowerService)) {
            throw new IllegalStateException("Your application 's createPowerService returns null" +
                    " or does not return a instance of AbstractPowerService.");
        }

        Handler handler = new Handler(getMainLooper());
        mCallProcessor = new ProtoCallProcessAdapter(handler);
    }

    @Call(path = PowerConstants.CALL_PATH_SLEEP)
    public void onSleep(Request request, Responder responder) {
        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<Boolean, AccessServiceException>() {
                    @Override
                    public Promise<Boolean, AccessServiceException> call() throws CallException {
                        return mService.sleep();
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<Boolean, AccessServiceException>() {
                    @Override
                    public Message convertDone(Boolean notSleeping) {
                        return BoolValue.newBuilder().setValue(notSleeping).build();
                    }

                    @Override
                    public CallException convertFail(AccessServiceException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Call(path = PowerConstants.CALL_PATH_QUERY_IS_SLEEPING)
    public void onQueryIsSleeping(Request request, Responder responder) {
        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<Boolean, AccessServiceException>() {
                    @Override
                    public Promise<Boolean, AccessServiceException> call() throws CallException {
                        return mService.isSleeping();
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<Boolean, AccessServiceException>() {
                    @Override
                    public Message convertDone(Boolean sleeping) {
                        return BoolValue.newBuilder().setValue(sleeping).build();
                    }

                    @Override
                    public CallException convertFail(AccessServiceException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Call(path = PowerConstants.CALL_PATH_WAKE_UP)
    public void onWakeUp(Request request, Responder responder) {
        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<Boolean, AccessServiceException>() {
                    @Override
                    public Promise<Boolean, AccessServiceException> call() throws CallException {
                        return mService.wakeUp();
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<Boolean, AccessServiceException>() {
                    @Override
                    public Message convertDone(Boolean sleeping) {
                        return BoolValue.newBuilder().setValue(sleeping).build();
                    }

                    @Override
                    public CallException convertFail(AccessServiceException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Call(path = PowerConstants.CALL_PATH_SHUTDOWN)
    public void onShutdown(Request request, Responder responder) {
        final PowerProto.ShutdownOption shutdownOption;
        if ((shutdownOption = ProtoParamParser.parseParam(
                request, PowerProto.ShutdownOption.class, responder)) == null) {
            return;
        }

        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<Void, AccessServiceException>() {
                    @Override
                    public Promise<Void, AccessServiceException> call() throws CallException {
                        return mService.shutdown(PowerConverters.toShutdownOptionPojo(shutdownOption));
                    }
                },
                new ProtoCallProcessAdapter.FConverter<AccessServiceException>() {
                    @Override
                    public CallException convertFail(AccessServiceException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Call(path = PowerConstants.CALL_PATH_GET_BATTERY_PROPERTIES)
    public void onGetBatteryProperties(Request request, Responder responder) {
        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<BatteryProperties, AccessServiceException>() {

                    @Override
                    public Promise<BatteryProperties, AccessServiceException>
                    call() throws CallException {
                        return mService.getBatteryProperties();
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<BatteryProperties, AccessServiceException>() {
                    @Override
                    public Message convertDone(BatteryProperties properties) {
                        return PowerConverters.toBatteryPropertiesProto(properties);
                    }

                    @Override
                    public CallException convertFail(AccessServiceException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }
}
