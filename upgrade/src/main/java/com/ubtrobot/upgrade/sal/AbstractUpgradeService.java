package com.ubtrobot.upgrade.sal;

import android.os.Handler;
import android.os.Looper;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.async.CancelableAsyncTask;
import com.ubtrobot.async.Deferred;
import com.ubtrobot.async.DoneCallback;
import com.ubtrobot.async.FailCallback;
import com.ubtrobot.async.ProgressCallback;
import com.ubtrobot.async.Promise;
import com.ubtrobot.cache.CachedField;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;
import com.ubtrobot.upgrade.DetectException;
import com.ubtrobot.upgrade.DetectOption;
import com.ubtrobot.upgrade.Firmware;
import com.ubtrobot.upgrade.FirmwarePackage;
import com.ubtrobot.upgrade.FirmwarePackageGroup;
import com.ubtrobot.upgrade.UpgradeException;
import com.ubtrobot.upgrade.UpgradeProgress;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AbstractUpgradeService implements UpgradeService {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("AbstractUpgradeService");

    private final Handler mHandler;

    private final CachedField<List<Firmware>> mFirmwareList;
    private boolean mUpgrading;

    public AbstractUpgradeService() {
        mHandler = new Handler(Looper.getMainLooper());
        mFirmwareList = new CachedField<>(new CachedField.FieldGetter<List<Firmware>>() {
            @Override
            public List<Firmware> get() {
                List<Firmware> firmwareList = getFirmwareListOrderByUpgradeSequence();
                if (firmwareList == null || firmwareList.isEmpty()) {
                    throw new IllegalStateException("getFirmwareListOrderByUpgradeSequence returns null or " +
                            "return a empty list.");
                }

                return Collections.unmodifiableList(firmwareList);
            }
        });
    }

    @Override
    public List<Firmware> getFirmwareList() {
        return mFirmwareList.get();
    }

    protected abstract List<Firmware> getFirmwareListOrderByUpgradeSequence();

    @Override
    public Promise<FirmwarePackageGroup, DetectException, Void> detect(DetectOption option) {
        if (option == null) {
            throw new IllegalArgumentException("Argument option is null.");
        }

        AsyncTask<FirmwarePackageGroup, DetectException, Void> task;
        if (option.isRemoteOrLocalSource()) {
            task = createDetectingFromRemoteSourceTask(option.getTimeout());

            if (task == null) {
                throw new IllegalStateException("createDetectingFromRemoteSourceTask returns null.");
            }
        } else {
            task = createDetectingFromLocalSourceTask(option.getLocalSourcePath(), option.getTimeout());

            if (task == null) {
                throw new IllegalStateException("createDetectingFromLocalSourceTask returns null.");
            }
        }

        task.start();
        return task.promise();
    }

    protected abstract CancelableAsyncTask<FirmwarePackageGroup, DetectException, Void>
    createDetectingFromRemoteSourceTask(long timeoutMills);

    protected abstract CancelableAsyncTask<FirmwarePackageGroup, DetectException, Void>
    createDetectingFromLocalSourceTask(String sourcePath, long timeoutMills);

    @Override
    public Promise<Void, UpgradeException, UpgradeProgress>
    upgrade(FirmwarePackageGroup packageGroup) {
        if (packageGroup == null || packageGroup.getPackageCount() == 0) {
            throw new IllegalArgumentException("Argument packageGroup is null or packageGroup " +
                    "has no firmwarePackage.");
        }

        Deferred<Void, UpgradeException, UpgradeProgress> deferred = new Deferred<>(mHandler);
        synchronized (this) {
            if (mUpgrading) {
                deferred.reject(new UpgradeException.Factory().prohibitReentry("Prohibit reentry. " +
                        "Already upgrading."));
                return deferred.promise();
            }

            mUpgrading = true;
            upgrade(packageGroup, deferred);

            return deferred.promise(
            ).done(new DoneCallback<Void>() {
                @Override
                public void onDone(Void result) {
                    synchronized (AbstractUpgradeService.this) {
                        mUpgrading = false;
                    }
                }
            }).fail(new FailCallback<UpgradeException>() {
                @Override
                public void onFail(UpgradeException result) {
                    synchronized (AbstractUpgradeService.this) {
                        mUpgrading = false;
                    }
                }
            });
        }
    }

    private void upgrade(
            FirmwarePackageGroup packageGroup,
            Deferred<Void, UpgradeException, UpgradeProgress> deferred) {
        HashMap<String, FirmwarePackage> packageMap = new HashMap<>();
        for (FirmwarePackage firmwarePackage : packageGroup) {
            if (packageMap.put(firmwarePackage.getName(), firmwarePackage) != null) {
                throw new IllegalArgumentException("Repeated firmware package name in the package group.");
            }
        }

        List<FirmwarePackage> upgradeQueue = new LinkedList<>();
        LinkedList<AsyncTask<Void, UpgradeException, Void>> verifyingTasks = new LinkedList<>();
        HashMap<String, AsyncTask<Void, UpgradeException, UpgradeProgress>> installingTasks =
                new HashMap<>();

        for (Firmware firmware : getFirmwareList()) {
            FirmwarePackage firmwarePackage = packageMap.remove(firmware.getName());
            if (firmwarePackage == null) {
                continue;
            }

            upgradeQueue.add(firmwarePackage);

            AsyncTask<Void, UpgradeException, Void> verifyingTask =
                    createVerifyingPackageTask(firmwarePackage);
            if (verifyingTask == null) {
                throw new IllegalStateException("createVerifyingPackageTask returns null.");
            }

            verifyingTasks.add(verifyingTask);
        }

        if (!packageMap.isEmpty()) {
            throw new IllegalArgumentException("Unsupported firmware packages. packages=" +
                    packageMap.values());
        }

        upgradeQueue = Collections.unmodifiableList(upgradeQueue);
        int offset = 0;
        for (FirmwarePackage firmwarePackage : upgradeQueue) {
            AsyncTask<Void, UpgradeException, UpgradeProgress> installingTask =
                    createInstallingPackageTask(upgradeQueue, offset++);
            if (installingTask == null) {
                throw new IllegalStateException("createInstallingPackageTask returns null.");
            }

            installingTasks.put(firmwarePackage.getName(), installingTask);
        }

        verifyAndInstallPackages(verifyingTasks, upgradeQueue, installingTasks, deferred);
    }

    protected abstract AsyncTask<Void, UpgradeException, Void>
    createVerifyingPackageTask(FirmwarePackage firmwarePackage);

    protected abstract AsyncTask<Void, UpgradeException, UpgradeProgress>
    createInstallingPackageTask(List<FirmwarePackage> firmwarePackages, int offset);

    private void verifyAndInstallPackages(
            final List<AsyncTask<Void, UpgradeException, Void>> verifyingTasks,
            final List<FirmwarePackage> upgradeQueue,
            final Map<String, AsyncTask<Void, UpgradeException, UpgradeProgress>> installingTasks,
            final Deferred<Void, UpgradeException, UpgradeProgress> deferred) {
        final int[] callbackCount = new int[1];
        final UpgradeException[] ue = new UpgradeException[1];

        deferred.notify(new UpgradeProgress.Builder(UpgradeProgress.STATE_CHECKING_BEGAN).build());
        for (AsyncTask<Void, UpgradeException, Void> task : verifyingTasks) {
            task.promise().done(new DoneCallback<Void>() {
                @Override
                public void onDone(Void result) {
                    if (++callbackCount[0] >= verifyingTasks.size()) {
                        if (ue[0] == null) {
                            deferred.notify(new UpgradeProgress.Builder(
                                    UpgradeProgress.STATE_CHECKING_ENDED).build());
                            deferred.notify(new UpgradeProgress.Builder(UpgradeProgress.
                                    STATE_UPGRADING_ALL_BEGAN).build());

                            installPackages(upgradeQueue, 0, installingTasks, deferred);
                        } else {
                            deferred.reject(ue[0]);
                        }
                    }
                }
            }).fail(new FailCallback<UpgradeException>() {
                @Override
                public void onFail(UpgradeException e) {
                    if (ue[0] == null) {
                        ue[0] = e;
                    }

                    if (++callbackCount[0] >= verifyingTasks.size()) {
                        deferred.reject(ue[0]);
                    }
                }
            });

            task.start();
        }
    }

    private void installPackages(
            final List<FirmwarePackage> upgradeQueue,
            final int offset,
            final Map<String, AsyncTask<Void, UpgradeException, UpgradeProgress>> installingTasks,
            final Deferred<Void, UpgradeException, UpgradeProgress> deferred) {
        if (offset >= upgradeQueue.size()) {
            deferred.notify(new UpgradeProgress.Builder(UpgradeProgress.STATE_UPGRADING_ALL_ENDED).
                    build());
            deferred.resolve(null);
            return;
        }

        final FirmwarePackage firmwarePackage = upgradeQueue.get(offset);
        AsyncTask<Void, UpgradeException, UpgradeProgress> task = installingTasks.get(
                firmwarePackage.getName());
        if (task == null) {
            throw new AssertionError("task != nul");
        }

        deferred.notify(new UpgradeProgress.Builder(UpgradeProgress.STATE_UPGRADING_SINGLE_BEGAN).
                setUpgradingFirmware(firmwarePackage.getName()).
                setUpgradingFirmwareOrder(offset + 1).build());

        task.promise(
        ).progress(new ProgressCallback<UpgradeProgress>() {
            @Override
            public void onProgress(UpgradeProgress progress) {
                deferred.notify(new UpgradeProgress.Builder(
                        UpgradeProgress.STATE_UPGRADING_SINGLE_IN_PROGRESS).
                        setUpgradingFirmware(firmwarePackage.getName()).
                        setUpgradingFirmwareOrder(offset + 1).
                        setDescription(progress.getDescription()).
                        setUpgradingSingleProgress(progress.getUpgradingSingleProgress()).
                        setUpgradingAllProgress(progress.getUpgradingAllProgress()).
                        setRemainingSeconds(progress.getRemainingSeconds()).
                        setWillRoboot(progress.willRoboot()).
                        build());
            }
        }).done(new DoneCallback<Void>() {
            @Override
            public void onDone(Void result) {
                deferred.notify(new UpgradeProgress.Builder(
                        UpgradeProgress.STATE_UPGRADING_SINGLE_ENDED).
                        setUpgradingFirmware(firmwarePackage.getName()).
                        setUpgradingFirmwareOrder(offset + 1).build());

                installPackages(upgradeQueue, offset + 1, installingTasks, deferred);
            }
        }).fail(new FailCallback<UpgradeException>() {
            @Override
            public void onFail(UpgradeException ue) {
                LOGGER.e(ue);
                deferred.reject(new UpgradeException.Factory().executingUpgradeError(
                        "Executing upgrade error. " + ue.getMessage(), ue.getCause()
                ));
            }
        });

        task.start();
    }
}