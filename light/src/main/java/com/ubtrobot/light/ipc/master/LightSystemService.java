package com.ubtrobot.light.ipc.master;

import android.app.Application;
import android.os.Handler;
import android.text.TextUtils;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Message;
import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.light.DisplayException;
import com.ubtrobot.light.LightDevice;
import com.ubtrobot.light.LightException;
import com.ubtrobot.light.LightingEffect;
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
import com.ubtrobot.master.competition.CompetingItemDetail;
import com.ubtrobot.master.competition.ProtoCompetingCallDelegate;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.master.service.MasterSystemService;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.util.LinkedList;
import java.util.List;

public class LightSystemService extends MasterSystemService {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("LightSystemService");

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

    @Override
    protected List<CompetingItemDetail> getCompetingItems() {
        try {
            List<LightDevice> lightList = mService.getLightList().getDone();
            LinkedList<CompetingItemDetail> details = new LinkedList<>();
            for (LightDevice lightDevice : lightList) {
                details.add(
                        new CompetingItemDetail.Builder(getName(),
                                LightConstants.COMPETING_ITEM_PREFIX_LIGHT + lightDevice.getId()).
                                setDescription("Light competing item").
                                addCallPath(LightConstants.CALL_PATH_TURN_ON).
                                addCallPath(LightConstants.CALL_PATH_CHANGE_COLOR).
                                addCallPath(LightConstants.CALL_PATH_TURN_OFF).
                                build()
                );
            }

            return details;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    @Call(path = LightConstants.CALL_PATH_GET_LIGHT_LIST)
    public void onGetLightList(Request request, Responder responder) {
        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<List<LightDevice>, AccessServiceException>() {
                    @Override
                    public Promise<List<LightDevice>, AccessServiceException>
                    call() throws CallException {
                        return mService.getLightList();
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<List<LightDevice>, AccessServiceException>() {
                    @Override
                    public Message convertDone(List<LightDevice> deviceList) {
                        return LightConverters.toLightDeviceListProto(deviceList);
                    }

                    @Override
                    public CallException convertFail(AccessServiceException e) {
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
                new CompetingCallDelegate.SessionCallable<Void, LightException>() {
                    @Override
                    public Promise<Void, LightException> call() throws CallException {
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
                new CompetingCallDelegate.SessionCallable<Void, LightException>() {
                    @Override
                    public Promise<Void, LightException> call() throws CallException {
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
                new CompetingCallDelegate.SessionCallable<Void, LightException>() {
                    @Override
                    public Promise<Void, LightException> call() throws CallException {
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

    @Call(path = LightConstants.CALL_PATH_GET_LIGHTING_EFFECT_LIST)
    public void onGetEffectList(Request request, Responder responder) {
        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<List<LightingEffect>, AccessServiceException>() {
                    @Override
                    public Promise<List<LightingEffect>, AccessServiceException> call() {
                        return mService.getEffectList();
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<
                        List<LightingEffect>, AccessServiceException>() {
                    @Override
                    public Message convertDone(List<LightingEffect> effectList) {
                        return LightConverters.toLightingEffectListProto(effectList);
                    }

                    @Override
                    public CallException convertFail(AccessServiceException e) {
                        LOGGER.e(e, "Get light effect list failed.");
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Call(path = LightConstants.CALL_PATH_DISPLAY_LIGHTING_EFFECT)
    public void onDisplayEffect(Request request, Responder responder) {
        final LightProto.DisplayOption option = ProtoParamParser.parseParam(request,
                LightProto.DisplayOption.class, responder);
        if (option == null) {
            return;
        }

        LinkedList<String> competingItemIds = new LinkedList<>();
        for (String lightId : option.getLightIdList()) {
            competingItemIds.add(LightConstants.COMPETING_ITEM_PREFIX_LIGHT + lightId);
        }

        mCompetingCallDelegate.onCall(
                request,
                competingItemIds,
                responder,
                new CompetingCallDelegate.SessionCallable<Void, DisplayException>() {
                    @Override
                    public Promise<Void, DisplayException> call() throws CallException {
                        return mService.display(option.getLightIdList(), option.getEffectId(),
                                LightConverters.toDisplayOptionPojo(option));
                    }
                },
                new ProtoCompetingCallDelegate.FConverter<DisplayException>() {
                    @Override
                    public CallException convertFail(DisplayException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }
}
