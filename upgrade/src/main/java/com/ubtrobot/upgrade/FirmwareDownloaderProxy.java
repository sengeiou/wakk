package com.ubtrobot.upgrade;

import android.os.Handler;

import com.ubtrobot.async.Promise;
import com.ubtrobot.master.adapter.CallAdapter;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.adapter.ProtoEventReceiver;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;
import com.ubtrobot.upgrade.ipc.UpgradeConstants;
import com.ubtrobot.upgrade.ipc.UpgradeConverters;
import com.ubtrobot.upgrade.ipc.UpgradeProto;

public class FirmwareDownloaderProxy extends AbstractFirmwareDownloader {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("FirmwareDownloaderProxy");

    private final MasterContext mMasterContext;
    private final ProtoCallAdapter mUpgradeService;

    private boolean mStateSubscribed;
    private StateReceiver mStateReceiver;
    private boolean mProgressSubscribed;
    private ProgressReceiver mProgressReceiver;

    FirmwareDownloaderProxy(
            MasterContext masterContext,
            ProtoCallAdapter upgradeService,
            Handler handler) {
        super(handler);
        mMasterContext = masterContext;
        mUpgradeService = upgradeService;
    }

    @Override
    public long totalBytes() {
        synchronized (this) {
            if (!mProgressSubscribed) {
                setTotalBytes(getDownloadProgress().getTotalBytes());
            }

            return super.totalBytes();
        }
    }

    private UpgradeProto.DownloadProgress getDownloadProgress() {
        try {
            return mUpgradeService.syncCall(
                    UpgradeConstants.CALL_PATH_GET_FIRMWARE_PACKAGE_DOWNLOAD_PROGRESS,
                    UpgradeProto.DownloadProgress.class
            );
        } catch (CallException e) {
            LOGGER.e(e, "Framework error when getting the download progress.");
            return UpgradeProto.DownloadProgress.getDefaultInstance();
        }
    }

    @Override
    public long downloadedBytes() {
        synchronized (this) {
            if (!mProgressSubscribed) {
                setDownloadedBytes(getDownloadProgress().getDownloadedBytes());
            }

            return super.downloadedBytes();
        }
    }

    @Override
    public int speed() {
        synchronized (this) {
            if (!mProgressSubscribed) {
                setSpeed(getDownloadProgress().getSpeed());
            }

            return super.speed();
        }
    }

    @Override
    public int state() {
        synchronized (this) {
            if (!mStateSubscribed) {
                setState(getDownloadState().getState());
            }

            return super.state();
        }
    }

    private UpgradeProto.DownloadState getDownloadState() {
        try {
            return mUpgradeService.syncCall(
                    UpgradeConstants.CALL_PATH_GET_FIRMWARE_PACKAGE_DOWNLOAD_STATE,
                    UpgradeProto.DownloadState.class
            );
        } catch (CallException e) {
            LOGGER.e(e, "Framework error when getting the download state.");
            return UpgradeProto.DownloadState.newBuilder().setErrorCode(STATE_ERROR).build();
        }
    }

    @Override
    public void registerStateListener(StateListener listener) {
        super.registerStateListener(listener);

        synchronized (this) {
            if (isAnyStateListenerRegistered() && !mStateSubscribed) {
                mStateSubscribed = true;

                mStateReceiver = new StateReceiver();
                mMasterContext.subscribe(mStateReceiver,
                        UpgradeConstants.ACTION_FIRMWARE_DOWNLOAD_STATE_CHANGE);

                setState(getDownloadState().getState());
            }
        }
    }

    @Override
    public void unregisterStateListener(StateListener listener) {
        super.unregisterStateListener(listener);

        synchronized (this) {
            if (!isAnyStateListenerRegistered() && mStateSubscribed) {
                mStateSubscribed = false;

                mMasterContext.unsubscribe(mStateReceiver);
                mStateReceiver = null;
            }
        }
    }

    @Override
    public void registerProgressListener(ProgressListener listener) {
        super.registerProgressListener(listener);

        synchronized (this) {
            if (isAnyProgressListenerRegistered() && !mProgressSubscribed) {
                mProgressSubscribed = true;

                mProgressReceiver = new ProgressReceiver();
                mMasterContext.subscribe(mProgressReceiver,
                        UpgradeConstants.ACTION_FIRMWARE_DOWNLOAD_PROGRESS_CHANGE);

                UpgradeProto.DownloadProgress progress = getDownloadProgress();
                setTotalBytes(progress.getTotalBytes());
                setDownloadedBytes(progress.getDownloadedBytes());
                setSpeed(progress.getSpeed());
            }
        }
    }

    @Override
    public void unregisterProgressListener(ProgressListener listener) {
        super.unregisterProgressListener(listener);

        synchronized (this) {
            if (!isAnyProgressListenerRegistered() && mProgressSubscribed) {
                mProgressSubscribed = false;

                mMasterContext.unsubscribe(mProgressReceiver);
                mProgressReceiver = null;
            }
        }
    }

    @Override
    public Promise<Void, DownloadOperationException, Void> ready(FirmwarePackageGroup packageGroup) {
        return mUpgradeService.call(
                UpgradeConstants.CALL_PATH_READY_FIRMWARE_PACKAGE_DOWNLOAD,
                UpgradeConverters.toFirmwarePackageGroupProto(packageGroup),
                new DownloadConverter()
        );
    }

    @Override
    public FirmwarePackageGroup packageGroup() {
        try {
            return UpgradeConverters.toFirmwarePackageGroupPojo(mUpgradeService.syncCall(
                    UpgradeConstants.CALL_PATH_GET_DOWNLOADING_FIRMWARE_PACKAGE_GROUP,
                    UpgradeProto.FirmwarePackageGroup.class
            ));
        } catch (CallException e) {
            LOGGER.e(e, "Framework error when getting the downloading firmware package group.");
            return new FirmwarePackageGroup.Builder("").build();
        }
    }

    @Override
    public Promise<Void, DownloadOperationException, Void> start() {
        return mUpgradeService.call(
                UpgradeConstants.CALL_PATH_START_FIRMWARE_PACKAGE_DOWNLOAD,
                new DownloadConverter()
        );
    }

    @Override
    public Promise<Void, DownloadOperationException, Void> stop() {
        return mUpgradeService.call(
                UpgradeConstants.CALL_PATH_STOP_FIRMWARE_PACKAGE_DOWNLOAD,
                new DownloadConverter()
        );
    }

    @Override
    public Promise<Void, DownloadOperationException, Void> clear() {
        return mUpgradeService.call(
                UpgradeConstants.CALL_PATH_CLEAR_FIRMWARE_PACKAGE_DOWNLOAD,
                new DownloadConverter()
        );
    }

    private class StateReceiver extends ProtoEventReceiver<UpgradeProto.DownloadState> {

        @Override
        public void onReceive(
                MasterContext masterContext,
                String action,
                UpgradeProto.DownloadState state) {
            synchronized (FirmwareDownloaderProxy.this) {
                DownloadException e = null;
                if (state.getState() == STATE_ERROR) {
                    e = new DownloadException.Factory().from(
                            state.getErrorCode(), state.getErrorMessage());
                }

                notifyStateChange(state.getState(), e);
            }
        }
    }

    private class ProgressReceiver extends ProtoEventReceiver<UpgradeProto.DownloadProgress> {

        @Override
        public void onReceive(
                MasterContext masterContext,
                String action,
                UpgradeProto.DownloadProgress progress) {
            synchronized (FirmwareDownloaderProxy.this) {
                setTotalBytes(progress.getTotalBytes());
                setDownloadedBytes(progress.getDownloadedBytes());
                setSpeed(progress.getSpeed());

                notifyProgressChange(downloadedBytes(), speed());
            }
        }
    }

    private static class DownloadConverter extends CallAdapter.FConverter<DownloadOperationException> {

        @Override
        public DownloadOperationException convertFail(CallException e) {
            return new DownloadOperationException.Factory().from(e);
        }
    }
}
