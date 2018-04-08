package com.ubtrobot.upgrade.sal;

import com.ubtrobot.upgrade.Firmware;

import java.util.List;

public interface UpgradeService {

    List<Firmware> getFirmwareList();
}
