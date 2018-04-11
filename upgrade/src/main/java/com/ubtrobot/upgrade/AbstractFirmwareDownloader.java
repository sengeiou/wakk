package com.ubtrobot.upgrade;

import android.os.Handler;

import com.ubtrobot.async.Consumer;
import com.ubtrobot.async.ListenerList;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.util.HashMap;

public abstract class AbstractFirmwareDownloader implements FirmwareDownloader {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("AbstractFirmwareDownloader");

    private final Handler mHandler;

    private volatile int mState = FirmwareDownloader.STATE_IDLE;

    private volatile long mTotalBytes;
    private volatile long mDownloadedBytes;
    private volatile int mSpeed;

    private final ListenerList<StateListener> mStateListeners;
    private final ListenerList<ProgressListener> mProgressListeners;

    protected AbstractFirmwareDownloader(Handler handler) {
        mHandler = handler;

        mStateListeners = new ListenerList<>(handler);
        mProgressListeners = new ListenerList<>(handler);
    }

    protected Handler getHandler() {
        return mHandler;
    }

    @Override
    public long totalBytes() {
        return mTotalBytes;
    }

    protected void setTotalBytes(long totalBytes) {
        mTotalBytes = totalBytes;
    }

    @Override
    public long downloadedBytes() {
        return mDownloadedBytes;
    }

    protected void setDownloadedBytes(long downloadedBytes) {
        mDownloadedBytes = downloadedBytes;
    }

    @Override
    public int speed() {
        return mSpeed;
    }

    protected void setSpeed(int speed) {
        mSpeed = speed;
    }

    @Override
    public int state() {
        return mState;
    }

    protected void setState(int state) {
        mState = state;
    }

    @Override
    public boolean isIdle() {
        return mState == STATE_IDLE;
    }

    @Override
    public boolean isReady() {
        return mState == STATE_READY;
    }

    @Override
    public boolean isDownloading() {
        return mState == STATE_DOWNLOADING;
    }

    @Override
    public boolean isComplete() {
        return mState == STATE_COMPLETE;
    }

    @Override
    public boolean isError() {
        return mState == STATE_ERROR;
    }

    @Override
    public boolean downloadFor(FirmwarePackageGroup packageGroup) {
        if (packageGroup == null) {
            throw new IllegalArgumentException("Argument packageGroup is null.");
        }

        if (isIdle()) {
            return false;
        }

        FirmwarePackageGroup downloading = packageGroup();
        if (!downloading.getName().equals(packageGroup.getName())) {
            return false;
        }
        if (downloading.isForced() != packageGroup.isForced()) {
            return false;
        }
        if (downloading.getPackageCount() != packageGroup.getPackageCount()) {
            return false;
        }

        HashMap<String, FirmwarePackage> packageMap = new HashMap<>();
        for (FirmwarePackage firmwarePackage : downloading) {
            if (packageMap.put(firmwarePackage.getName(), firmwarePackage) != null) {
                throw new IllegalStateException("Repeated firmware package name. name=" +
                        firmwarePackage.getName());
            }
        }

        for (FirmwarePackage firmwarePackage : packageGroup) {
            FirmwarePackage downloadingPackage = packageMap.get(firmwarePackage.getName());
            if (downloadingPackage == null) {
                return false;
            }

            if (!downloadingPackage.getGroup().equals(firmwarePackage.getGroup())) {
                return false;
            }
            if (!downloadingPackage.getVersion().equals(firmwarePackage.getVersion())) {
                return false;
            }
            if (downloadingPackage.isForced() != firmwarePackage.isForced()) {
                return false;
            }
            if (downloadingPackage.isIncremental() != firmwarePackage.isIncremental()) {
                return false;
            }
            if (!downloadingPackage.getPackageUrl().equals(firmwarePackage.getPackageUrl())) {
                return false;
            }
            if (!downloadingPackage.getPackageMd5().equals(firmwarePackage.getPackageMd5())) {
                return false;
            }
            if (!downloadingPackage.getIncrementUrl().equals(firmwarePackage.getIncrementUrl())) {
                return false;
            }
            if (!downloadingPackage.getIncrementMd5().equals(firmwarePackage.getIncrementMd5())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void registerStateListener(StateListener listener) {
        mStateListeners.register(listener);
    }

    protected boolean isAnyStateListenerRegistered() {
        return !mStateListeners.isEmpty();
    }

    @Override
    public void unregisterStateListener(StateListener listener) {
        mStateListeners.unregister(listener);
    }

    protected void notifyStateChange(final int state, final DownloadException e) {
        if (state < STATE_IDLE || state > STATE_ERROR) {
            throw new IllegalArgumentException("Illegal state value. state=" + state);
        }

        if (state == STATE_ERROR && e == null) {
            throw new IllegalArgumentException("Argument e is null when state is " + state);
        }

        synchronized (this) {
            if (mState == state) {
                LOGGER.w("Notify state change, but the state not changed. state=%s", state);
                return;
            }

            mState = state;
            mStateListeners.forEach(new Consumer<StateListener>() {
                @Override
                public void accept(StateListener listener) {
                    listener.onStateChange(AbstractFirmwareDownloader.this, state, e);
                }
            });
        }
    }

    @Override
    public void registerProgressListener(ProgressListener listener) {
        mProgressListeners.register(listener);
    }

    protected boolean isAnyProgressListenerRegistered() {
        return !mProgressListeners.isEmpty();
    }

    @Override
    public void unregisterProgressListener(ProgressListener listener) {
        mProgressListeners.unregister(listener);
    }

    protected void notifyProgressChange(final long downloadedBytes, final int speed) {
        mProgressListeners.forEach(new Consumer<ProgressListener>() {
            @Override
            public void accept(ProgressListener listener) {
                listener.onProgressChange(AbstractFirmwareDownloader.this, downloadedBytes, speed);
            }
        });
    }
}