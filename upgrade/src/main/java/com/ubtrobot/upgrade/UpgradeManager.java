package com.ubtrobot.upgrade;

import android.os.Handler;
import android.os.Looper;

import com.ubtrobot.async.Promise;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.master.service.ServiceProxy;
import com.ubtrobot.upgrade.ipc.UpgradeConstants;

import java.util.List;

/**
 * 升级管理器
 */
public class UpgradeManager {

    private final FirmwareList mFirmwareList;
    private final UpgradeDetector mDetector;

    public UpgradeManager(MasterContext masterContext) {
        ServiceProxy proxy = masterContext.createSystemServiceProxy(
                UpgradeConstants.SERVICE_NAME);
        Handler handler = new Handler(Looper.getMainLooper());
        ProtoCallAdapter upgradeService = new ProtoCallAdapter(proxy, handler);

        mFirmwareList = new FirmwareList(upgradeService);
        mDetector = new UpgradeDetector(upgradeService);
    }

    /**
     * 获取固件列表
     *
     * @return 固件列表
     */
    public List<Firmware> getFirmwareList() {
        return mFirmwareList.all();
    }

    /**
     * 获取某个固件
     *
     * @param name 固件名称
     * @return 固件
     */
    public Firmware getFirmware(String name) {
        return mFirmwareList.get(name);
    }

    /**
     * 检测升级（使用默认选项）
     *
     * @return 升级回调
     */
    public Promise<FirmwarePackageGroup, DetectException, Void> detectUpgrade() {
        return mDetector.detect();
    }

    /**
     * 检测升级
     *
     * @param option 检测选项
     * @return 升级回调
     */
    public Promise<FirmwarePackageGroup, DetectException, Void> detectUpgrade(DetectOption option) {
        return mDetector.detect(option);
    }

    /**
     * 获取固件下载器
     *
     * @return 固件下载器
     */
    public FirmwareDownloader firmwareDownloader() {
        // TODO
        return null;
    }

    /**
     * 执行升级
     *
     * @param packageGroup 固件包组
     * @return 升级回调
     */
    public Promise<Void, UpgradeException, UpgradeProgress>
    upgrade(FirmwarePackageGroup packageGroup) {
        return null;
    }
}