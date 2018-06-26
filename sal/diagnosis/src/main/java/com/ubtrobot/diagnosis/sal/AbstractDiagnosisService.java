package com.ubtrobot.diagnosis.sal;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.async.Promise;
import com.ubtrobot.diagnosis.Part;
import com.ubtrobot.exception.AccessServiceException;

import java.util.List;

public abstract class AbstractDiagnosisService implements DiagnosisService {

    @Override
    public Promise<List<Part>, AccessServiceException> getPartList() {
        AsyncTask<List<Part>, AccessServiceException> task = createGettingPartList();
        if (task == null) {
            throw new IllegalStateException("createGettingPartList returns null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<List<Part>, AccessServiceException> createGettingPartList();
}
