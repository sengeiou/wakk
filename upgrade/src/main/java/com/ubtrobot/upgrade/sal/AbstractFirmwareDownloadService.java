package com.ubtrobot.upgrade.sal;

import android.os.Handler;
import android.os.Looper;

import com.ubtrobot.async.Deferred;
import com.ubtrobot.async.Promise;
import com.ubtrobot.cache.CachedField;
import com.ubtrobot.upgrade.AbstractFirmwareDownloader;
import com.ubtrobot.upgrade.DownloadException;
import com.ubtrobot.upgrade.FirmwarePackageGroup;

public abstract class AbstractFirmwareDownloadService extends AbstractFirmwareDownloader
        implements FirmwareDownloadService {

    private final CachedField<FirmwarePackageGroup> mPackageGroup;

    protected AbstractFirmwareDownloadService() {
        super(new Handler(Looper.getMainLooper()));
        mPackageGroup = new CachedField<>(new CachedField.FieldGetter<FirmwarePackageGroup>() {
            @Override
            public FirmwarePackageGroup get() {
                FirmwarePackageGroup packageGroup = doGetPackageGroup();
                if (packageGroup == null) {
                    packageGroup = new FirmwarePackageGroup.Builder("").build();
                }

                return packageGroup;
            }
        });

        if (mPackageGroup.get().getPackageCount() == 0) {
            setState(STATE_IDLE);
        } else {
            setState(STATE_READY);
        }
    }

    @Override
    public Promise<Void, DownloadException, Void> ready(FirmwarePackageGroup packageGroup) {
        if (packageGroup.getPackageCount() == 0) {
            throw new IllegalArgumentException("Illegal package group which has no firmware package.");
        }

        Deferred<Void, DownloadException, Void> deferred = new Deferred<>(getHandler());
        synchronized (this) {
            if (isIdle()) {
                doReady(packageGroup);
                mPackageGroup.clear();

                deferred.resolve(null);
                notifyStateChange(STATE_READY, null);
            } else {
                operationInIllegalState(deferred);
            }
        }

        return deferred.promise();
    }

    private void operationInIllegalState(Deferred<Void, DownloadException, Void> deferred) {
        deferred.reject(new DownloadException.Factory().internalError("")); // TODO
    }

    protected abstract void doReady(FirmwarePackageGroup packageGroup);

    @Override
    public FirmwarePackageGroup packageGroup() {
        synchronized (this) {
            return mPackageGroup.get();
        }
    }

    protected abstract FirmwarePackageGroup doGetPackageGroup();

    @Override
    public Promise<Void, DownloadException, Void> start() {
        Deferred<Void, DownloadException, Void> deferred = new Deferred<>(getHandler());

        synchronized (this) {
            if (isDownloading()) {
                deferred.resolve(null);
            } else if (isReady() || isPausing() || isError()) {
                doStart();

                deferred.resolve(null);
                notifyStateChange(STATE_DOWNLOADING, null);
            } else {
                operationInIllegalState(deferred);
            }
        }

        return deferred.promise();
    }

    protected abstract void doStart();

    @Override
    public Promise<Void, DownloadException, Void> pause() {
        Deferred<Void, DownloadException, Void> deferred = new Deferred<>(getHandler());

        synchronized (this) {
            if (isPausing()) {
                deferred.resolve(null);
            } else if (isDownloading()) {
                doPause();

                deferred.resolve(null);
                notifyStateChange(STATE_PAUSING, null);
            } else {
                operationInIllegalState(deferred);
            }
        }

        return deferred.promise();
    }

    protected abstract void doPause();

    @Override
    public Promise<Void, DownloadException, Void> clear() {
        Deferred<Void, DownloadException, Void> deferred = new Deferred<>(getHandler());

        synchronized (this) {
            if (isIdle()) {
                deferred.resolve(null);
                return deferred.promise();
            }

            if (isDownloading()) {
                doPause();
            }
            doClear();
            mPackageGroup.clear();

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
}
