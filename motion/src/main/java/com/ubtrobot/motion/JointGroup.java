package com.ubtrobot.motion;

import android.os.Handler;
import android.os.Looper;

import com.google.protobuf.Message;
import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.competition.Competing;
import com.ubtrobot.master.competition.CompetingItem;
import com.ubtrobot.master.competition.CompetitionSession;
import com.ubtrobot.motion.ipc.MotionConstants;
import com.ubtrobot.motion.ipc.MotionConverters;
import com.ubtrobot.motion.ipc.MotionProto;
import com.ubtrobot.transport.message.CallException;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JointGroup implements Competing {

    private final ProtoCallAdapter mMotionService;
    private final List<JointDevice> mJointDevices;
    private final Map<String, JointDevice> mJointDeviceMap;

    private final Handler mHandler;

    JointGroup(ProtoCallAdapter motionService, List<JointDevice> jointDevices) {
        mMotionService = motionService;
        mJointDevices = Collections.unmodifiableList(jointDevices);

        HashMap<String, JointDevice> jointDeviceMap = new HashMap<>();
        for (JointDevice jointDevice : mJointDevices) {
            jointDeviceMap.put(jointDevice.getId(), jointDevice);
        }
        mJointDeviceMap = Collections.unmodifiableMap(jointDeviceMap);

        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public List<CompetingItem> getCompetingItems() {
        LinkedList<CompetingItem> items = new LinkedList<>();
        for (JointDevice jointDevice : mJointDevices) {
            items.add(new CompetingItem(MotionConstants.SERVICE_NAME,
                    MotionConstants.COMPETING_ITEM_PREFIX_JOINT + jointDevice.getId()));
        }

        return items;
    }

    public List<JointDevice> getJointDeviceList() {
        return mJointDevices;
    }

    public ProgressivePromise<Void, JointException, JointGroupRotatingProgress>
    rotate(CompetitionSession session, Map<String, List<JointRotatingOption>> optionSequenceMap) {
        if (session == null || !session.isAccessible(this)) {
            throw new IllegalArgumentException("The session is null or does not contain " +
                    "the competing of the joint group.");
        }

        MotionProto.JointRotatingOptionSequenceMap.Builder mapBuilder
                = MotionProto.JointRotatingOptionSequenceMap.newBuilder();
        for (Map.Entry<String, List<JointRotatingOption>> entry : optionSequenceMap.entrySet()) {
            String jointId = entry.getKey();
            List<JointRotatingOption> options = entry.getValue();
            if (!mJointDeviceMap.containsKey(jointId) || options.isEmpty()) {
                continue;
            }

            MotionProto.JointRotatingOptionSequence.Builder seqBuilder
                    = MotionProto.JointRotatingOptionSequence.newBuilder();

            for (JointRotatingOption option : options) {
                seqBuilder.addOption(MotionConverters.toJointRotatingOptionProto(option, jointId));
            }

            mapBuilder.putOptionSequence(jointId, seqBuilder.build());
        }

        MotionProto.JointRotatingOptionSequenceMap sequenceMap = mapBuilder.build();
        if (sequenceMap.getOptionSequenceCount() == 0) {
            throw new IllegalArgumentException("Illegal argument optionSequenceMap." +
                    "NOT contain available option sequence.");
        }

        return new ProtoCallAdapter(
                session.createSystemServiceProxy(MotionConstants.SERVICE_NAME),
                mHandler
        ).callStickily(
                MotionConstants.CALL_PATH_JOINT_ROTATE,
                sequenceMap,
                new ProtoCallAdapter.DFPProtoConverter<Void, Message, JointException,
                        JointGroupRotatingProgress, MotionProto.JointGroupRotatingProgress>() {
                    @Override
                    public Class<Message> doneProtoClass() {
                        return Message.class;
                    }

                    @Override
                    public Void convertDone(Message message) {
                        return null;
                    }

                    @Override
                    public JointException convertFail(CallException e) {
                        return new JointException.Factory().from(e);
                    }

                    @Override
                    public Class<MotionProto.JointGroupRotatingProgress> progressProtoClass() {
                        return MotionProto.JointGroupRotatingProgress.class;
                    }

                    @Override
                    public JointGroupRotatingProgress
                    convertProgress(MotionProto.JointGroupRotatingProgress progress) {
                        return MotionConverters.toJointGroupRotatingProgressPojo(progress);
                    }
                }
        );
    }

    public Promise<Map<String, Boolean>, AccessServiceException> isRotating() {
        return mMotionService.call(
                MotionConstants.CALL_PATH_QUERY_JOINT_IS_ROTATING,
                MotionConverters.toJointIdListProto(mJointDevices),
                new ProtoCallAdapter.DFProtoConverter<Map<String, Boolean>,
                        MotionProto.JointIdList, AccessServiceException>() {
                    @Override
                    public Class<MotionProto.JointIdList> doneProtoClass() {
                        return MotionProto.JointIdList.class;
                    }

                    @Override
                    public Map<String, Boolean> convertDone(MotionProto.JointIdList jointIdList) {
                        HashSet<String> idSet = new HashSet<>(jointIdList.getIdList());
                        HashMap<String, Boolean> ret = new HashMap<>();

                        for (JointDevice jointDevice : mJointDevices) {
                            ret.put(jointDevice.getId(), idSet.contains(jointDevice.getId()));
                        }

                        return ret;
                    }

                    @Override
                    public AccessServiceException convertFail(CallException e) {
                        return new AccessServiceException.Factory().from(e);
                    }
                }
        );
    }

    public Promise<Map<String, Float>, AccessServiceException> getAngles() {
        return mMotionService.call(
                MotionConstants.CALL_PATH_GET_JOINT_ANGLE,
                MotionConverters.toJointIdListProto(mJointDevices),
                new ProtoCallAdapter.DFProtoConverter<Map<String, Float>,
                        MotionProto.JointAngleMap, AccessServiceException>() {
                    @Override
                    public Class<MotionProto.JointAngleMap> doneProtoClass() {
                        return MotionProto.JointAngleMap.class;
                    }

                    @Override
                    public Map<String, Float> convertDone(MotionProto.JointAngleMap jointAngleMap) {
                        return jointAngleMap.getAngleMap();
                    }

                    @Override
                    public AccessServiceException convertFail(CallException e) {
                        return new AccessServiceException.Factory().from(e);
                    }
                }
        );
    }

    @Override
    public String toString() {
        return "JointGroup{" +
                "jointDevices=" + mJointDevices +
                '}';
    }
}