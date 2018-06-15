package com.ubtrobot.power;

import android.os.Handler;
import android.os.Looper;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Message;
import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.competition.Competing;
import com.ubtrobot.master.competition.CompetingItem;
import com.ubtrobot.master.competition.CompetitionSession;
import com.ubtrobot.power.ipc.PowerConstants;
import com.ubtrobot.power.ipc.PowerConverters;
import com.ubtrobot.transport.message.CallException;

import java.util.LinkedList;
import java.util.List;

public class ChargingStationConnection implements Competing {

    private static final String MOTION_SERVICE_NAME = "motion";
    private static final String MOTION_COMPETING_ITEM_LOCOMOTOR = "locomotor";

    private final ProtoCallAdapter mPowerService;
    private final Handler mHandler;

    ChargingStationConnection(ProtoCallAdapter powerService) {
        mPowerService = powerService;
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public List<CompetingItem> getCompetingItems() {
        LinkedList<CompetingItem> items = new LinkedList<>();
        items.add(new CompetingItem(MOTION_SERVICE_NAME, MOTION_COMPETING_ITEM_LOCOMOTOR));
        items.add(new CompetingItem(PowerConstants.SERVICE_NAME,
                PowerConstants.COMPETING_ITEM_CHARGING_STATTION_CONNECTION));
        return items;
    }

    public Promise<Boolean, ChargeException>
    connectToChargingStation(CompetitionSession session, ConnectOption option) {
        if (session == null || !session.isAccessible(this)) {
            throw new IllegalArgumentException("The session is null or does not contain " +
                    "the competing of the changing station connection.");
        }

        return new ProtoCallAdapter(
                session.createSystemServiceProxy(PowerConstants.SERVICE_NAME),
                mHandler
        ).callStickily(
                PowerConstants.CALL_PATH_CONNECT_TO_CHARGING_STATION,
                PowerConverters.toConnectOptionProto(option),
                new ConnectionConverter()
        );
    }

    public Promise<Boolean, ChargeException> connectToChargingStation(CompetitionSession session) {
        return connectToChargingStation(session, ConnectOption.DEFAULT);
    }

    public Promise<Boolean, AccessServiceException> isConnectedToChargingStation() {
        return mPowerService.call(
                PowerConstants.CALL_PATH_QUERY_CONNECTED_TO_CHARGING_STATTION,
                new ProtoCallAdapter.DFProtoConverter<Boolean, BoolValue, AccessServiceException>() {
                    @Override
                    public Class<BoolValue> doneProtoClass() {
                        return BoolValue.class;
                    }

                    @Override
                    public Boolean convertDone(BoolValue connected) throws Exception {
                        return connected.getValue();
                    }

                    @Override
                    public AccessServiceException convertFail(CallException e) {
                        return new AccessServiceException.Factory().from(e);
                    }
                }
        );
    }

    public Promise<Boolean, ChargeException>
    disconnectFromChargingStation(CompetitionSession session) {
        if (session == null || !session.isAccessible(this)) {
            throw new IllegalArgumentException("The session is null or does not contain " +
                    "the competing of the changing station connection.");
        }

        return new ProtoCallAdapter(
                session.createSystemServiceProxy(PowerConstants.SERVICE_NAME),
                mHandler
        ).callStickily(
                PowerConstants.CALL_PATH_DISCONNECT_FROM_CHARGING_STATION,
                null,
                new ConnectionConverter()
        );
    }

    private static class ConnectionConverter implements ProtoCallAdapter.DFPProtoConverter<
            Boolean, BoolValue, ChargeException, Void, Message> {

        @Override
        public Class<BoolValue> doneProtoClass() {
            return BoolValue.class;
        }

        @Override
        public Boolean convertDone(BoolValue ret) {
            return ret.getValue();
        }

        @Override
        public ChargeException convertFail(CallException e) {
            return new ChargeException.Factory().from(e);
        }

        @Override
        public Class<Message> progressProtoClass() {
            return Message.class;
        }

        @Override
        public Void convertProgress(Message progress) {
            return null;
        }
    }
}
