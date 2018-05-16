package com.ubtrobot.upgrade;

import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.upgrade.ipc.UpgradeConstants;
import com.ubtrobot.upgrade.ipc.UpgradeConverters;
import com.ubtrobot.upgrade.ipc.UpgradeProto;

public class Upgrader {

    private final ProtoCallAdapter mUpgradeService;

    public Upgrader(ProtoCallAdapter upgradeService) {
        mUpgradeService = upgradeService;
    }

    public ProgressivePromise<Void, UpgradeException, UpgradeProgress>
    upgrade(FirmwarePackageGroup packageGroup) {
        if (packageGroup == null) {
            throw new IllegalArgumentException("Argument packageGroup or option is null.");
        }

        return mUpgradeService.callStickily(
                UpgradeConstants.CALL_PATH_UPGRADE_FIRMWARE,
                UpgradeConverters.toFirmwarePackageGroupProto(packageGroup),
                new ProtoCallAdapter.FPProtoConverter<
                        UpgradeException, UpgradeProgress, UpgradeProto.UpgradeProgress>() {
                    @Override
                    public UpgradeException convertFail(CallException e) {
                        return new UpgradeException.Factory().from(e);
                    }

                    @Override
                    public Class<UpgradeProto.UpgradeProgress> progressProtoClass() {
                        return UpgradeProto.UpgradeProgress.class;
                    }

                    @Override
                    public UpgradeProgress convertProgress(UpgradeProto.UpgradeProgress progress) {
                        return UpgradeConverters.toUpgradeProgressPojo(progress);
                    }
                }
        );
    }
}
