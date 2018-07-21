package com.ubtrobot.diagnosis;

public interface DiagnosisListener {

    void onDiagnose(Part part, Diagnosis diagnosis);
}