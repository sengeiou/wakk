package com.ubtrobot.diagnosis;

import android.os.Handler;
import android.os.Looper;

import com.ubtrobot.diagnosis.ipc.DiagnosisConstants;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.context.MasterContext;

import java.util.List;

public class DiagnosisManager {

    private final PartList mPartList;

    public DiagnosisManager(MasterContext masterContext) {
        mPartList = new PartList(new ProtoCallAdapter(
                masterContext.createSystemServiceProxy(DiagnosisConstants.SERVICE_NAME),
                new Handler(Looper.getMainLooper())
        ));
    }

    public List<Part> getPartList() {
        return mPartList.all();
    }

    public Part getPart(String id) {
        return mPartList.get(id);
    }
}
