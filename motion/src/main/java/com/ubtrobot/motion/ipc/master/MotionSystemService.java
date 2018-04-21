package com.ubtrobot.motion.ipc.master;

import android.app.Application;
import android.os.Handler;

import com.google.protobuf.BoolValue;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Message;
import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.adapter.CallProcessAdapter;
import com.ubtrobot.master.adapter.ProtoCallProcessAdapter;
import com.ubtrobot.master.adapter.ProtoParamParser;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.competition.CompetingCallDelegate;
import com.ubtrobot.master.competition.ProtoCompetingCallDelegate;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.master.service.MasterSystemService;
import com.ubtrobot.master.transport.message.CallGlobalCode;
import com.ubtrobot.motion.Joint;
import com.ubtrobot.motion.JointDevice;
import com.ubtrobot.motion.JointException;
import com.ubtrobot.motion.JointList;
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

import java.util.List;

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

    @Call(path = MotionConstants.CALL_PATH_GET_JOINT_LIST)
    public void onGetJointList(Request request, Responder responder) {
        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<List<JointDevice>, AccessServiceException, Void>() {
                    @Override
                    public Promise<List<JointDevice>, AccessServiceException, Void>
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
        String jointId;
        if ((jointId = ProtoParamParser.parseStringParam(request, responder)) == null) {
            return;
        }

        try {
            responder.respondSuccess(ProtoParam.create(BoolValue.newBuilder().
                    setValue(mService.isJointRotating(jointId)).build()));
        } catch (JointList.JointNotFoundException e) {
            jointNotFound(jointId, responder);
        }
    }

    private void jointNotFound(String jointId, Responder responder) {
        responder.respondFailure(CallGlobalCode.BAD_REQUEST, "Bad request. Joint not found. " +
                "jointId=" + jointId);
    }

    @Call(path = MotionConstants.CALL_PATH_GET_JOINT_ANGLE)
    public void onGetJointAngle(Request request, Responder responder) {
        String jointId;
        if ((jointId = ProtoParamParser.parseStringParam(request, responder)) == null) {
            return;
        }

        try {
            responder.respondSuccess(ProtoParam.create(FloatValue.newBuilder().
                    setValue(mService.getJointAngle(jointId)).build()
            ));
        } catch (JointList.JointNotFoundException e) {
            jointNotFound(jointId, responder);
        }
    }

    @Call(path = MotionConstants.CALL_PATH_JOINT_ROTATE)
    public void onJointRotate(final Request request, final Responder responder) {
        final MotionProto.JointRotatingOption option;
        if ((option = ProtoParamParser.parseParam(
                request, MotionProto.JointRotatingOption.class, responder)) == null) {
            return;
        }

        mCompetingCallDelegate.onCall(
                request,
                MotionConstants.COMPETING_ITEM_PREFIX_JOINT + option.getJointId(),
                responder,
                new CompetingCallDelegate.SessionCallable<
                        Void, JointException, Joint.RotatingProgress>() {
                    @Override
                    public Promise<Void, JointException, Joint.RotatingProgress>
                    call() throws CallException {
                        try {
                            if (option.getRelatively()) {
                                if (option.getUseSpeed()) {
                                    return mService.jointRotateBy(option.getJointId(),
                                            option.getAngle(), option.getSpeed());
                                } else {
                                    return mService.jointRotateBy(option.getJointId(),
                                            option.getAngle(), option.getTimeMillis());
                                }
                            } else {
                                if (option.getUseSpeed()) {
                                    return mService.jointRotateTo(option.getJointId(),
                                            option.getAngle(), option.getSpeed());
                                } else {
                                    return mService.jointRotateTo(option.getJointId(),
                                            option.getAngle(), option.getTimeMillis());
                                }
                            }
                        } catch (JointList.JointNotFoundException e) {
                            throw new CallException(
                                    CallGlobalCode.BAD_REQUEST,
                                    "Illegal argument. Joint NOT found. jointId=" +
                                            option.getJointId());
                        }
                    }
                },
                new ProtoCompetingCallDelegate.DFPConverter<
                        Void, JointException, Joint.RotatingProgress>() {
                    @Override
                    public Message convertDone(Void done) {
                        return null;
                    }

                    @Override
                    public CallException convertFail(JointException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }

                    @Override
                    public Message convertProgress(Joint.RotatingProgress progress) {
                        return MotionConverters.toJointRotatingProgressProto(progress);
                    }
                }
        );
    }

    @Call(path = MotionConstants.CALL_PATH_GET_LOCOMOTOR)
    public void onGetLocomotor(final Request request, final Responder responder) {
        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<LocomotorDevice, AccessServiceException, Void>() {
                    @Override
                    public Promise<LocomotorDevice, AccessServiceException, Void>
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
}