package com.ubtrobot.upgrade.sal.impl;

import android.content.Context;
import android.os.RecoverySystem;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;
import com.ubtrobot.upgrade.FirmwarePackage;
import com.ubtrobot.upgrade.UpgradeException;
import com.ubtrobot.upgrade.UpgradeProgress;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;

public class AndroidInstallingPackageTask
        extends AsyncTask<Void, UpgradeException, UpgradeProgress> {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("AndroidInstallingPackageTask");

    private final Context mContext;
    private final FirmwarePackage mFirmwarePackage;
    private final Executor mExecutor;

    public AndroidInstallingPackageTask(
            Context context,
            FirmwarePackage firmwarePackage,
            Executor executor) {
        mContext = context;
        mFirmwarePackage = firmwarePackage;
        mExecutor = executor;
    }

    @Override
    protected void onStart() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    RecoverySystem.installPackage(mContext, new File(mFirmwarePackage.getLocalFile()));
                } catch (IOException e) {
                    UpgradeException ue = new UpgradeException.Factory().internalError(
                            "Upgrade android error. writing the recovery command file fails, " +
                                    "or if the reboot itself fails.", e);
                    LOGGER.e(ue);
                    reject(ue);
                }
            }
        });
    }
}
