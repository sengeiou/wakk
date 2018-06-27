package com.ubtrobot.diagnosis.ipc;

import com.ubtrobot.diagnosis.Diagnosis;
import com.ubtrobot.diagnosis.Part;

import java.util.LinkedList;
import java.util.List;

public class DiagnosisConverters {

    private DiagnosisConverters() {
    }

    public static List<Part> toPartListPojo(DiagnosisProto.PartList partListProto) {
        LinkedList<Part> partList = new LinkedList<>();
        for (DiagnosisProto.Part part : partListProto.getPartList()) {
            partList.add(toPartPojo(part));
        }

        return partList;
    }

    public static Part toPartPojo(DiagnosisProto.Part partProto) {
        return new Part.Builder(partProto.getId()).setName(partProto.getName())
                .setDescription(partProto.getDescription()).build();
    }

    public static DiagnosisProto.PartList toPartListProto(List<Part> partList) {
        DiagnosisProto.PartList.Builder builder = DiagnosisProto.PartList.newBuilder();
        for (Part part : partList) {
            builder.addPart(toPartProto(part));
        }
        return builder.build();
    }

    public static DiagnosisProto.Part toPartProto(Part part) {
        return DiagnosisProto.Part.newBuilder().setId(part.getId()).setName(part.getName())
                .setDescription(part.getDescription()).build();
    }

    public static Diagnosis toDiagnosisPojo(DiagnosisProto.Diagnosis diagnosisProto) {
        return new Diagnosis.Builder().setPartId(diagnosisProto.getPartId())
                .setFaulty(diagnosisProto.getFaulty()).setFault(diagnosisProto.getFault())
                .setCause(diagnosisProto.getCause()).build();
    }

    public static DiagnosisProto.Diagnosis toDiagnosisProto(Diagnosis diagnosisProto) {
        return DiagnosisProto.Diagnosis.newBuilder().setPartId(diagnosisProto.getPartId())
                .setFaulty(diagnosisProto.isFaulty()).setFault(diagnosisProto.getFault())
                .setCause(diagnosisProto.getCause()).build();
    }

    public static List<Diagnosis>
    toDiagnosisListPojo(DiagnosisProto.DiagnosisList diagnosisListProto) {
        LinkedList<Diagnosis> diagnosesList = new LinkedList<>();
        for (DiagnosisProto.Diagnosis diagnosis : diagnosisListProto.getDiagnosisList()) {
            diagnosesList.add(toDiagnosisPojo(diagnosis));
        }
        return diagnosesList;
    }

    public static DiagnosisProto.DiagnosisList toDiagnosisListProto(List<Diagnosis> diagnosisList) {
        DiagnosisProto.DiagnosisList.Builder builder = DiagnosisProto.DiagnosisList.newBuilder();
        for (Diagnosis diagnosis : diagnosisList) {
            builder.addDiagnosis(toDiagnosisProto(diagnosis));
        }
        return builder.build();
    }
}
