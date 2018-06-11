package com.ubtrobot.power;

import android.os.Handler;
import android.os.Looper;

import com.google.protobuf.BoolValue;
import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.adapter.CallAdapter;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.power.ipc.PowerConstants;
import com.ubtrobot.power.ipc.PowerConverters;
import com.ubtrobot.transport.message.CallException;

public class PowerManager {

    private ProtoCallAdapter mService;

    public PowerManager(MasterContext masterContext) {
        mService = new ProtoCallAdapter(masterContext.createSystemServiceProxy(
                PowerConstants.SERVICE_NAME), new Handler(Looper.getMainLooper()));
    }

    public Promise<Boolean, AccessServiceException> sleep() {
        return mService.call(
                PowerConstants.CALL_PATH_SLEEP,
                new ProtoCallAdapter.DFProtoConverter<Boolean, BoolValue, AccessServiceException>() {
                    @Override
                    public Class<BoolValue> doneProtoClass() {
                        return BoolValue.class;
                    }

                    @Override
                    public Boolean convertDone(BoolValue notSleeping) throws Exception {
                        return notSleeping.getValue();
                    }

                    @Override
                    public AccessServiceException convertFail(CallException e) {
                        return new AccessServiceException.Factory().from(e);
                    }
                }
        );
    }

    public Promise<Boolean, AccessServiceException> isSleeping() {
        return mService.call(
                PowerConstants.CALL_PATH_QUERY_IS_SLEEPING,
                new ProtoCallAdapter.DFProtoConverter<Boolean, BoolValue, AccessServiceException>() {
                    @Override
                    public Class<BoolValue> doneProtoClass() {
                        return BoolValue.class;
                    }

                    @Override
                    public Boolean convertDone(BoolValue sleeping) throws Exception {
                        return sleeping.getValue();
                    }

                    @Override
                    public AccessServiceException convertFail(CallException e) {
                        return new AccessServiceException.Factory().from(e);
                    }
                }
        );
    }

    public Promise<Boolean, AccessServiceException> wakeUp() {
        return mService.call(
                PowerConstants.CALL_PATH_WAKE_UP,
                new ProtoCallAdapter.DFProtoConverter<Boolean, BoolValue, AccessServiceException>() {
                    @Override
                    public Class<BoolValue> doneProtoClass() {
                        return BoolValue.class;
                    }

                    @Override
                    public Boolean convertDone(BoolValue notWakeUp) throws Exception {
                        return notWakeUp.getValue();
                    }

                    @Override
                    public AccessServiceException convertFail(CallException e) {
                        return new AccessServiceException.Factory().from(e);
                    }
                }
        );
    }

    public Promise<Void, AccessServiceException> shutdown() {
        return shutdown(ShutdownOption.DEFAULT);
    }

    public Promise<Void, AccessServiceException> shutdown(ShutdownOption option) {
        return mService.call(
                PowerConstants.CALL_PATH_SHUTDOWN,
                PowerConverters.toShutdownOptionProto(option),
                new CallAdapter.FConverter<AccessServiceException>() {
                    @Override
                    public AccessServiceException convertFail(CallException e) {
                        return new AccessServiceException.Factory().from(e);
                    }
                }
        );
    }
}
