package com.ubtrobot.light;

import android.os.Handler;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import com.ubtrobot.async.Promise;
import com.ubtrobot.light.ipc.LightConstants;
import com.ubtrobot.light.ipc.LightConverters;
import com.ubtrobot.master.adapter.CallAdapter;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.competition.Competing;
import com.ubtrobot.master.competition.CompetingItem;
import com.ubtrobot.master.competition.CompetitionSession;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.util.Collections;
import java.util.List;

public class Light implements Competing {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("Light");

    private final ProtoCallAdapter mLightService;

    private final LightList mLightList;
    private final LightDevice mDevice;

    private final Handler mHandler;

    Light(ProtoCallAdapter lightService, LightList lightList, LightDevice device, Handler handler) {
        mLightService = lightService;

        mLightList = lightList;
        mDevice = device;

        mHandler = handler;
    }

    @Override
    public List<CompetingItem> getCompetingItems() {
        return Collections.singletonList(new CompetingItem(LightConstants.SERVICE_NAME,
                LightConstants.COMPETING_ITEM_PREFIX_LIGHT + getId()));
    }

    public String getId() {
        return mDevice.getId();
    }

    public LightDevice getDevice() {
        return mDevice;
    }

    public Promise<Void, LightException> turnOn(CompetitionSession session, int argb) {
        checkSession(session);
        checkColor(argb);
        mLightList.get(getId());

        ProtoCallAdapter lightService = new ProtoCallAdapter(
                session.createSystemServiceProxy(LightConstants.SERVICE_NAME),
                mHandler
        );
        return lightService.call(
                LightConstants.CALL_PATH_TURN_ON,
                LightConverters.toLightColorProto(getId(), argb),
                new CallAdapter.FConverter<LightException>() {
                    @Override
                    public LightException convertFail(CallException e) {
                        return new LightException.Factory().from(e);
                    }
                }
        );
    }

    private void checkSession(CompetitionSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Argument session is null.");
        }

        if (!session.containsCompeting(this)) {
            throw new IllegalArgumentException("The competition session does NOT contain the light.");
        }
    }

    private void checkColor(int argb) {
        if (argb < 0) {
            throw new IllegalArgumentException("Argument argb < 0");
        }
    }

    public boolean isTurnOn() {
        try {
            return mLightService.syncCall(LightConstants.CALL_PATH_GET_IS_TURN_ON,
                    StringValue.newBuilder().setValue(getId()).build(), BoolValue.class).
                    getValue();
        } catch (CallException e) {
            LOGGER.e(e, "Framework error when querying if the light is on.");
            return false;
        }
    }

    public Promise<Void, LightException> changeColor(CompetitionSession session, int argb) {
        checkSession(session);
        checkColor(argb);
        mLightList.get(getId());

        ProtoCallAdapter lightService = new ProtoCallAdapter(
                session.createSystemServiceProxy(LightConstants.SERVICE_NAME),
                mHandler
        );
        return lightService.call(
                LightConstants.CALL_PATH_CHANGE_COLOR,
                LightConverters.toLightColorProto(getId(), argb),
                new CallAdapter.FConverter<LightException>() {
                    @Override
                    public LightException convertFail(CallException e) {
                        return new LightException.Factory().from(e);
                    }
                }
        );
    }

    public int getColor() {
        try {
            return mLightService.syncCall(LightConstants.CALL_PATH_GET_COLOR,
                    StringValue.newBuilder().setValue(getId()).build(), Int32Value.class).
                    getValue();
        } catch (CallException e) {
            LOGGER.e(e, "Framework error when getting the light's color.");
            return 0;
        }
    }

    public Promise<Void, LightException> turnOff(CompetitionSession session) {
        checkSession(session);
        mLightList.get(getId());

        ProtoCallAdapter lightService = new ProtoCallAdapter(
                session.createSystemServiceProxy(LightConstants.SERVICE_NAME),
                mHandler
        );
        return lightService.call(
                LightConstants.CALL_PATH_TURN_OFF,
                StringValue.newBuilder().setValue(getId()).build(),
                new CallAdapter.FConverter<LightException>() {
                    @Override
                    public LightException convertFail(CallException e) {
                        return new LightException.Factory().from(e);
                    }
                }
        );
    }
}
