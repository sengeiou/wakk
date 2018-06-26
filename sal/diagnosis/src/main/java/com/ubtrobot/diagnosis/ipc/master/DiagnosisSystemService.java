package com.ubtrobot.diagnosis.ipc.master;

import android.app.Application;
import android.os.Handler;

import com.google.protobuf.Message;
import com.ubtrobot.async.Promise;
import com.ubtrobot.diagnosis.Part;
import com.ubtrobot.diagnosis.ipc.DiagnosisConstants;
import com.ubtrobot.diagnosis.ipc.DiagnosisConverters;
import com.ubtrobot.diagnosis.sal.AbstractDiagnosisService;
import com.ubtrobot.diagnosis.sal.DiagnosisFactory;
import com.ubtrobot.diagnosis.sal.DiagnosisService;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.adapter.CallProcessAdapter;
import com.ubtrobot.master.adapter.ProtoCallProcessAdapter;
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
}
