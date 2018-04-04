package com.ubtrobot.upgrade;

import com.ubtrobot.async.Promise;

/**
 * 固件下载器
 */
public interface FirmwareDownloader {

    int STATE_IDLE = 0;
    int STATE_READY = 1;
    int STATE_DOWNLOADING = 2;
    int STATE_PAUSING = 3;
    int STATE_COMPLETE = 4;
    int STATE_ERROR = 5;

    /**
     * 获取（多个）固件包总大小
     *
     * @return 总大小
     */
    long totalBytes();

    /**
     * 获取已下载的大小
     *
     * @return 已下载的大小
     */
    long downloadedBytes();

    /**
     * 获取下载速度
     *
     * @return 下载速度
     */
    int speed();

    /**
     * 获取下载状态
     *
     * @return 下载状态
     */
    int state();

    /**
     * 准备待下载的固件包组
     *
     * @param packageGroup 固件包组
     * @return 异步结果
     */
    Promise<Void, DownloadException, Void> ready(FirmwarePackageGroup packageGroup);

    /**
     * 获取在下载的固件包组
     *
     * @return 固件包组
     */
    FirmwarePackageGroup packageGroup();

    /**
     * 查询是否闲置
     *
     * @return 是否闲置
     */
    boolean isIdle();

    /**
     * 查询是否准备好下载
     *
     * @return 是否准备好下载
     */
    boolean isReady();

    /**
     * 查询是否正在下载
     *
     * @return 是否正在下载
     */
    boolean isDownloading();

    /**
     * 查询是否暂停中
     *
     * @return 是否暂停中
     */
    boolean isPausing();

    /**
     * 查询是否完成
     *
     * @return 是否完成
     */
    boolean isComplete();

    /**
     * 查询是否出错
     *
     * @return 是否出错
     */
    boolean isError();

    /**
     * 查询是否在为某个固件包组下载
     *
     * @param packageGroup 固件包组
     * @return 是否在为某个固件包组下载
     */
    boolean downloadFor(FirmwarePackageGroup packageGroup);

    /**
     * 启动或恢复下载
     *
     * @return 异步结果
     */
    Promise<Void, DownloadException, Void> start();

    /**
     * 暂停下载
     *
     * @return 异步结果
     */
    Promise<Void, DownloadException, Void> pause();

    /**
     * 清除下载
     *
     * @return 异步结果
     */
    Promise<Void, DownloadException, Void> clear();

    /**
     * 注册状态监听器
     *
     * @param listener 状态监听器
     */
    void registerStateListener(StateListener listener);

    /**
     * 移除注册状态监听器的注册
     *
     * @param listener 状态监听器
     */
    void unregisterStateListener(StateListener listener);

    /**
     * 注册进度监听器
     *
     * @param listener 进度监听器
     */
    void registerProgressListener(ProgressListener listener);

    /**
     * 移除注册进度监听器的注册
     *
     * @param listener 进度监听器
     */
    void unregisterProgressListener(ProgressListener listener);

    /**
     * 状态监听器
     */
    interface StateListener {

        void onStateChange(FirmwareDownloader downloader, int state, DownloadException e);
    }

    /**
     * 进度监听器
     */
    interface ProgressListener {

        void onProgressChange(FirmwareDownloader downloader, long downloadedBytes, int speed);
    }
}
