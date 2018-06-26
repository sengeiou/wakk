package com.ubtrobot.diagnosis.ipc;

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
}
