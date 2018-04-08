package com.ubtrobot.upgrade.sal;

public interface UpgradeFactory {

    UpgradeService createUpgradeService();

    FirmwareDownloadService createFirmwareDownloadService();
}