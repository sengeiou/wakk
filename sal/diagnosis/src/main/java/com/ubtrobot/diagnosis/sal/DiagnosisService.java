package com.ubtrobot.diagnosis.sal;

import com.ubtrobot.async.Promise;
import com.ubtrobot.diagnosis.Diagnosis;
import com.ubtrobot.diagnosis.Part;
import com.ubtrobot.exception.AccessServiceException;

import java.util.Collection;
import java.util.List;

public interface DiagnosisService {

    Promise<List<Part>, AccessServiceException> getPartList();

    Promise<List<Diagnosis>, AccessServiceException> getDiagnosisList(Collection<String> partIdList);
}
