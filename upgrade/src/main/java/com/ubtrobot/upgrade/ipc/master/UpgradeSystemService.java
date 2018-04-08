package com.ubtrobot.upgrade.ipc.master;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.google.protobuf.Message;
import com.ubtrobot.async.Promise;
import com.ubtrobot.master.adapter.CallProcessAdapter;
import com.ubtrobot.master.adapter.ProtoCallProcessAdapter;
import com.ubtrobot.master.adapter.ProtoParamParser;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.master.service.MasterSystemService;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;
import com.ubtrobot.upgrade.DetectException;
import com.ubtrobot.upgrade.DownloadException;
import com.ubtrobot.upgrade.FirmwareDownloader;
import com.ubtrobot.upgrade.FirmwarePackageGroup;
import com.ubtrobot.upgrade.UpgradeException;
import com.ubtrobot.upgrade.UpgradeProgress;
import com.ubtrobot.upgrade.ipc.UpgradeConstants;
import com.ubtrobot.upgrade.ipc.UpgradeConverters;
import com.ubtrobot.upgrade.ipc.UpgradeProto;
import com.ubtrobot.upgrade.sal.AbstractFirmwareDownloadService;
import com.ubtrobot.upgrade.sal.AbstractUpgradeService;
import com.ubtrobot.upgrade.sal.FirmwareDownloadService;
import com.ubtrobot.upgrade.sal.UpgradeFactory;
import com.ubtrobot.upgrade.sal.UpgradeService;

public class UpgradeSystemService extends MasterSystemService {

    private UpgradeService mUpgradeService;

    private FirmwareDownloadService mDownloadService;
    private StateListener mStateListener = new StateListener();
    private ProgressListener mProgressListener = new ProgressListener();

    private ProtoCallProcessAdapter mCallProcessor;

    @Override
    protected void onServiceCreate() {
        Application application = getApplication();
        if (!(application instanceof UpgradeFactory)) {
            throw new IllegalStateException(
                    "Your application should implement UpgradeFactory interface.");
        }

        mUpgradeService = ((UpgradeFactory) application).createUpgradeService();
        if (mUpgradeService == null || !(mUpgradeService instanceof AbstractUpgradeService)) {
            throw new IllegalStateException("Your application 's createUpgradeService returns " +
                    "null or does not return a instance of AbstractUpgradeService.");
        }

        mDownloadService = ((UpgradeFactory) application).createFirmwareDownloadService();
        if (mDownloadService == null ||
                !(mDownloadService instanceof AbstractFirmwareDownloadService)) {
            throw new IllegalStateException("Your application 's createFirmwareDownloadService" +
                    " returns null or does not return a instance of AbstractFirmwareDownloadService.");
        }

        mDownloadService.registerStateListener(mStateListener);
        mDownloadService.registerProgressListener(mProgressListener);

        mCallProcessor = new ProtoCallProcessAdapter(new Handler(Looper.getMainLooper()));
    }

    @Call(path = UpgradeConstants.CALL_PATH_GET_FIRMWARE_LIST)
    public void onGetFirmwareList(Request request, Responder responder) {
        responder.respondSuccess(ProtoParam.create(
                UpgradeConverters.toFirmwareListProto(mUpgradeService.getFirmwareList())
        ));
    }

    @Call(path = UpgradeConstants.CALL_PATH_DETECT_UPGRADE)
    public void onDetectUpgrade(Request request, final Responder responder) {
        final UpgradeProto.DetectOption option = ProtoParamParser.parseParam(request,
                UpgradeProto.DetectOption.class, responder);
        if (option == null) {
            return;
        }

        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<FirmwarePackageGroup, DetectException, Void>() {
                    @Override
                    public Promise<FirmwarePackageGroup, DetectException, Void>
                    call() {
                        return mUpgradeService.detect(UpgradeConverters.toDetectOptionPojo(option));
                    }
                },
                new ProtoCallProcessAdapter.DFConverter<FirmwarePackageGroup, DetectException>() {
                    @Override
                    public Message convertDone(FirmwarePackageGroup group) {
                        return UpgradeConverters.toFirmwarePackageGroupProto(group);
                    }

                    @Override
                    public CallException convertFail(DetectException fail) {
                        return new CallException(fail.getCode(), fail.getMessage());
                    }
                }
        );
    }

    @Call(path = UpgradeConstants.CALL_PATH_GET_FIRMWARE_PACKAGE_DOWNLOAD_STATE)
    public void onGetDownloadState(Request request, Responder responder) {
        responder.respondSuccess(ProtoParam.create(UpgradeConverters.toDownloadStateProto(
                mDownloadService.state())));
    }

    @Call(path = UpgradeConstants.CALL_PATH_GET_FIRMWARE_PACKAGE_DOWNLOAD_PROGRESS)
    public void onGetDownloadProgress(Request request, Responder responder) {
        responder.respondSuccess(ProtoParam.create(UpgradeConverters.toDownloadProgressProto(
                mDownloadService.totalBytes(),
                mDownloadService.downloadedBytes(),
                mDownloadService.speed()
        )));
    }

    @Call(path = UpgradeConstants.CALL_PATH_GET_DOWNLOADING_FIRMWARE_PACKAGE_GROUP)
    public void onGetDownloading(Request request, Responder responder) {
        responder.respondSuccess(ProtoParam.create(UpgradeConverters.toFirmwarePackageGroupProto(
                mDownloadService.packageGroup())));
    }

    @Call(path = UpgradeConstants.CALL_PATH_READY_FIRMWARE_PACKAGE_DOWNLOAD)
    public void onReadyFirmwarePackageDownload(Request request, Responder responder) {
        final UpgradeProto.FirmwarePackageGroup packageGroup = ProtoParamParser.parseParam(request,
                UpgradeProto.FirmwarePackageGroup.class, responder);
        if (packageGroup == null) {
            return;
        }

        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<Void, DownloadException, Void>() {
                    @Override
                    public Promise<Void, DownloadException, Void> call() throws CallException {
                        return mDownloadService.ready(UpgradeConverters.
                                toFirmwarePackageGroupPojo(packageGroup));
                    }
                },
                new DownloadConverter()
        );
    }

    @Call(path = UpgradeConstants.CALL_PATH_START_FIRMWARE_PACKAGE_DOWNLOAD)
    public void onStartFirmwarePackageDownload(Request request, Responder responder) {
        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<Void, DownloadException, Void>() {
                    @Override
                    public Promise<Void, DownloadException, Void> call() throws CallException {
                        return mDownloadService.start();
                    }
                },
                new DownloadConverter()
        );
    }

    @Call(path = UpgradeConstants.CALL_PATH_PAUSE_FIRMWARE_PACKAGE_DOWNLOAD)
    public void onPauseFirmwarePackageDownload(Request request, Responder responder) {
        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<Void, DownloadException, Void>() {
                    @Override
                    public Promise<Void, DownloadException, Void> call() throws CallException {
                        return mDownloadService.pause();
                    }
                },
                new DownloadConverter()
        );
    }

    @Call(path = UpgradeConstants.CALL_PATH_CLEAR_FIRMWARE_PACKAGE_DOWNLOAD)
    public void onClearFirmwarePackageDownload(Request request, Responder responder) {
        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<Void, DownloadException, Void>() {
                    @Override
                    public Promise<Void, DownloadException, Void> call() throws CallException {
                        return mDownloadService.clear();
                    }
                },
                new DownloadConverter()
        );
    }

    @Call(path = UpgradeConstants.CALL_PATH_UPGRADE_FIRMWARE)
    public void onUgradeFirmware(Request request, Responder responder) {
        final UpgradeProto.FirmwarePackageGroup packageGroup = ProtoParamParser.parseParam(request,
                UpgradeProto.FirmwarePackageGroup.class, responder);
        if (packageGroup == null) {
            return;
        }

        mCallProcessor.onCall(
                responder,
                new CallProcessAdapter.Callable<Void, UpgradeException, UpgradeProgress>() {
                    @Override
                    public Promise<Void, UpgradeException, UpgradeProgress>
                    call() throws CallException {
                        return mUpgradeService.upgrade(UpgradeConverters.
                                toFirmwarePackageGroupPojo(packageGroup));
                    }
                },
                new ProtoCallProcessAdapter.DFPConverter<Void, UpgradeException, UpgradeProgress>() {
                    @Override
                    public Message convertDone(Void done) {
                        return null;
                    }

                    @Override
                    public CallException convertFail(UpgradeException ue) {
                        return new CallException(ue.getCode(), ue.getMessage());
                    }

                    @Override
                    public Message convertProgress(UpgradeProgress progress) {
                        return UpgradeConverters.toUpgradeProgressProto(progress);
                    }
                }
        );
    }

    @Override
    protected void onServiceDestroy() {
        mDownloadService.unregisterStateListener(mStateListener);
        mDownloadService.unregisterProgressListener(mProgressListener);
    }

    private class StateListener implements FirmwareDownloader.StateListener {

        @Override
        public void onStateChange(FirmwareDownloader downloader, int state, DownloadException e) {
            publish(
                    UpgradeConstants.ACTION_FIRMWARE_DOWNLOAD_STATE_CHANGE,
                    ProtoParam.create(UpgradeConverters.toDownloadStateProto(state, e))
            );
        }
    }

    private class ProgressListener implements FirmwareDownloader.ProgressListener {

        @Override
        public void
        onProgressChange(FirmwareDownloader downloader, long downloadedBytes, int speed) {
            publish(
                    UpgradeConstants.ACTION_FIRMWARE_DOWNLOAD_PROGRESS_CHANGE,
                    ProtoParam.create(UpgradeConverters.toDownloadProgressProto(
                            mDownloadService.totalBytes(),
                            downloadedBytes,
                            speed
                    ))
            );
        }
    }

    private static class DownloadConverter
            extends ProtoCallProcessAdapter.FConverter<DownloadException> {

        @Override
        public CallException convertFail(DownloadException e) {
            return new CallException(e.getCode(), e.getMessage());
        }
    }
}