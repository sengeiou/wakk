package com.ubtrobot.diagnosis.sal;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.async.BaseProgressivePromise;
import com.ubtrobot.async.DefaultProgressivePromise;
import com.ubtrobot.async.ProgressiveAsyncTask;
import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.diagnosis.Diagnosis;
import com.ubtrobot.diagnosis.Part;
import com.ubtrobot.diagnosis.RepairException;
import com.ubtrobot.diagnosis.RepairProgress;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractDiagnosisService implements DiagnosisService {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("AbstractDiagnosisService");

    private final Map<String, BaseProgressivePromise> mRepqiringParts = new HashMap<>();

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

    @Override
    public ProgressivePromise<Void, RepairException, RepairProgress> repair(final String partId) {
        synchronized (mRepqiringParts) {
            if (mRepqiringParts.containsKey(partId)) {
                DefaultProgressivePromise<Void, RepairException, RepairProgress> promise
                        = new DefaultProgressivePromise<>();
                promise.reject(new RepairException.Factory().prohibitReentry("Prohibit reentry. Already repairing."));

                return promise;
            }

            ProgressiveAsyncTask<Void, RepairException, RepairProgress> task
                    = new ProgressiveAsyncTask<Void, RepairException, RepairProgress>() {
                @Override
                protected void onStart() {
                    synchronized (mRepqiringParts) {
                        startRepairing(partId);
                    }
                }

                @Override
                protected void onCancel() {
                    synchronized (mRepqiringParts) {
                        stopRepairing(partId);
                        mRepqiringParts.remove(partId);
                    }
                }
            };

            mRepqiringParts.put(partId, task.promise());
            task.start();

            return task.promise();
        }
    }

    protected void reportRepairingProgress(String partId, RepairProgress progress) {
        synchronized (mRepqiringParts) {
            BaseProgressivePromise promise = mRepqiringParts.get(partId);
            if (null != promise) {
                promise.report(progress);
            }
        }
    }

    protected void resolveRepairing(String partId) {
        synchronized (mRepqiringParts) {
            BaseProgressivePromise promise = mRepqiringParts.get(partId);
            if (null != promise) {
                promise.resolve(null);

                mRepqiringParts.remove(partId);
            }
        }
    }

    protected void rejectRepairing(String partId, RepairException e) {
        synchronized (mRepqiringParts) {
            BaseProgressivePromise promise = mRepqiringParts.get(partId);
            if (null != promise) {
                promise.reject(e);

                mRepqiringParts.remove(partId);
            }
        }
    }

    protected abstract void startRepairing(String partId);

    protected abstract void stopRepairing(String partId);
}
