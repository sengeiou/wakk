package com.ubtrobot.upgrade.sal;

import android.os.Handler;
import android.os.Looper;

import com.ubtrobot.async.Deferred;
import com.ubtrobot.async.Promise;
import com.ubtrobot.upgrade.AbstractFirmwareDownloader;
import com.ubtrobot.upgrade.DownloadException;
import com.ubtrobot.upgrade.DownloadOperationException;
import com.ubtrobot.upgrade.FirmwarePackageGroup;

public abstract class AbstractFirmwareDownloadService extends AbstractFirmwareDownloader
        implements FirmwareDownloadService {

    protected AbstractFirmwareDownloadService() {
        super(new Handler(Looper.getMainLooper()));
    }

    @Override
    public Promise<Void, DownloadOperationException, Void> ready(FirmwarePackageGroup packageGroup) {
        if (packageGroup.getPackageCount() == 0) {
            throw new IllegalArgumentException("Illegal package group which has no firmware package.");
        }

        Deferred<Void, DownloadOperationException, Void> deferred = new Deferred<>(getHandler());
        synchronized (this) {
            if (isIdle()) {
                doReady(packageGroup);

                deferred.resolve(null);
                notifyStateChange(STATE_READY, null);
            } else {
                deferred.reject(new DownloadOperationException.Factory().illegalOperation(
                        "Should not ready when not idle."));
            }
        }

        return deferred.promise();
    }

    protected abstract void doReady(FirmwarePackageGroup packageGroup);

    @Override
    public FirmwarePackageGroup packageGroup() {
        synchronized (this) {
            FirmwarePackageGroup group = doGetPackageGroup();
            return group == null ? FirmwarePackageGroup.DEFAULT : group;
        }
    }

    protected abstract FirmwarePackageGroup doGetPackageGroup();

    @Override
    public Promise<Void, DownloadOperationException, Void> start() {
        Deferred<Void, DownloadOperationException, Void> deferred = new Deferred<>(getHandler());

        synchronized (this) {
            if (isDownloading()) {
                deferred.resolve(null);
            } else if (isReady() || isError()) {
                doStart();

                deferred.resolve(null);
                notifyStateChange(STATE_DOWNLOADING, null);
            } else {
                deferred.reject(new DownloadOperationException.Factory().illegalOperation(
                        "Should not start when idle or complete."));
            }
        }

        return deferred.promise();
    }

    protected abstract void doStart();

    @Override
    public Promise<Void, DownloadOperationException, Void> stop() {
        Deferred<Void, DownloadOperationException, Void> deferred = new Deferred<>(getHandler());

        synchronized (this) {
            if (isReady()) {
                deferred.resolve(null);
            } else if (isDownloading()) {
                doStop();

                deferred.resolve(null);
                notifyStateChange(STATE_READY, null);
            } else {
                deferred.reject(new DownloadOperationException.Factory().illegalOperation(
                        "Should not stop when idle, complete or error."));
            }
        }

        return deferred.promise();
    }

    protected abstract void doStop();

    @Override
    public Promise<Void, DownloadOperationException, Void> clear() {
        Deferred<Void, DownloadOperationException, Void> deferred = new Deferred<>(getHandler());

        synchronized (this) {
            if (isIdle()) {
                deferred.resolve(null);
                return deferred.promise();
            }

            if (isDownloading()) {
                doStop();
            }
            doClear();

            deferred.resolve(null);
            notifyStateChange(STATE_IDLE, null);
        }

        return deferred.promise();
    }

    protected abstract void doClear();

    protected void notifyError(DownloadException e) {
        if (e == null) {
            throw new IllegalArgumentException("Argument e is null.");
        }

        notifyStateChange(STATE_ERROR, e);
    }

    protected void notifyComplete() {
        notifyStateChange(STATE_COMPLETE, null);
    }
}