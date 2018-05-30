package com.ubtrobot.motion;

import android.os.Handler;

import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import com.ubtrobot.async.Promise;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.competition.Competing;
import com.ubtrobot.master.competition.CompetingItem;
import com.ubtrobot.master.competition.CompetitionSession;
import com.ubtrobot.motion.ipc.MotionConstants;
import com.ubtrobot.transport.message.CallException;

import java.util.LinkedList;
import java.util.List;

public class MotionScriptExecutor implements Competing {

    private final JointList mJointList;
    private final LocomotorGetter mLocomotorGetter;
    private final Handler mHandler = new Handler();

    public MotionScriptExecutor(JointList jointList, LocomotorGetter locomotorGetter) {
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
                MotionConstants.COMPETING_ITEM_SCRIPT_EXECUTOR));
        return items;
    }

    public Promise<Void, ExecuteException> execute(CompetitionSession session, String scriptId) {
        if (session == null || !session.isAccessible(this)) {
            throw new IllegalArgumentException("The session is null or does not contain " +
                    "the competing of the motion script executor.");
        }

        return new ProtoCallAdapter(
                session.createSystemServiceProxy(MotionConstants.SERVICE_NAME),
                mHandler
        ).callStickily(
                MotionConstants.CALL_PATH_EXECUTE_MOTION_SCRIPT,
                StringValue.newBuilder().setValue(scriptId).build(),
                new ProtoCallAdapter.FPProtoConverter<ExecuteException, Void, Message>() {
                    @Override
                    public ExecuteException convertFail(CallException e) {
                        return new ExecuteException.Factory().from(e);
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
