package com.ubtrobot.upgrade.sal.impl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.reflect.TypeToken;
import com.liulishuo.okdownload.DownloadContext;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.SpeedCalculator;
import com.liulishuo.okdownload.core.breakpoint.BlockInfo;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.listener.DownloadListener4WithSpeed;
import com.liulishuo.okdownload.core.listener.assist.Listener4SpeedAssistExtend;
import com.ubtrobot.commons.ObjectStorage;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;
import com.ubtrobot.upgrade.DownloadException;
import com.ubtrobot.upgrade.FirmwarePackage;
import com.ubtrobot.upgrade.FirmwarePackageGroup;
import com.ubtrobot.upgrade.sal.AbstractFirmwareDownloadService;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PlatformDownloadService extends AbstractFirmwareDownloadService {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("PlatformDownloadService");

    private static final String SP_FIRMWARE_PACKAGE_DOWNLOAD = "firmware_package_download.prefs";
    private static final String KEY_FIRMWARE_PACKAGE_GROUP = "firmware_package_group";
    private static final String KEY_TASK_INFO_MAP = "task_info_map";

    private final ObjectStorage mObjectStorage;
    private FirmwarePackageGroup mFirmwarePackageGroup;

    private final String mDownloadDirectory;
    private DownloadContext mDownloadContext;
    private Map<String, TaskInfo> mTaskInfoMap;

    public PlatformDownloadService(Context context, String downloadDirectory) {
        mObjectStorage = new ObjectStorage(context.getSharedPreferences(SP_FIRMWARE_PACKAGE_DOWNLOAD,
                Context.MODE_PRIVATE));
        mDownloadDirectory = downloadDirectory;

        FirmwarePackageGroup group = groupLocked();
        if (group.getPackageCount() == 0) {
            setState(STATE_IDLE);
        } else {
            initDownloadContextLocked();

            int[] downloadedSize = new int[1];
            boolean allComplete = isAllTaskCompleteLocked(downloadedSize);

            setDownloadedBytes(downloadedSize[0]);

            if (allComplete) {
                setState(STATE_COMPLETE);
            } else {
                setState(STATE_READY);
            }
        }
    }

    private FirmwarePackageGroup groupLocked() {
        if (mFirmwarePackageGroup != null) {
            return mFirmwarePackageGroup;
        }

        mFirmwarePackageGroup = mObjectStorage.get(KEY_FIRMWARE_PACKAGE_GROUP,
                FirmwarePackageGroup.class);
        if (mFirmwarePackageGroup == null) {
            mFirmwarePackageGroup = FirmwarePackageGroup.DEFAULT;
        }

        return mFirmwarePackageGroup;
    }

    private void initDownloadContextLocked() {
        DownloadContext.Builder builder = new DownloadContext.QueueSet()
                .setParentPathFile(new File(mDownloadDirectory))
                .setMinIntervalMillisCallbackProcess(500)
                .commit();

        FirmwarePackageGroup group = groupLocked();
        for (FirmwarePackage firmwarePackage : group) {
            if (firmwarePackage.isIncremental()) {
                builder.bind(firmwarePackage.getIncrementUrl()).setTag(firmwarePackage.getName());
            } else {
                builder.bind(firmwarePackage.getPackageUrl()).setTag(firmwarePackage.getName());
            }
        }

        mDownloadContext = builder.build();
        setTotalBytes(group.getTotalSize());
    }

    private boolean isAllTaskCompleteLocked(int[] outDownloadedSize) {
        Map<String, TaskInfo> taskInfoMap = taskInfoMapLocked();
        for (DownloadTask downloadTask : downloadContextLocked().getTasks()) {
            TaskInfo taskInfo = taskInfoMap.get(downloadTask.getTag().toString());
            if (taskInfo == null) {
                return false;
            }

            outDownloadedSize[0] += taskInfo.downloadedSize;
            if (taskInfo.downloadedSize <= 0 || taskInfo.downloadedSize != taskInfo.totalSize) {
                return false;
            }
        }

        return true;
    }

    private DownloadContext downloadContextLocked() {
        if (mDownloadContext == null) {
            throw new AssertionError("mDownloadContext != null when use mDownloadContext");
        }

        return mDownloadContext;
    }

    private Map<String, TaskInfo> taskInfoMapLocked() {
        if (mTaskInfoMap != null) {
            return mTaskInfoMap;
        }

        mTaskInfoMap = mObjectStorage.get(KEY_TASK_INFO_MAP, new TypeToken<Map<String, TaskInfo>>() {
        });
        if (mTaskInfoMap == null) {
            mTaskInfoMap = new HashMap<>();
        }

        return mTaskInfoMap;
    }

    @Override
    protected void doReady(FirmwarePackageGroup packageGroup) {
        synchronized (this) {
            mObjectStorage.save(KEY_FIRMWARE_PACKAGE_GROUP, packageGroup);
            mFirmwarePackageGroup = packageGroup;

            mObjectStorage.remove(KEY_TASK_INFO_MAP);
            mTaskInfoMap = new HashMap<>();

            initDownloadContextLocked();
        }
    }

    @Override
    protected FirmwarePackageGroup doGetPackageGroup() {
        synchronized (this) {
            return mFirmwarePackageGroup == null ? FirmwarePackageGroup.DEFAULT : mFirmwarePackageGroup;
        }
    }

    @Override
    protected void doStart() {
        synchronized (this) {
            downloadContextLocked().startOnSerial(new OkDownloadListener());
        }
    }

    @Override
    protected void doStop() {
        synchronized (this) {
            downloadContextLocked().stop();
        }
    }

    @Override
    protected void doClear() {
        synchronized (this) {
            for (DownloadTask downloadTask : downloadContextLocked().getTasks()) {
                downloadTask.cancel();
            }

            for (TaskInfo taskInfo : taskInfoMapLocked().values()) {
                new File(taskInfo.filePath).delete();
            }

            mObjectStorage.remove(KEY_FIRMWARE_PACKAGE_GROUP);
            mFirmwarePackageGroup = null;

            mObjectStorage.remove(KEY_TASK_INFO_MAP);
            mTaskInfoMap = null;
        }
    }

    private static class TaskInfo {

        String tag;
        long downloadedSize;
        long totalSize;
        String filePath;

        TaskInfo(String tag, long downloadedSize, long totalSize, String filePath) {
            this.tag = tag;
            this.downloadedSize = downloadedSize;
            this.totalSize = totalSize;
            this.filePath = filePath;
        }
    }

    private class OkDownloadListener extends DownloadListener4WithSpeed {

        private LinkedList<EndCause> mEndCauses = new LinkedList<>();
        private ArrayList<Exception> mRealCauses = new ArrayList<>();

        @Override
        public void taskStart(@NonNull DownloadTask task) {
            // Ignore
        }

        @Override
        public void connectStart(
                @NonNull DownloadTask task,
                int blockIndex,
                @NonNull Map<String, List<String>> requestHeaderFields) {
            // Ignore
        }

        @Override
        public void connectEnd(
                @NonNull DownloadTask task,
                int blockIndex,
                int responseCode,
                @NonNull Map<String, List<String>> responseHeaderFields) {
            // Ignore
        }

        @Override
        public void infoReady(
                @NonNull DownloadTask task,
                @NonNull BreakpointInfo info,
                boolean fromBreakpoint,
                @NonNull Listener4SpeedAssistExtend.Listener4SpeedModel model) {
            LOGGER.i("Download task info ready. url=%s, totalLength=%s", task.getUrl(),
                    info.getTotalLength());

            synchronized (PlatformDownloadService.this) {
                saveFirmwarePackageLocalPathLocked(task, info);
                saveTaskInfoLocked(new TaskInfo(
                        task.getTag().toString(),
                        info.getTotalOffset(),
                        info.getTotalLength(),
                        info.getFile().getAbsolutePath()
                ));
            }
        }

        private void saveFirmwarePackageLocalPathLocked(DownloadTask task, BreakpointInfo info) {
            FirmwarePackageGroup group = groupLocked();
            FirmwarePackageGroup.Builder builder = new FirmwarePackageGroup.Builder(
                    group.getName()).setForced(group.isForced()).
                    setReleaseNote(group.getReleaseNote()).setReleaseTime(group.getReleaseTime());
            for (FirmwarePackage firmwarePackage : group) {
                if (firmwarePackage.getName().equals(task.getTag())) {
                    builder.addPackage(firmwarePackage.newBuilder().setLocalFile(
                            info.getFile().getAbsolutePath()).build());
                } else {
                    builder.addPackage(firmwarePackage);
                }
            }

            FirmwarePackageGroup newGroup = builder.build();
            mObjectStorage.save(KEY_FIRMWARE_PACKAGE_GROUP, builder.build());
            mFirmwarePackageGroup = newGroup;
        }

        private void saveTaskInfoLocked(TaskInfo taskInfo) {
            Map<String, TaskInfo> taskInfoMap = taskInfoMapLocked();
            taskInfoMap.put(taskInfo.tag, taskInfo);

            mObjectStorage.save(KEY_TASK_INFO_MAP, taskInfoMap);
        }

        private void saveDownloadedSizeLocked(String tag, boolean complete) {
            Map<String, TaskInfo> taskInfoMap = taskInfoMapLocked();
            TaskInfo taskInfo = taskInfoMap.get(tag);
            if (taskInfo == null) {
                LOGGER.e("Task info NOT found. tag=%s, complete=%s", tag, complete);
                return;
            }

            if (complete) {
                taskInfo.downloadedSize = taskInfo.totalSize;
            }
            mObjectStorage.save(KEY_TASK_INFO_MAP, taskInfoMap);
        }

        @Override
        public void progressBlock(
                @NonNull DownloadTask task,
                int blockIndex,
                long currentBlockOffset,
                @NonNull SpeedCalculator blockSpeed) {
            // Ignore
        }

        @Override
        public void progress(
                @NonNull DownloadTask task,
                long currentOffset,
                @NonNull SpeedCalculator taskSpeed) {
            long speed = taskSpeed.getInstantBytesPerSecondAndFlush();
            LOGGER.d("Download task progress. url=%s, offset=%s, speed=%s", task.getUrl(),
                    currentOffset, speed);

            synchronized (PlatformDownloadService.this) {
                long totalOffset = 0;
                for (TaskInfo taskInfo : taskInfoMapLocked().values()) {
                    if (task.getTag().equals(taskInfo.tag)) {
                        taskInfo.downloadedSize = currentOffset;
                    }

                    totalOffset += taskInfo.downloadedSize;
                }

                notifyProgressChange(totalOffset, (int) speed);
            }
        }

        @Override
        public void blockEnd(
                @NonNull DownloadTask task,
                int blockIndex,
                BlockInfo info,
                @NonNull SpeedCalculator blockSpeed) {
            // Ignore
        }

        @Override
        public void taskEnd(
                @NonNull DownloadTask task,
                @NonNull EndCause cause,
                @Nullable Exception realCause,
                @NonNull SpeedCalculator taskSpeed) {
            LOGGER.i(realCause, "Download task end. url=%s, endCause=%s", task.getUrl(), cause);

            synchronized (PlatformDownloadService.this) {
                saveDownloadedSizeLocked(task.getTag().toString(), EndCause.COMPLETED == cause);

                if (EndCause.CANCELED == cause) {
                    return;
                }

                mEndCauses.add(cause);
                mRealCauses.add(realCause);

                if (mEndCauses.size() < downloadContextLocked().getTasks().length) {
                    return;
                }

                for (int i = 0; i < mEndCauses.size(); i++) {
                    if (EndCause.COMPLETED == mEndCauses.get(i)) {
                        continue;
                    }

                    Exception exception = mRealCauses.get(i);
                    // TODO
                    notifyError(new DownloadException.Factory().internalError("TODO"));
                    return;
                }

                notifyComplete();
            }
        }
    }
}