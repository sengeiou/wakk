package com.ubtrobot.upgrade.sal;

import android.text.TextUtils;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.commons.Md5Utils;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;
import com.ubtrobot.upgrade.FirmwarePackage;
import com.ubtrobot.upgrade.UpgradeException;

import java.io.IOException;

public abstract class AbstractVerifyingPackageTask extends
        AsyncTask<Void, UpgradeException> {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("AbstractVerifyingPackageTask");

    private final FirmwarePackage mFirmwarePackage;

    public AbstractVerifyingPackageTask(FirmwarePackage firmwarePackage) {
        if (firmwarePackage == null || TextUtils.isEmpty(firmwarePackage.getLocalFile())) {
            throw new IllegalArgumentException("Illegal firmware package. null or has no local file.");
        }

        if (firmwarePackage.isIncremental()) {
            if (firmwarePackage.getIncrementMd5() == null
                    || firmwarePackage.getIncrementMd5().length() != 32) {
                throw new IllegalArgumentException("Illegal firmware package. Illegal increment md5.");
            }
        } else {
            if (firmwarePackage.getPackageMd5() == null
                    || firmwarePackage.getPackageMd5().length() != 32) {
                throw new IllegalArgumentException("Illegal firmware package. Illegal package md5.");
            }
        }

        mFirmwarePackage = firmwarePackage;
    }

    protected FirmwarePackage firmwarePackage() {
        return mFirmwarePackage;
    }

    protected void verifyPackageMd5() throws UpgradeException {
        try {
            String md5 = Md5Utils.calculateFileMd5(mFirmwarePackage.getLocalFile());
            if (mFirmwarePackage.isIncremental()) {
                if (!md5.equalsIgnoreCase(mFirmwarePackage.getIncrementMd5())) {
                    throw new UpgradeException.Factory().verifyPackageError("Verify increment " +
                            "package md5 error. expected=" + mFirmwarePackage.getIncrementMd5() +
                            ", actual=" + md5);
                }
            } else {
                if (!md5.equalsIgnoreCase(mFirmwarePackage.getPackageMd5())) {
                    throw new UpgradeException.Factory().verifyPackageError("Verify package md5 " +
                            "error. expected=" + mFirmwarePackage.getPackageMd5() + ", actual=" +
                            md5);
                }
            }
        } catch (IOException e) {
            UpgradeException ue = new UpgradeException.Factory().internalError("Verify " +
                    "package md5 failed due to package file error or system permission problem.", e);
            LOGGER.e(ue);
            throw ue;
        }
    }
}
