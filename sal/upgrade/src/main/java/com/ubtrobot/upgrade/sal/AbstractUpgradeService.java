package com.ubtrobot.upgrade.sal;

import android.os.Handler;
import android.os.Looper;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.async.DefaultProgressivePromise;
import com.ubtrobot.async.DoneCallback;
import com.ubtrobot.async.FailCallback;
import com.ubtrobot.async.ProgressCallback;
import com.ubtrobot.async.ProgressiveAsyncTask;
import com.ubtrobot.async.ProgressivePromise;
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
    public Promise<FirmwarePackageGroup, DetectException> detect(DetectOption option) {
        if (option == null) {
            throw new IllegalArgumentException("Argument option is null.");
        }

        AsyncTask<FirmwarePackageGroup, DetectException> task;
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

    protected abstract AsyncTask<FirmwarePackageGroup, DetectException>
    createDetectingFromRemoteSourceTask(long timeoutMills);

    protected abstract AsyncTask<FirmwarePackageGroup, DetectException>
    createDetectingFromLocalSourceTask(String sourcePath, long timeoutMills);

    @SuppressWarnings("unchecked")
    @Override
    public ProgressivePromise<Void, UpgradeException, UpgradeProgress>
    upgrade(final FirmwarePackageGroup packageGroup) {
        if (packageGroup == null || packageGroup.getPackageCount() == 0) {
            throw new IllegalArgumentException("Argument packageGroup is null or packageGroup " +
                    "has no firmwarePackage.");
        }

        final DefaultProgressivePromise<Void, UpgradeException, UpgradeProgress> promise
                = new DefaultProgressivePromise<>();
        synchronized (this) {
            if (mUpgrading) {
                promise.reject(new UpgradeException.Factory().prohibitReentry(
                        "Prohibit reentry. Already upgrading."));
                return promise;
            }

            mUpgrading = true;
            upgrade(packageGroup, promise);
        }

        return promise.done(new DoneCallback<Void>() {
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

    private void upgrade(
            FirmwarePackageGroup packageGroup,
            DefaultProgressivePromise<Void, UpgradeException, UpgradeProgress> deferred) {
        HashMap<String, FirmwarePackage> packageMap = new HashMap<>();
        for (FirmwarePackage firmwarePackage : packageGroup) {
            if (packageMap.put(firmwarePackage.getName(), firmwarePackage) != null) {
                throw new IllegalArgumentException("Repeated firmware package name in the package group.");
            }
        }

        List<FirmwarePackage> upgradeQueue = new LinkedList<>();
        LinkedList<AsyncTask<Void, UpgradeException>> verifyingTasks = new LinkedList<>();
        HashMap<String, ProgressiveAsyncTask<Void, UpgradeException, UpgradeProgress>> installingTasks =
                new HashMap<>();

        for (Firmware firmware : getFirmwareList()) {
            FirmwarePackage firmwarePackage = packageMap.remove(firmware.getName());
            if (firmwarePackage == null) {
                continue;
            }

            upgradeQueue.add(firmwarePackage);

            AsyncTask<Void, UpgradeException> verifyingTask =
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
            ProgressiveAsyncTask<Void, UpgradeException, UpgradeProgress> installingTask =
                    createInstallingPackageTask(upgradeQueue, offset++);
            if (installingTask == null) {
                throw new IllegalStateException("createInstallingPackageTask returns null.");
            }

            installingTasks.put(firmwarePackage.getName(), installingTask);
        }

        verifyAndInstallPackages(verifyingTasks, upgradeQueue, installingTasks, deferred);
    }

    protected abstract AsyncTask<Void, UpgradeException>
    createVerifyingPackageTask(FirmwarePackage firmwarePackage);

    protected abstract ProgressiveAsyncTask<Void, UpgradeException, UpgradeProgress>
    createInstallingPackageTask(List<FirmwarePackage> firmwarePackages, int offset);

    private void verifyAndInstallPackages(
            final List<AsyncTask<Void, UpgradeException>> verifyingTasks,
            final List<FirmwarePackage> upgradeQueue,
            final Map<String, ProgressiveAsyncTask<Void, UpgradeException, UpgradeProgress>> installingTasks,
            final DefaultProgressivePromise<Void, UpgradeException, UpgradeProgress> deferred) {
        final int[] callbackCount = new int[1];
        final UpgradeException[] ue = new UpgradeException[1];

        deferred.report(new UpgradeProgress.Builder(UpgradeProgress.STATE_CHECKING_BEGAN).build());
        for (AsyncTask<Void, UpgradeException> task : verifyingTasks) {
            task.promise().done(new DoneCallback<Void>() {
                @Override
                public void onDone(Void result) {
                    if (++callbackCount[0] >= verifyingTasks.size()) {
                        if (ue[0] == null) {
                            deferred.report(new UpgradeProgress.Builder(
                                    UpgradeProgress.STATE_CHECKING_ENDED).build());
                            deferred.report(new UpgradeProgress.Builder(UpgradeProgress.
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
            final Map<String, ProgressiveAsyncTask<Void, UpgradeException, UpgradeProgress>> installingTasks,
            final DefaultProgressivePromise<Void, UpgradeException, UpgradeProgress> deferred) {
        if (offset >= upgradeQueue.size()) {
            deferred.report(new UpgradeProgress.Builder(UpgradeProgress.STATE_UPGRADING_ALL_ENDED).
                    build());
            deferred.resolve(null);
            return;
        }

        final FirmwarePackage firmwarePackage = upgradeQueue.get(offset);
        ProgressiveAsyncTask<Void, UpgradeException, UpgradeProgress> task = installingTasks.get(
                firmwarePackage.getName());
        if (task == null) {
            throw new AssertionError("task != nul");
        }

        deferred.report(new UpgradeProgress.Builder(UpgradeProgress.STATE_UPGRADING_SINGLE_BEGAN).
                setUpgradingFirmware(firmwarePackage.getName()).
                setUpgradingFirmwareOrder(offset + 1).build());

        task.promise(
        ).progress(new ProgressCallback<UpgradeProgress>() {
            @Override
            public void onProgress(UpgradeProgress progress) {
                deferred.report(new UpgradeProgress.Builder(
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
                deferred.report(new UpgradeProgress.Builder(
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