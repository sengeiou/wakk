package com.ubtrobot.upgrade;

import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;
import com.ubtrobot.upgrade.ipc.UpgradeConstants;
import com.ubtrobot.upgrade.ipc.UpgradeConverters;
import com.ubtrobot.upgrade.ipc.UpgradeProto;

import java.util.Collections;
import java.util.List;

public class FirmwareList {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("FirmwareList");

    private final ProtoCallAdapter mUpgradeService;

    public FirmwareList(ProtoCallAdapter upgradeService) {
        mUpgradeService = upgradeService;
    }

    public List<Firmware> all() {
        try {
            return UpgradeConverters.toFirmwareListPojo(mUpgradeService.syncCall(
                    UpgradeConstants.CALL_PATH_GET_FIRMWARE_LIST, UpgradeProto.FirmwareList.class
            ));
        } catch (CallException e) {
            LOGGER.e(e, "Framework error when getting the firmware list.");
            return Collections.emptyList();
        }
    }

    public Firmware get(String name) {
        for (Firmware firmware : all()) {
            if (firmware.getName().equals(name)) {
                return firmware;
            }
        }

        throw new FirmwareNotFoundException();
    }

    public static class FirmwareNotFoundException extends RuntimeException {
    }
}