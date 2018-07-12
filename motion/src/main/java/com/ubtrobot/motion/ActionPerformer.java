package com.ubtrobot.motion;

import android.os.Handler;

import com.google.protobuf.Message;
import com.ubtrobot.async.Promise;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.competition.Competing;
import com.ubtrobot.master.competition.CompetingItem;
import com.ubtrobot.master.competition.CompetitionSession;
import com.ubtrobot.motion.ipc.MotionConstants;
import com.ubtrobot.motion.ipc.MotionProto;
import com.ubtrobot.transport.message.CallException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ActionPerformer implements Competing {

    private final JointList mJointList;
    private final LocomotorGetter mLocomotorGetter;
    private final Handler mHandler = new Handler();

    public ActionPerformer(JointList jointList, LocomotorGetter locomotorGetter) {
        mJointList = jointList;
        mLocomotorGetter = locomotorGetter;
    }

    @Override
    public List<CompetingItem> getCompetingItems() {
        LinkedList<CompetingItem> items = new LinkedList<>();
        for (Joint joint : mJointList.all()) {
            items.add(new CompetingItem(MotionConstants.SERVICE_NAME,
                    MotionConstants.COMPETING_ITEM_PREFIX_JOINT + joint.getId()));
        }

        if (mLocomotorGetter.exists()) {
            items.add(new CompetingItem(MotionConstants.SERVICE_NAME,
                    MotionConstants.COMPETING_ITEM_LOCOMOTOR));
        }

        items.add(new CompetingItem(MotionConstants.SERVICE_NAME,
                MotionConstants.COMPETING_ITEM_ACTION_PERFORMER));
        return items;
    }

    public Promise<Void, PerformException>
    performAction(CompetitionSession session, List<String> actionIdList) {
        if (session == null || !session.isAccessible(this)) {
            throw new IllegalArgumentException("The session is null or does not contain " +
                    "the competing of the motion action performer.");
        }

        MotionProto.ActionIdList.Builder builder
                = MotionProto.ActionIdList.newBuilder().addAllId(actionIdList);

        return new ProtoCallAdapter(
                session.createSystemServiceProxy(MotionConstants.SERVICE_NAME),
                mHandler
        ).callStickily(
                MotionConstants.CALL_PATH_PERFORM_MOTION_ACTION,
                builder.build(),
                new ProtoCallAdapter.FPProtoConverter<PerformException, Void, Message>() {
                    @Override
                    public PerformException convertFail(CallException e) {
                        return new PerformException.Factory().from(e);
                    }

                    @Override
                    public Class<Message> progressProtoClass() {
                        return Message.class;
                    }

                    @Override
                    public Void convertProgress(Message protoParam) {
                        return null;
                    }
                }
        );
    }
}
