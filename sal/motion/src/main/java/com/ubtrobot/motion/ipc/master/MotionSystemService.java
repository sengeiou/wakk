package com.ubtrobot.motion.ipc.master;

import android.app.Application;
import android.os.Handler;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Message;
import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.adapter.CallProcessAdapter;
import com.ubtrobot.master.adapter.ProtoCallProcessAdapter;
import com.ubtrobot.master.adapter.ProtoParamParser;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.competition.CompetingCallDelegate;
import com.ubtrobot.master.competition.CompetingItemDetail;
import com.ubtrobot.master.competition.CompetitionSessionInfo;
import com.ubtrobot.master.competition.ProtoCompetingCallDelegate;
import com.ubtrobot.master.service.MasterSystemService;
import com.ubtrobot.motion.PerformException;
import com.ubtrobot.motion.JointDevice;
import com.ubtrobot.motion.JointException;
import com.ubtrobot.motion.JointGroupRotatingProgress;
import com.ubtrobot.motion.LocomotionException;
import com.ubtrobot.motion.LocomotionProgress;
import com.ubtrobot.motion.LocomotorDevice;
import com.ubtrobot.motion.ipc.MotionConstants;
import com.ubtrobot.motion.ipc.MotionConverters;
import com.ubtrobot.motion.ipc.MotionProto;
import com.ubtrobot.motion.sal.AbstractMotionService;
import com.ubtrobot.motion.sal.MotionFactory;
import com.ubtrobot.motion.sal.MotionService;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MotionSystemService extends MasterSystemService {

    private MotionService mService;
    private ProtoCallProcessAdapter mCallProcessor;
    private ProtoCompetingCallDelegate mCompetingCallDelegate;

    @Override
    protected void onServiceCreate() {
        Application application = getApplication();
        if (!(application instanceof MotionFactory)) {
            throw new IllegalStateException(
                    "Your application should implement MotionFactory interface.");
        }

        mService = ((MotionFactory) application).createMotionService();
        if (mService == null || !(mService instanceof AbstractMotionService)) {
            throw new IllegalStateException("Your application 's createMotionService returns null" +
                    " or does not return a instance of createMotionService.");
        }

        Handler handler = new Handler(getMainLooper());
        mCallProcessor = new ProtoCallProcessAdapter(handler);
        mCompetingCallDelegate = new ProtoCompetingCallDelegate(this, handler);
    }

    @Override
    protected List<CompetingItemDetail> getCompetingItems() {
        LinkedList<CompetingItemDetail> itemDetails = new LinkedList<>();
        try {
            List<JointDevice> jointDevices = mService.getJointList().get();
            for (JointDevice jointDevice : jointDevices) {
                itemDetails.add(
                        new CompetingItemDetail.Builder(getName(), MotionConstants.COMPETING_ITEM_PREFIX_JOINT + jointDevice.getId())
                                .addCallPath(MotionConstants.CALL_PATH_JOINT_ROTATE)
                                .addCallPath(MotionConstants.CALL_PATH_JOINT_RELEASE)
                                .setDescription("Competing item detail for joint " + jointDevice.getId())
                                .build()
                );
            }

            itemDetails.add(new CompetingItemDetail.Builder(
                    getName(),
                    MotionConstants.COMPETING_ITEM_ACTION_PERFORMER
            ).addCallPath(MotionConstants.CALL_PATH_PERFORM_MOTION_ACTION)
                    .setDescription("Competing item detail for performing action")
                    .build());

            itemDetails.add(new CompetingItemDetail.Builder(
                    getName(), MotionConstants.COMPETING_ITEM_LOCOMOTOR
            ).addCallPath(MotionConstants.CALL_PATH_LOCOMOTE)
                    .setDescription("Competing item detail for locomotion")
                    .build());
            return itemDetails;
        } catch (AccessServiceException e) {
            throw new IllegalStateException(e);
        }
    }

    @Call(path = MotionConstants.CALL_PATH_GET_JOINT_LIST)
    public void onGetJointList(Request request, Responder responder) {
        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<List<JointDevice>, AccessServiceException>() {
                    @Override
                    public Promise<List<JointDevice>, AccessServiceException>
                    call() throws CallException {
                        return mService.getJointList();
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<List<JointDevice>, AccessServiceException>() {
                    @Override
                    public Message convertDone(List<JointDevice> jointDevices) {
                        return MotionConverters.toJointDeviceListProto(jointDevices);
                    }

                    @Override
                    public CallException convertFail(AccessServiceException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Call(path = MotionConstants.CALL_PATH_QUERY_JOINT_IS_ROTATING)
    public void onQueryJointIsRotating(Request request, Responder responder) {
        final MotionProto.JointIdList jointIdList;
        if ((jointIdList = ProtoParamParser.parseParam(
                request, MotionProto.JointIdList.class, responder)) == null) {
            return;
        }

        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<List<String>, AccessServiceException>() {
                    @Override
                    public Promise<List<String>, AccessServiceException> call() throws CallException {
                        return mService.isJointsRotating(jointIdList.getIdList());
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<List<String>, AccessServiceException>() {
                    @Override
                    public Message convertDone(List<String> idList) {
                        return MotionProto.JointIdList.newBuilder().addAllId(idList).build();
                    }

                    @Override
                    public CallException convertFail(AccessServiceException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Call(path = MotionConstants.CALL_PATH_GET_JOINT_ANGLE)
    public void onGetJointAngle(Request request, Responder responder) {
        final MotionProto.JointIdList jointIdList;
        if ((jointIdList = ProtoParamParser.parseParam(
                request, MotionProto.JointIdList.class, responder)) == null) {
            return;
        }

        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<Map<String, Float>, AccessServiceException>() {
                    @Override
                    public Promise<Map<String, Float>, AccessServiceException> call() throws CallException {
                        return mService.getJointsAngle(jointIdList.getIdList());
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<Map<String, Float>, AccessServiceException>() {
                    @Override
                    public Message convertDone(Map<String, Float> angleMap) {
                        return MotionProto.JointAngleMap.newBuilder().putAllAngle(angleMap).build();
                    }

                    @Override
                    public CallException convertFail(AccessServiceException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Call(path = MotionConstants.CALL_PATH_JOINT_ROTATE)
    public void onJointRotate(final Request request, final Responder responder) {
        final MotionProto.JointRotatingOptionSequenceMap optionSequenceMap;
        if ((optionSequenceMap = ProtoParamParser.parseParam(
                request, MotionProto.JointRotatingOptionSequenceMap.class, responder)) == null) {
            return;
        }

        LinkedList<String> competingItemIds = new LinkedList<>();
        for (String jointId : optionSequenceMap.getOptionSequenceMap().keySet()) {
            competingItemIds.add(MotionConstants.COMPETING_ITEM_PREFIX_JOINT + jointId);
        }

        mCompetingCallDelegate.onCall(
                request,
                competingItemIds,
                responder,
                new CompetingCallDelegate.SessionProgressiveCallable<Void, JointException, JointGroupRotatingProgress>() {
                    @Override
                    public ProgressivePromise<Void, JointException, JointGroupRotatingProgress>
                    call() throws CallException {
                        return mService.jointRotate(MotionConverters.
                                toJointRotatingOptionSequenceMapPojo(optionSequenceMap));
                    }
                },
                new ProtoCompetingCallDelegate.DFPConverter<Void, JointException, JointGroupRotatingProgress>() {
                    @Override
                    public Message convertProgress(JointGroupRotatingProgress progress) {
                        return MotionConverters.toJointGroupRotatingProgressProto(progress);
                    }

                    @Override
                    public Message convertDone(Void done) {
                        return null;
                    }

                    @Override
                    public CallException convertFail(JointException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Call(path = MotionConstants.CALL_PATH_GET_LOCOMOTOR)
    public void onGetLocomotor(final Request request, final Responder responder) {
        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<LocomotorDevice, AccessServiceException>() {
                    @Override
                    public Promise<LocomotorDevice, AccessServiceException>
                    call() throws CallException {
                        return mService.getLocomotor();
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<LocomotorDevice, AccessServiceException>() {
                    @Override
                    public Message convertDone(LocomotorDevice locomotorDevice) {
                        return MotionConverters.toLocomotorDeviceProto(locomotorDevice);
                    }

                    @Override
                    public CallException convertFail(AccessServiceException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Call(path = MotionConstants.CALL_PATH_QUERY_IS_LOCOMOTING)
    public void onQueryIsLocomoting(Request request, Responder responder) {
        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<Boolean, AccessServiceException>() {
                    @Override
                    public Promise<Boolean, AccessServiceException> call() throws CallException {
                        return mService.isLocomoting();
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<Boolean, AccessServiceException>() {
                    @Override
                    public Message convertDone(Boolean locomoting) {
                        return BoolValue.newBuilder().setValue(locomoting).build();
                    }

                    @Override
                    public CallException convertFail(AccessServiceException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Call(path = MotionConstants.CALL_PATH_LOCOMOTE)
    public void onLocomote(Request request, Responder responder) {
        final MotionProto.LocomotionOptionSequence optionSequence;
        if ((optionSequence = ProtoParamParser.parseParam(
                request, MotionProto.LocomotionOptionSequence.class, responder)) == null) {
            return;
        }

        mCompetingCallDelegate.onCall(
                request,
                MotionConstants.COMPETING_ITEM_LOCOMOTOR,
                responder,
                new CompetingCallDelegate.SessionProgressiveCallable<
                        Void, LocomotionException, LocomotionProgress>() {
                    @Override
                    public ProgressivePromise<Void, LocomotionException, LocomotionProgress>
                    call() throws CallException {
                        return mService.locomote(MotionConverters
                                .toLocomotionOptionSequencePojo(optionSequence));
                    }
                },
                new ProtoCompetingCallDelegate.DFPConverter<
                        Void, LocomotionException, LocomotionProgress>() {
                    @Override
                    public Message convertProgress(LocomotionProgress progress) {
                        return MotionConverters.toLocomotionProgressProto(progress);
                    }

                    @Override
                    public Message convertDone(Void done) {
                        return null;
                    }

                    @Override
                    public CallException convertFail(LocomotionException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Call(path = MotionConstants.CALL_PATH_PERFORM_MOTION_ACTION)
    public void onActionPerform(final Request request, final Responder responder) {
        final MotionProto.ActionIdList actionIdList;
        if ((actionIdList = ProtoParamParser.parseParam(
                request, MotionProto.ActionIdList.class, responder)) == null) {
            return;
        }

        LinkedList<String> competingItems = new LinkedList<>();
        try {
            List<JointDevice> jointDevices = mService.getJointList().get();
            for (JointDevice jointDevice : jointDevices) {
                competingItems.add(MotionConstants.COMPETING_ITEM_PREFIX_JOINT + jointDevice.getId());
            }
        } catch (AccessServiceException e) {
            throw new IllegalStateException(e);
        }

        mCompetingCallDelegate.onCall(
                request,
                competingItems,
                responder,
                new CompetingCallDelegate.SessionCallable<Void, PerformException>() {
                    @Override
                    public Promise<Void, PerformException> call() throws CallException {
                        return mService.performAction(actionIdList.getIdList());
                    }
                },
                new ProtoCompetingCallDelegate.FConverter<PerformException>() {
                    @Override
                    public CallException convertFail(PerformException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Call(path = MotionConstants.CALL_PATH_JOINT_RELEASE)
    public void onJointRelease(final Request request, final Responder responder) {
        final MotionProto.JointIdList jointIdList;
        if ((jointIdList = ProtoParamParser.parseParam(
                request, MotionProto.JointIdList.class, responder)) == null) {
            return;
        }

        LinkedList<String> competingItemIds = new LinkedList<>();
        for (String string : jointIdList.getIdList()) {
            competingItemIds.add(MotionConstants.COMPETING_ITEM_PREFIX_JOINT + string);
        }

        mCompetingCallDelegate.onCall(
                request,
                competingItemIds,
                responder, new CompetingCallDelegate.SessionCallable<Void, JointException>() {
                    @Override
                    public Promise<Void, JointException> call() throws CallException {
                        return mService.jointsRelease(jointIdList.getIdList());
                    }
                }, new ProtoCompetingCallDelegate.DFConverter<Void, JointException>() {
                    @Override
                    public Message convertDone(Void done) {
                        return null;
                    }

                    @Override
                    public CallException convertFail(JointException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                });
    }

    @Call(path = MotionConstants.CALL_PATH_QUERY_JOINT_IS_RELEASED)
    public void onQueryJointIsReleased(Request request, Responder responder) {
        final MotionProto.JointIdList jointIdList;
        if ((jointIdList = ProtoParamParser.parseParam(
                request, MotionProto.JointIdList.class, responder)) == null) {
            return;
        }

        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<List<String>, JointException>() {
                    @Override
                    public Promise<List<String>, JointException> call() throws CallException {
                        return mService.isJointsReleased(jointIdList.getIdList());
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<List<String>, JointException>() {
                    @Override
                    public Message convertDone(List<String> idList) {
                        return MotionProto.JointIdList.newBuilder().addAllId(idList).build();
                    }

                    @Override
                    public CallException convertFail(JointException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Override
    protected void onCompetitionSessionInactive(CompetitionSessionInfo sessionInfo) {
        mCompetingCallDelegate.onCompetitionSessionInactive(sessionInfo);
    }
}