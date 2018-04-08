package com.ubtrobot.upgrade.ipc;

import com.ubtrobot.upgrade.Firmware;
import com.ubtrobot.upgrade.FirmwarePackage;

import java.util.LinkedList;
import java.util.List;

public class UpgradeConverters {

    private UpgradeConverters() {
    }

    public static List<Firmware> toFirmwareListPojo(UpgradeProto.FirmwareList firmwareListProto) {
        LinkedList<Firmware> firmwareList = new LinkedList<>();
        for (UpgradeProto.Firmware firmwareProto : firmwareListProto.getFirmwareList()) {
            firmwareList.add(toFirmwarePojo(firmwareProto));
        }

        return firmwareList;
    }

    public static Firmware toFirmwarePojo(UpgradeProto.Firmware firmwareProto) {
        return new Firmware.Builder(firmwareProto.getName(), firmwareProto.getVersion()).
                setUpgradeTime(firmwareProto.getUpgradeTime()).
                setCurrentPackage(toFirmwarePackagePojo(firmwareProto.getCurrentPackage())).
                build();
    }

    public static UpgradeProto.Firmware toFirmwareProto(Firmware firmware) {
        return UpgradeProto.Firmware.newBuilder().
                setName(firmware.getName()).
                setVersion(firmware.getVersion()).
                setUpgradeTime(firmware.getUpgradeTime()).
                setCurrentPackage(totoFirmwarePackageProto(firmware.getCurrentPackage())).
                build();
    }

    public static FirmwarePackage
    toFirmwarePackagePojo(UpgradeProto.FirmwarePackage firmwarePackageProto) {
        return new FirmwarePackage.Builder(
                firmwarePackageProto.getName(),
                firmwarePackageProto.getVersion()).
                setGroup(firmwarePackageProto.getGroup()).
                setForced(firmwarePackageProto.getForced()).
                setIncremental(firmwarePackageProto.getIncremental()).
                setPackageUrl(firmwarePackageProto.getPackageUrl()).
                setPackageMd5(firmwarePackageProto.getPackageMd5()).
                setIncrementUrl(firmwarePackageProto.getIncrementUrl()).
                setIncrementMd5(firmwarePackageProto.getIncrementMd5()).
                setReleaseTime(firmwarePackageProto.getReleaseTime()).
                setReleaseNote(firmwarePackageProto.getReleaseNote()).
                setLocalFile(firmwarePackageProto.getLocalFile()).
                build();
    }

    public static UpgradeProto.FirmwarePackage
    totoFirmwarePackageProto(FirmwarePackage firmwarePackage) {
        return UpgradeProto.FirmwarePackage.newBuilder().
                setName(firmwarePackage.getName()).
                setVersion(firmwarePackage.getVersion()).
                setGroup(firmwarePackage.getGroup()).
                setForced(firmwarePackage.isForced()).
                setIncremental(firmwarePackage.isIncremental()).
                setPackageUrl(firmwarePackage.getPackageUrl()).
                setPackageMd5(firmwarePackage.getPackageMd5()).
                setIncrementUrl(firmwarePackage.getIncrementUrl()).
                setIncrementMd5(firmwarePackage.getIncrementMd5()).
                setReleaseTime(firmwarePackage.getReleaseTime()).
                setReleaseNote(firmwarePackage.getReleaseNote()).
                setLocalFile(firmwarePackage.getLocalFile()).
                build();
    }

    public static UpgradeProto.FirmwareList toFirmwareListProto(List<Firmware> firmwareList) {
        UpgradeProto.FirmwareList.Builder builder = UpgradeProto.FirmwareList.newBuilder();
        for (Firmware firmware : firmwareList) {
            builder.addFirmware(toFirmwareProto(firmware));
        }

        return builder.build();
    }
}
