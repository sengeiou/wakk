package com.ubtrobot.diagnosis;

import com.ubtrobot.cache.CachedField;
import com.ubtrobot.diagnosis.ipc.DiagnosisConstants;
import com.ubtrobot.diagnosis.ipc.DiagnosisConverters;
import com.ubtrobot.diagnosis.ipc.DiagnosisProto;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartList {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("PartList");

    private final CachedField<List<Part>> mParts;
    private final CachedField<Map<String, Part>> mPartMap;

    PartList(final ProtoCallAdapter diagnosisService) {
        mParts = new CachedField<>(new CachedField.FieldGetter<List<Part>>() {
            @Override
            public List<Part> get() {
                try {
                    DiagnosisProto.PartList partList = diagnosisService.syncCall(
                            DiagnosisConstants.CALL_PATH_GET_PART_LIST,
                            DiagnosisProto.PartList.class);
                    return Collections.unmodifiableList(DiagnosisConverters.toPartListPojo(partList));
                } catch (CallException e) {
                    LOGGER.e(e, "Framework error when getting the diagnosable part list.");
                }

                return null;
            }
        });

        mPartMap = new CachedField<>(new CachedField.FieldGetter<Map<String, Part>>() {
            @Override
            public Map<String, Part> get() {
                List<Part> parts = mParts.get();
                if (parts == null) {
                    return null;
                }

                HashMap<String, Part> partMap = new HashMap<>();
                for (Part part : parts) {
                    partMap.put(part.getId(), part);
                }

                return partMap;
            }
        });
    }

    public List<Part> all() {
        List<Part> parts = mParts.get();
        return parts == null ? Collections.<Part>emptyList() : parts;
    }

    public Part get(String partId) {
        Map<String, Part> partMap = mPartMap.get();
        Part part = partMap == null ? null : partMap.get(partId);

        if (part == null) {
            throw new PartNotFoundException();
        }
        return part;
    }

    public static class PartNotFoundException extends RuntimeException {
    }
}
