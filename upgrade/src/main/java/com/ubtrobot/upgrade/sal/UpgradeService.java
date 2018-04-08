package com.ubtrobot.upgrade.sal;

import com.ubtrobot.async.Promise;
import com.ubtrobot.upgrade.DetectException;
import com.ubtrobot.upgrade.DetectOption;
import com.ubtrobot.upgrade.Firmware;
import com.ubtrobot.upgrade.FirmwarePackageGroup;

import java.util.List;

public interface UpgradeService {

    List<Firmware> getFirmwareList();

    Promise<FirmwarePackageGroup, DetectException, Void> detect(DetectOption option);
}
