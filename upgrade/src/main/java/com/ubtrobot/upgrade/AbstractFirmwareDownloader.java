package com.ubtrobot.upgrade;

import android.os.Handler;

import com.ubtrobot.async.Consumer;
import com.ubtrobot.async.ListenerList;

public abstract class AbstractFirmwareDownloader implements FirmwareDownloader {

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
    public boolean isPausing() {
        return mState == STATE_PAUSING;
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
    public void registerStateListener(StateListener listener) {
        mStateListeners.register(listener);
    }

    @Override
    public void unregisterStateListener(StateListener listener) {
        mStateListeners.unregister(listener);
    }

    protected void notifyStateChange(final int state, final DownloadException e) {
        mStateListeners.forEach(new Consumer<StateListener>() {
            @Override
            public void accept(StateListener listener) {
                listener.onStateChange(AbstractFirmwareDownloader.this, state, e);
            }
        });
    }

    @Override
    public void registerProgressListener(ProgressListener listener) {
        mProgressListeners.register(listener);
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