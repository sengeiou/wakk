package com.ubtrobot.power.ipc.master;

import android.app.Application;
import android.os.Handler;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Message;
import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.adapter.CallProcessAdapter;
import com.ubtrobot.master.adapter.ProtoCallProcessAdapter;
import com.ubtrobot.master.adapter.ProtoParamParser;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.competition.CompetingCallDelegate;
import com.ubtrobot.master.competition.CompetingItemDetail;
import com.ubtrobot.master.competition.ProtoCompetingCallDelegate;
import com.ubtrobot.master.service.MasterSystemService;
import com.ubtrobot.power.BatteryProperties;
import com.ubtrobot.power.ChargeException;
import com.ubtrobot.power.ipc.PowerConstants;
import com.ubtrobot.power.ipc.PowerConverters;
import com.ubtrobot.power.ipc.PowerProto;
import com.ubtrobot.power.sal.AbstractPowerService;
import com.ubtrobot.power.sal.PowerFactory;
import com.ubtrobot.power.sal.PowerService;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;

import java.util.Collections;
import java.util.List;

public class PowerSystemService extends MasterSystemService {

    private PowerService mService;
    private ProtoCallProcessAdapter mCallProcessor;
    private ProtoCompetingCallDelegate mCompetingCallDelegate;

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
        mCompetingCallDelegate = new ProtoCompetingCallDelegate(this, handler);
    }

    @Override
    protected List<CompetingItemDetail> getCompetingItems() {
        return Collections.singletonList(new CompetingItemDetail.Builder(getName(),
                PowerConstants.COMPETING_ITEM_CHARGING_STATTION_CONNECTION)
                .addCallPath(PowerConstants.CALL_PATH_CONNECT_TO_CHARGING_STATION)
                .addCallPath(PowerConstants.CALL_PATH_DISCONNECT_FROM_CHARGING_STATION)
                .setDescription("Competing item for charging stations connection.").build());
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
        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<Void, AccessServiceException>() {
                    @Override
                    public Promise<Void, AccessServiceException> call() throws CallException {
                        return mService.shutdown();
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

    @Call(path = PowerConstants.CALL_PATH_SCHEDULE_STARTUP)
    public void onScheduleStartup(Request request, Responder responder) {
        final Int32Value waitSeconds = ProtoParamParser.parseParam(request, Int32Value.class, responder);
        if (waitSeconds == null) {
            return;
        }

        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<Void, AccessServiceException>() {
                    @Override
                    public Promise<Void, AccessServiceException> call() throws CallException {
                        return mService.scheduleStartup(waitSeconds.getValue());
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

    @Call(path = PowerConstants.CALL_PATH_CANCEL_STARTUP)
    public void onCancelStartupSchedule(Request request, Responder responder) {
        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<Boolean, AccessServiceException>() {
                    @Override
                    public Promise<Boolean, AccessServiceException> call() throws CallException {
                        return mService.cancelStartupSchedule();
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<Boolean, AccessServiceException>() {
                    @Override
                    public Message convertDone(Boolean taskScheduled) {
                        return BoolValue.newBuilder().setValue(taskScheduled).build();
                    }

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

    @Call(path = PowerConstants.CALL_PATH_CONNECT_TO_CHARGING_STATION)
    public void onConnectToChargingStation(Request request, Responder responder) {
        final PowerProto.ConnectOption connectOption;
        if ((connectOption = ProtoParamParser.parseParam(
                request, PowerProto.ConnectOption.class, responder)) == null) {
            return;
        }

        mCompetingCallDelegate.onCall(
                request,
                PowerConstants.COMPETING_ITEM_CHARGING_STATTION_CONNECTION,
                responder,
                new CompetingCallDelegate.SessionCallable<Boolean, ChargeException>() {
                    @Override
                    public Promise<Boolean, ChargeException> call() throws CallException {
                        return mService.connectToChargingStation(
                                PowerConverters.toConnectOptionPojo(connectOption));
                    }
                },
                new ProtoCompetingCallDelegate.DFConverter<Boolean, ChargeException>() {
                    @Override
                    public Message convertDone(Boolean disconnectedPrevious) {
                        return BoolValue.newBuilder().setValue(disconnectedPrevious).build();
                    }

                    @Override
                    public CallException convertFail(ChargeException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Call(path = PowerConstants.CALL_PATH_QUERY_CONNECTED_TO_CHARGING_STATTION)
    public void onQueryConnectedToChargingStation(Request request, Responder responder) {
        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<Boolean, AccessServiceException>() {
                    @Override
                    public Promise<Boolean, AccessServiceException> call() throws CallException {
                        return mService.isConnectedToChargingStation();
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<Boolean, AccessServiceException>() {
                    @Override
                    public Message convertDone(Boolean connected) {
                        return BoolValue.newBuilder().setValue(connected).build();
                    }

                    @Override
                    public CallException convertFail(AccessServiceException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Call(path = PowerConstants.CALL_PATH_DISCONNECT_FROM_CHARGING_STATION)
    public void onDisconnectFromChargingStation(Request request, Responder responder) {
        mCompetingCallDelegate.onCall(
                request,
                PowerConstants.COMPETING_ITEM_CHARGING_STATTION_CONNECTION,
                responder,
                new CompetingCallDelegate.SessionCallable<Boolean, ChargeException>() {
                    @Override
                    public Promise<Boolean, ChargeException> call() throws CallException {
                        return mService.disconnectFromChargingStation();
                    }
                },
                new ProtoCompetingCallDelegate.DFConverter<Boolean, ChargeException>() {
                    @Override
                    public Message convertDone(Boolean connectedPrevious) {
                        return BoolValue.newBuilder().setValue(connectedPrevious).build();
                    }

                    @Override
                    public CallException convertFail(ChargeException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }
}
