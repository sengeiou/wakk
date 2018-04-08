package com.ubtrobot.upgrade;

import com.ubtrobot.async.Promise;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.upgrade.ipc.UpgradeConstants;
import com.ubtrobot.upgrade.ipc.UpgradeConverters;
import com.ubtrobot.upgrade.ipc.UpgradeProto;

/**
 * 升级检测者
 */
public class UpgradeDetector {

    private final ProtoCallAdapter mUpgradeService;

    public UpgradeDetector(ProtoCallAdapter upgradeService) {
        mUpgradeService = upgradeService;
    }

    public Promise<FirmwarePackageGroup, DetectException, Void> detect() {
        return detect(new DetectOption.Builder(true).build());
    }

    public Promise<FirmwarePackageGroup, DetectException, Void> detect(DetectOption option) {
        if (option == null) {
            throw new IllegalArgumentException("Argument option is null.");
        }

        return mUpgradeService.call(
                UpgradeConstants.CALL_PATH_DETECT_UPGRADE,
                UpgradeConverters.toDetectOptionProto(option),
                new ProtoCallAdapter.DFProtoConverter<
                        FirmwarePackageGroup, UpgradeProto.FirmwarePackageGroup, DetectException>() {
                    @Override
                    public DetectException convertFail(CallException e) {
                        return new DetectException.Factory().from(e);
                    }

                    @Override
                    public Class<UpgradeProto.FirmwarePackageGroup> doneProtoClass() {
                        return UpgradeProto.FirmwarePackageGroup.class;
                    }

                    @Override
                    public FirmwarePackageGroup
                    convertDone(UpgradeProto.FirmwarePackageGroup packageGroup) {
                        return UpgradeConverters.toFirmwarePackageGroupPojo(packageGroup);
                    }
                }
        );
    }
}
