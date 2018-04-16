package com.ubtrobot.light.ipc.master;

import android.app.Application;
import android.os.Handler;
import android.text.TextUtils;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Message;
import com.ubtrobot.async.Promise;
import com.ubtrobot.light.LightDevice;
import com.ubtrobot.light.LightDeviceException;
import com.ubtrobot.light.LightException;
import com.ubtrobot.light.ipc.LightConstants;
import com.ubtrobot.light.ipc.LightConverters;
import com.ubtrobot.light.ipc.LightProto;
import com.ubtrobot.light.sal.AbstractLightService;
import com.ubtrobot.light.sal.LightFactory;
import com.ubtrobot.light.sal.LightService;
import com.ubtrobot.master.adapter.CallProcessAdapter;
import com.ubtrobot.master.adapter.ProtoCallProcessAdapter;
import com.ubtrobot.master.adapter.ProtoParamParser;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.competition.CompetingCallDelegate;
import com.ubtrobot.master.competition.ProtoCompetingCallDelegate;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.master.service.MasterSystemService;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;

import java.util.List;

public class LightSystemService extends MasterSystemService {

    private LightService mService;
    private ProtoCallProcessAdapter mCallProcessor;
    private ProtoCompetingCallDelegate mCompetingCallDelegate;

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
        mCompetingCallDelegate = new ProtoCompetingCallDelegate(this, handler);
    }

    @Call(path = LightConstants.CALL_PATH_GET_LIGHT_LIST)
    public void onGetLightList(Request request, Responder responder) {
        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<List<LightDevice>, LightDeviceException, Void>() {
                    @Override
                    public Promise<List<LightDevice>, LightDeviceException, Void>
                    call() throws CallException {
                        return mService.getLightList();
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<List<LightDevice>, LightDeviceException>() {
                    @Override
                    public Message convertDone(List<LightDevice> deviceList) {
                        return LightConverters.toLightDeviceListProto(deviceList);
                    }

                    @Override
                    public CallException convertFail(LightDeviceException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Call(path = LightConstants.CALL_PATH_TURN_ON)
    public void onLightTurnOn(Request request, Responder responder) {
        final LightProto.LightColor lightColor = ProtoParamParser.parseParam(request,
                LightProto.LightColor.class, responder);
        if (lightColor == null) {
            return;
        }

        mCompetingCallDelegate.onCall(
                request,
                LightConstants.COMPETING_ITEM_PREFIX_LIGHT + lightColor.getLightId(),
                responder,
                new CompetingCallDelegate.SessionCallable<Void, LightException, Void>() {
                    @Override
                    public Promise<Void, LightException, Void> call() throws CallException {
                        return mService.turnOn(lightColor.getLightId(), lightColor.getColor());
                    }
                },
                new ProtoCompetingCallDelegate.FConverter<LightException>() {
                    @Override
                    public CallException convertFail(LightException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Call(path = LightConstants.CALL_PATH_GET_IS_TURN_ON)
    public void onGetLightIsOn(Request request, Responder responder) {
        String lightId;
        if ((lightId = ProtoParamParser.parseStringParam(request, responder)) == null) {
            return;
        }

        responder.respondSuccess(ProtoParam.create(BoolValue.newBuilder().
                setValue(mService.isOn(lightId)).build()));
    }

    @Call(path = LightConstants.CALL_PATH_CHANGE_COLOR)
    public void onChangeLightColor(Request request, Responder responder) {
        final LightProto.LightColor lightColor = ProtoParamParser.parseParam(request,
                LightProto.LightColor.class, responder);
        if (lightColor == null) {
            return;
        }

        mCompetingCallDelegate.onCall(
                request,
                LightConstants.COMPETING_ITEM_PREFIX_LIGHT + lightColor.getLightId(),
                responder,
                new CompetingCallDelegate.SessionCallable<Void, LightException, Void>() {
                    @Override
                    public Promise<Void, LightException, Void> call() throws CallException {
                        return mService.changeColor(lightColor.getLightId(), lightColor.getColor());
                    }
                },
                new ProtoCompetingCallDelegate.FConverter<LightException>() {
                    @Override
                    public CallException convertFail(LightException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Call(path = LightConstants.CALL_PATH_GET_COLOR)
    public void onGetLightColor(Request request, Responder responder) {
        String lightId;
        if ((lightId = ProtoParamParser.parseStringParam(request, responder)) == null) {
            return;
        }

        responder.respondSuccess(ProtoParam.create(Int32Value.newBuilder().
                setValue(mService.getColor(lightId)).build()));
    }

    @Call(path = LightConstants.CALL_PATH_TURN_OFF)
    public void onLightTurnOff(Request request, Responder responder) {
        final String lightId = ProtoParamParser.parseStringParam(request, responder);
        if (TextUtils.isEmpty(lightId)) {
            return;
        }

        mCompetingCallDelegate.onCall(
                request,
                LightConstants.COMPETING_ITEM_PREFIX_LIGHT + lightId,
                responder,
                new CompetingCallDelegate.SessionCallable<Void, LightException, Void>() {
                    @Override
                    public Promise<Void, LightException, Void> call() throws CallException {
                        return mService.turnOff(lightId);
                    }
                },
                new ProtoCompetingCallDelegate.FConverter<LightException>() {
                    @Override
                    public CallException convertFail(LightException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }
}
