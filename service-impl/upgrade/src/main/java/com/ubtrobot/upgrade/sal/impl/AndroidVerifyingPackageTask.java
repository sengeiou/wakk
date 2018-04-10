package com.ubtrobot.upgrade.sal.impl;

import android.os.RecoverySystem;

import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;
import com.ubtrobot.upgrade.FirmwarePackage;
import com.ubtrobot.upgrade.UpgradeException;
import com.ubtrobot.upgrade.sal.AbstractVerifyingPackageTask;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.Executor;

public class AndroidVerifyingPackageTask extends AbstractVerifyingPackageTask {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("AndroidVerifyingPackageTask");

    private final Executor mExecutor;

    public AndroidVerifyingPackageTask(FirmwarePackage firmwarePackage, Executor executor) {
        super(firmwarePackage);

        mExecutor = executor;
    }

    @Override
    protected void onStart() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    verifyPackageMd5();
                    RecoverySystem.verifyPackage(
                            new File(firmwarePackage().getLocalFile()), null, null);
                    resolve(null);
                } catch (UpgradeException e) {
                    reject(e);
                } catch (GeneralSecurityException e) {
                    UpgradeException ue = new UpgradeException.Factory().verifyPackageError(
                            "Verify android package error due to security reason.", e);

                    LOGGER.e(e);
                    reject(ue);
                } catch (IOException e) {
                    UpgradeException ue = new UpgradeException.Factory().internalError("Verify " +
                            "package md5 failed due to package file error or system " +
                            "permission problem.", e);

                    LOGGER.e(e);
                    reject(ue);
                }
            }
        });
    }
}
