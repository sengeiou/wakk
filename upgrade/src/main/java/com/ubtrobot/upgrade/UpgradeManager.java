package com.ubtrobot.upgrade;

import com.ubtrobot.async.Promise;
import com.ubtrobot.master.context.MasterContext;

import java.util.List;

/**
 * 升级管理器
 */
public class UpgradeManager {

    public UpgradeManager(MasterContext masterContext) {
    }

    /**
     * 获取固件列表
     *
     * @return 固件列表
     */
    public List<Firmware> getFirmwareList() {
        // TODO
        return null;
    }

    /**
     * 获取某个固件
     *
     * @param name 固件名称
     * @return 固件
     */
    public Firmware getFirmware(String name) {
        // TODO
        return null;
    }

    /**
     * 检测升级（使用默认选项）
     *
     * @return 升级回调
     */
    public Promise<FirmwarePackageGroup, DetectException, Void> detectUpgrade() {
        // TODO
        return null;
    }

    /**
     * 检测升级
     *
     * @param option 检测选项
     * @return 升级回调
     */
    public Promise<FirmwarePackageGroup, DetectException, Void> detectUpgrade(DetectOption option) {
        // TODO
        return null;
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