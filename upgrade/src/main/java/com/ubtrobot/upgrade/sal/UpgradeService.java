package com.ubtrobot.upgrade.sal;

import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.upgrade.DetectException;
import com.ubtrobot.upgrade.DetectOption;
import com.ubtrobot.upgrade.Firmware;
import com.ubtrobot.upgrade.FirmwarePackageGroup;
import com.ubtrobot.upgrade.UpgradeException;
import com.ubtrobot.upgrade.UpgradeProgress;

import java.util.List;

public interface UpgradeService {

    List<Firmware> getFirmwareList();

    Promise<FirmwarePackageGroup, DetectException> detect(DetectOption option);

    ProgressivePromise<Void, UpgradeException, UpgradeProgress>
    upgrade(FirmwarePackageGroup packageGroup);
}