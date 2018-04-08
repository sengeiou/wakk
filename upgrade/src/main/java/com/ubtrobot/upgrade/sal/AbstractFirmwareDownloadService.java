package com.ubtrobot.upgrade.sal;

import android.os.Handler;
import android.os.Looper;

import com.ubtrobot.async.Promise;
import com.ubtrobot.upgrade.AbstractFirmwareDownloader;
import com.ubtrobot.upgrade.DownloadException;
import com.ubtrobot.upgrade.FirmwarePackageGroup;
import com.ubtrobot.upgrade.sal.FirmwareDownloadService;

public class AbstractFirmwareDownloadService extends AbstractFirmwareDownloader
        implements FirmwareDownloadService {

    protected AbstractFirmwareDownloadService() {
        super(new Handler(Looper.getMainLooper()));
    }

    @Override
    public boolean downloadFor(FirmwarePackageGroup packageGroup) {
        return false;
    }

    @Override
    public Promise<Void, DownloadException, Void> ready(FirmwarePackageGroup packageGroup) {
        return null;
    }

    @Override
    public FirmwarePackageGroup packageGroup() {
        return null;
    }

    @Override
    public Promise<Void, DownloadException, Void> start() {
        return null;
    }

    @Override
    public Promise<Void, DownloadException, Void> pause() {
        return null;
    }

    @Override
    public Promise<Void, DownloadException, Void> clear() {
        return null;
    }
}
