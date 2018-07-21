package com.ubtrobot.diagnosis.ipc.master;

import android.app.Application;
import android.os.Handler;

import com.google.protobuf.Message;
import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.diagnosis.Diagnosis;
import com.ubtrobot.diagnosis.Part;
import com.ubtrobot.diagnosis.RepairException;
import com.ubtrobot.diagnosis.RepairProgress;
import com.ubtrobot.diagnosis.ipc.DiagnosisConstants;
import com.ubtrobot.diagnosis.ipc.DiagnosisConverters;
import com.ubtrobot.diagnosis.ipc.DiagnosisProto;
import com.ubtrobot.diagnosis.sal.AbstractDiagnosisService;
import com.ubtrobot.diagnosis.sal.DiagnosisFactory;
import com.ubtrobot.diagnosis.sal.DiagnosisService;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.adapter.CallProcessAdapter;
import com.ubtrobot.master.adapter.ProtoCallProcessAdapter;
import com.ubtrobot.master.adapter.ProtoParamParser;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.service.MasterSystemService;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;

import java.util.List;

public class DiagnosisSystemService extends MasterSystemService {

    private DiagnosisService mService;
    private ProtoCallProcessAdapter mCallProcessor;

    @Override
    protected void onServiceCreate() {
        Application application = getApplication();
        if (!(application instanceof DiagnosisFactory)) {
            throw new IllegalStateException(
                    "Your application should implement DiagnosisFactory interface.");
        }

        mService = ((DiagnosisFactory) application).createDiagnosisService();
        if (mService == null || !(mService instanceof AbstractDiagnosisService)) {
            throw new IllegalStateException("Your application 's createDiagnosisService returns null" +
                    " or does not return a instance of AbstractDiagnosisService.");
        }

        Handler handler = new Handler(getMainLooper());
        mCallProcessor = new ProtoCallProcessAdapter(handler);
    }

    @Call(path = DiagnosisConstants.CALL_PATH_GET_PART_LIST)
    public void onGetPartList(Request request, Responder responder) {
        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<List<Part>, AccessServiceException>() {
                    @Override
                    public Promise<List<Part>, AccessServiceException> call() throws CallException {
                        return mService.getPartList();
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<List<Part>, AccessServiceException>() {
                    @Override
                    public Message convertDone(List<Part> partList) {
                        return DiagnosisConverters.toPartListProto(partList);
                    }

                    @Override
                    public CallException convertFail(AccessServiceException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Call(path = DiagnosisConstants.CALL_PATH_GET_DIAGNOSIS_LIST)
    public void onGetDiagnosisList(Request request, Responder responder) {
        final DiagnosisProto.PartIdList partIdList = ProtoParamParser.parseParam(
                request, DiagnosisProto.PartIdList.class, responder);
        if (partIdList == null) {
            return;
        }

        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<List<Diagnosis>, AccessServiceException>() {
                    @Override
                    public Promise<List<Diagnosis>, AccessServiceException> call() throws CallException {
                        return mService.getDiagnosisList(partIdList.getIdList());
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<List<Diagnosis>, AccessServiceException>() {
                    @Override
                    public Message convertDone(List<Diagnosis> diagnosisList) {
                        return DiagnosisConverters.toDiagnosisListProto(diagnosisList);
                    }

                    @Override
                    public CallException convertFail(AccessServiceException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    @Call(path = DiagnosisConstants.CALL_PATH_DIAGNOSIS_REPAIR)
    public void onDiagnosisRepair(Request request, Responder responder) {
        final String partId = ProtoParamParser.parseStringParam(request, responder);

        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.ProgressiveCallable<Void, RepairException, RepairProgress>() {
                    @Override
                    public ProgressivePromise<Void, RepairException, RepairProgress> call() throws CallException {
                        return mService.repair(partId);
                    }
                },
                new ProtoCallProcessAdapter.DFPConverter<Void, RepairException, RepairProgress>() {
                    @Override
                    public Message convertProgress(RepairProgress progress) {
                        return DiagnosisConverters.toRepairProgressProto(progress);
                    }

                    @Override
                    public Message convertDone(Void done) {
                        return null;
                    }

                    @Override
                    public CallException convertFail(RepairException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }
}
