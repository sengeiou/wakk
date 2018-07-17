package com.ubtrobot.diagnosis;

import android.os.Handler;
import android.os.Looper;

import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import com.ubtrobot.async.Function;
import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.async.PromiseOperators;
import com.ubtrobot.diagnosis.ipc.DiagnosisConstants;
import com.ubtrobot.diagnosis.ipc.DiagnosisConverters;
import com.ubtrobot.diagnosis.ipc.DiagnosisProto;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.adapter.StaticProtoEventReceiver;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.transport.message.CallException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DiagnosisManager {

    private final MasterContext mMasterContext;
    private final Handler mHandler;

    private final ProtoCallAdapter mService;
    private final PartList mPartList;
    private final HashMap<String, DiagnosisReceiver> mReceivers = new HashMap<>();

    public DiagnosisManager(MasterContext masterContext) {
        mMasterContext = masterContext;
        mHandler = new Handler(Looper.getMainLooper());

        mService = new ProtoCallAdapter(masterContext.createSystemServiceProxy(
                DiagnosisConstants.SERVICE_NAME), mHandler);
        mPartList = new PartList(mService);
    }

    public List<Part> getPartList() {
        return mPartList.all();
    }

    public Part getPart(String id) {
        return mPartList.get(id);
    }

    public Promise<List<Diagnosis>, AccessServiceException>
    getDiagnosisList(Collection<String> partIds) {
        if (partIds == null) {
            throw new IllegalArgumentException("Argument partIds is null.");
        }

        if (partIds.isEmpty()) {
            partIds = new LinkedList<>();
            for (Part part : mPartList.all()) {
                partIds.add(part.getId());
            }
        } else {
            for (String partId : partIds) {
                mPartList.get(partId); // 检查是否存在
            }
        }

        return mService.call(
                DiagnosisConstants.CALL_PATH_GET_DIAGNOSIS_LIST,
                DiagnosisProto.PartIdList.newBuilder().addAllId(partIds).build(),
                new ProtoCallAdapter.DFProtoConverter<
                        List<Diagnosis>, DiagnosisProto.DiagnosisList, AccessServiceException>() {
                    @Override
                    public Class<DiagnosisProto.DiagnosisList> doneProtoClass() {
                        return DiagnosisProto.DiagnosisList.class;
                    }

                    @Override
                    public List<Diagnosis>
                    convertDone(DiagnosisProto.DiagnosisList diagnosisList) throws Exception {
                        return DiagnosisConverters.toDiagnosisListPojo(diagnosisList);
                    }

                    @Override
                    public AccessServiceException convertFail(CallException e) {
                        return new AccessServiceException.Factory().from(e);
                    }
                }
        );
    }

    public Promise<List<Diagnosis>, AccessServiceException> getDiagnosisList(String... partIds) {
        return getDiagnosisList(Arrays.asList(partIds));
    }

    public Promise<Diagnosis, AccessServiceException> getDiagnosis(String partId) {
        return PromiseOperators.mapDone(
                getDiagnosisList(Collections.singleton(partId)),
                new Function<List<Diagnosis>, Diagnosis, AccessServiceException>() {
                    @Override
                    public Diagnosis apply(List<Diagnosis> diagnoses) throws AccessServiceException {
                        if (diagnoses.isEmpty()) {
                            throw new AccessServiceException.Factory().internalError(
                                    "Service returns empty list for getDiagnosisList(aPartId)");
                        }

                        return diagnoses.get(0);
                    }
                }
        );
    }

    public ProgressivePromise<Void, RepairException, RepairProgress> repair(String partId) {
        mPartList.get(partId); // 检查部件是否存在

        return mService.callStickily(
                DiagnosisConstants.CALL_PATH_DIAGNOSIS_REPAIR,
                StringValue.newBuilder().setValue(partId).build(),
                new ProtoCallAdapter.DFPProtoConverter<Void, Message, RepairException, RepairProgress, DiagnosisProto.RepairProgress>() {
                    @Override
                    public Class<Message> doneProtoClass() {
                        return Message.class;
                    }

                    @Override
                    public Void convertDone(Message protoParam) {
                        return null;
                    }

                    @Override
                    public RepairException convertFail(CallException e) {
                        return new RepairException.Factory().from(e);
                    }

                    @Override
                    public Class<DiagnosisProto.RepairProgress> progressProtoClass() {
                        return DiagnosisProto.RepairProgress.class;
                    }

                    @Override
                    public RepairProgress convertProgress(DiagnosisProto.RepairProgress protoParam) {
                        return DiagnosisConverters.toRepairProgressPojo(protoParam);
                    }
                }
        );
    }

    public void registerDiagnosisListener(DiagnosisListener listener, Collection<String> partIds) {
        if (listener == null) {
            throw new IllegalArgumentException("Argument listener is null.");
        }

        if (partIds.isEmpty()) {
            List<Part> parts = mPartList.all();
            if (parts == null) {
                throw new PartList.PartNotFoundException();
            }

            partIds = new LinkedList<>();
            for (Part part : parts) {
                partIds.add(part.getId());
            }
        }

        synchronized (mReceivers) {
            for (String partId : partIds) {
                mPartList.get(partId); // 检查部件是否存在

                DiagnosisReceiver receiver = mReceivers.get(partId);
                if (receiver == null) {
                    receiver = new DiagnosisReceiver();
                    mReceivers.put(partId, receiver);

                    mMasterContext.subscribe(receiver,
                            DiagnosisConstants.ACTION_DIAGNOSE_PREFIX + partId);
                }

                receiver.listeners.add(listener);
            }
        }
    }

    public void registerDiagnosisListener(DiagnosisListener listener, String... partIds) {
        registerDiagnosisListener(listener, Arrays.asList(partIds));
    }

    public void unregisterDiagnosisListener(DiagnosisListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Argument listener is null.");
        }

        synchronized (mReceivers) {
            Iterator<Map.Entry<String, DiagnosisReceiver>> mapIterator = mReceivers.entrySet().iterator();
            while (mapIterator.hasNext()) {
                Map.Entry<String, DiagnosisReceiver> entry = mapIterator.next();
                Iterator<DiagnosisListener> iterator = entry.getValue().listeners.iterator();

                while (iterator.hasNext()) {
                    DiagnosisListener aListener = iterator.next();
                    if (aListener == listener) {
                        iterator.remove();
                    }
                }

                if (entry.getValue().listeners.isEmpty()) {
                    mapIterator.remove();
                    mMasterContext.unsubscribe(entry.getValue());
                }
            }
        }
    }

    private class DiagnosisReceiver extends StaticProtoEventReceiver<DiagnosisProto.Diagnosis> {

        private final List<DiagnosisListener> listeners;

        public DiagnosisReceiver() {
            listeners = new LinkedList<>();
        }

        @Override
        protected Class<DiagnosisProto.Diagnosis> protoClass() {
            return DiagnosisProto.Diagnosis.class;
        }

        @Override
        public void onReceive(
                MasterContext masterContext,
                String action,
                DiagnosisProto.Diagnosis diagnosisProto) {
            final Part part = mPartList.get(diagnosisProto.getPartId());
            final Diagnosis diagnosis = DiagnosisConverters.toDiagnosisPojo(diagnosisProto);

            synchronized (mReceivers) {
                for (final DiagnosisListener listener : listeners) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onDiagnose(part, diagnosis);
                        }
                    });
                }
            }
        }
    }
}