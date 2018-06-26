package com.ubtrobot.diagnosis.sal;

import com.ubtrobot.async.Promise;
import com.ubtrobot.diagnosis.Part;
import com.ubtrobot.exception.AccessServiceException;

import java.util.List;

public interface DiagnosisService {

    Promise<List<Part>, AccessServiceException> getPartList();
}
