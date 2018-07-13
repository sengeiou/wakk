package com.ubtrobot.diagnosis.sal;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.async.Promise;
import com.ubtrobot.diagnosis.Diagnosis;
import com.ubtrobot.diagnosis.Part;
import com.ubtrobot.diagnosis.ipc.DiagnosisConstants;
import com.ubtrobot.diagnosis.ipc.DiagnosisConverters;
import com.ubtrobot.diagnosis.ipc.DiagnosisProto;
import com.ubtrobot.diagnosis.ipc.master.DiagnosisSystemService;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.Master;
import com.ubtrobot.master.context.ContextRunnable;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.util.Collection;
import java.util.List;

public abstract class AbstractDiagnosisService implements DiagnosisService {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("AbstractDiagnosisService");

    @Override
    public Promise<List<Part>, AccessServiceException> getPartList() {
        AsyncTask<List<Part>, AccessServiceException> task = createGettingPartListTask();
        if (task == null) {
            throw new IllegalStateException("createGettingPartListTask returns null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<List<Part>, AccessServiceException> createGettingPartListTask();

    @Override
    public Promise<List<Diagnosis>, AccessServiceException>
    getDiagnosisList(Collection<String> partIdList) {
        AsyncTask<List<Diagnosis>, AccessServiceException> task =
                createGettingDiagnosisListTask(partIdList);
        if (task == null) {
            throw new IllegalStateException("createGettingDiagnosisListTask returns null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<List<Diagnosis>, AccessServiceException>
    createGettingDiagnosisListTask(Collection<String> partIdList);

    protected void reportDiagnosis(final Diagnosis diagnosis) {
        if (diagnosis == null) {
            throw new IllegalArgumentException("Argument diagnosis is null.");
        }

        boolean sensorSystemServiceStarted = Master.get().execute(
                DiagnosisSystemService.class,
                new ContextRunnable<DiagnosisSystemService>() {
                    @Override
                    public void run(DiagnosisSystemService diagnosisSystemService) {
                        ProtoParam<DiagnosisProto.Diagnosis> param =
                                ProtoParam.create(DiagnosisConverters.toDiagnosisProto(diagnosis));
                        diagnosisSystemService.publish(
                                DiagnosisConstants.ACTION_DIAGNOSE_PREFIX + diagnosis.getPartId(),
                                param
                        );
                    }
                }
        );

        if (!sensorSystemServiceStarted) {
            LOGGER.e("Report diagnosis failed. Pls start DiagnosisSystemService first.");
        }
    }
}