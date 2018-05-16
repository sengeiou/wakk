package com.ubtrobot.upgrade.sal;

import android.os.Handler;
import android.os.Looper;

import com.ubtrobot.async.DefaultPromise;
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
    public Promise<Void, DownloadOperationException> ready(FirmwarePackageGroup packageGroup) {
        if (packageGroup.getPackageCount() == 0) {
            throw new IllegalArgumentException("Illegal package group which has no firmware package.");
        }

        DefaultPromise<Void, DownloadOperationException> promise = new DefaultPromise<>(getHandler());
        synchronized (this) {
            if (isIdle()) {
                doReady(packageGroup);

                promise.resolve(null);
                notifyStateChange(STATE_READY, null);
            } else {
                promise.reject(new DownloadOperationException.Factory().illegalOperation(
                        "Should not ready when not idle."));
            }
        }

        return promise;
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
    public Promise<Void, DownloadOperationException> start() {
        DefaultPromise<Void, DownloadOperationException> promise = new DefaultPromise<>(getHandler());

        synchronized (this) {
            if (isDownloading()) {
                promise.resolve(null);
            } else if (isReady() || isError()) {
                doStart();

                promise.resolve(null);
                notifyStateChange(STATE_DOWNLOADING, null);
            } else {
                promise.reject(new DownloadOperationException.Factory().illegalOperation(
                        "Should not start when idle or complete."));
            }
        }

        return promise;
    }

    protected abstract void doStart();

    @Override
    public Promise<Void, DownloadOperationException> stop() {
        DefaultPromise<Void, DownloadOperationException> promise = new DefaultPromise<>(getHandler());

        synchronized (this) {
            if (isReady()) {
                promise.resolve(null);
            } else if (isDownloading()) {
                doStop();

                promise.resolve(null);
                notifyStateChange(STATE_READY, null);
            } else {
                promise.reject(new DownloadOperationException.Factory().illegalOperation(
                        "Should not stop when idle, complete or error."));
            }
        }

        return promise;
    }

    protected abstract void doStop();

    @Override
    public Promise<Void, DownloadOperationException> clear() {
        DefaultPromise<Void, DownloadOperationException> promise = new DefaultPromise<>(getHandler());

        synchronized (this) {
            if (isIdle()) {
                promise.resolve(null);
                return promise;
            }

            if (isDownloading()) {
                doStop();
            }
            doClear();

            promise.resolve(null);
            notifyStateChange(STATE_IDLE, null);
        }

        return promise;
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