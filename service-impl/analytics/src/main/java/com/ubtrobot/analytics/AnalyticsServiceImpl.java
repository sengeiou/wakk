package com.ubtrobot.analytics;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AnalyticsServiceImpl implements Analytics {

    private static final String TAG = "Analytics";

    private static final int COUNT_PER_SAVE = 5;
    private static final int COUNT_PER_REPORT = 8;

    private static final long SAVE_INTERVAL = TimeUnit.SECONDS.toNanos(60);
    private static final long REPORT_INTERVAL = TimeUnit.MINUTES.toNanos(3);
    private static final long FORBID_INTERVAL = TimeUnit.MINUTES.toNanos(5);

    private final byte[] mSaveLock = new byte[0];
    private final byte[] mReportLock = new byte[0];

    private volatile long mShouldSaveAt = System.nanoTime() + SAVE_INTERVAL;
    private volatile long mShouldReportAt = System.nanoTime() + REPORT_INTERVAL;
    private volatile long mForbidReportAt;

    private Executor mExecutor;
    private Strategy mStrategy;
    private MemEventStorage mMemStorage;
    private DiskEventStorage mDiskStorage;
    private EventReporter mReporter;
    private boolean mEnable = true;

    public AnalyticsServiceImpl(Context context, Executor executor,
                                String appId, String appKey, String deviceId) {
        if (context == null) {
            throw new IllegalArgumentException("Context is null.");
        }

        checkString("appId", appId);
        checkString("appKey", appKey);
        checkString("deviceId", deviceId);

        mExecutor = executor != null ? executor : Executors.newSingleThreadExecutor();

        mStrategy = Strategy.DEFAULT;
        mMemStorage = new MemEventStorage(new MemoryEventStorage());
        mReporter = new HttpReport(context.getApplicationContext(), appId, appKey, deviceId);
        mDiskStorage = new DiskEventStorage(context.getApplicationContext());
    }

    private void checkString(String stringName, String stringValue) {
        if (stringValue == null || stringValue.length() == 0) {
            throw new IllegalArgumentException("Argument is null:" + stringName);
        }
    }

    @Override
    public void enable(boolean enable) {
        mEnable = enable;
    }

    @Override
    public void setStrategy(Strategy strategy) {
        mStrategy = strategy;
    }

    @Override
    public Strategy getStrategy() {
        return mStrategy;
    }

    @Override
    public void recordEvent(Event event) {
        if (!mEnable) {
            return;
        }
        doRecordEvent(event);
    }

    private void doRecordEvent(Event event) {
        mMemStorage.writeEvent(event);

        if (!shouldSave() && !shouldReport()) {
            return;
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                saveEvents();
                if (shouldReport()) {
                    reportEvents();
                }
            }
        };

        mExecutor.execute(runnable);
    }

    private boolean shouldSave() {
        return mMemStorage.getEventCount() >= COUNT_PER_SAVE ||
                (System.nanoTime() - mShouldSaveAt >= 0);
    }

    private boolean shouldReport() {
        long now = System.nanoTime();

        return (now - mForbidReportAt > 0) && ((now - mShouldReportAt >= 0)
                || isEventCountDiskStorageLTReport());
    }

    private void saveEvents() {
        synchronized (mSaveLock) {
            while (mMemStorage.getEventCount() >= COUNT_PER_SAVE) {
                List<Event> events = mMemStorage.readEvents(COUNT_PER_SAVE);
                if (events.isEmpty()) {
                    return;
                }

                if (writeEventsDiskStorage(events)) {
                    mMemStorage.removeEvents(events.size());
                }
            }

            mShouldSaveAt = System.nanoTime() + SAVE_INTERVAL;
        }
    }

    private boolean writeEventsDiskStorage(List<Event> events) {
        boolean isWriteSuccess;
        try {
            mDiskStorage.writeEvents(events);
            isWriteSuccess = true;
        } catch (IOException e) {
            Log.w(TAG, "Save events to disk failed.");
            isWriteSuccess = false;
        }

        return isWriteSuccess;
    }

    private void reportEvents() {
        synchronized (mReportLock) {
            do {
                List<Event> events = getEventListDiskStorage();
                Log.i(TAG, "/--------上报-------");
                for (Event event : events) {
                    Log.i(TAG, event.toString());
                }
                Log.i(TAG, "---------------/");
                if (events.size() < COUNT_PER_REPORT) {
                    return;
                }

                boolean isReportEventsSuccess = reportEvents(events);
                if (!isReportEventsSuccess) {
                    mForbidReportAt = System.nanoTime() + FORBID_INTERVAL;
                    break;
                } else {
                    mForbidReportAt = 0;
                    removeEventsDiskStorage(events);
                }

                // TODO 上报设备信息

            } while (isEventCountDiskStorageLTReport());

            mShouldReportAt = System.nanoTime() + REPORT_INTERVAL;
        }
    }

    private boolean reportEvents(List<Event> events) {
        boolean isSuccess;

        try {
            mReporter.reportEvents(events);
            isSuccess = true;
        } catch (ReportException e) {
            if (e.causedByInternalServerError()) {
                Log.w(TAG, "Report(reportEvents) events failed due to server error.");
            }
            isSuccess = false;
        }

        return isSuccess;
    }

    private boolean isEventCountDiskStorageLTReport() {
        try {
            return mDiskStorage.getEventCount() >= COUNT_PER_REPORT;
        } catch (IOException e) {
            Log.w(TAG, "Report(getEventCount) events failed due to server error.");
        }
        return false;
    }

    private void removeEventsDiskStorage(List<Event> events) {
        try {
            mDiskStorage.removeEvents(events.size());
        } catch (IOException e) {
            Log.w(TAG, "Report(removeEvents) events failed due to server error.");
        }
    }

    private List<Event> getEventListDiskStorage() {
        List<Event> events = new ArrayList<>();
        try {
            events = mDiskStorage.readEvents(COUNT_PER_REPORT);
        } catch (IOException e) {
            Log.w(TAG, "Operate event disk storage failed.");
        }
        return events;
    }

    private static class MemEventStorage implements EventStorage {

        private final EventStorage mRealMemEventStorage;

        MemEventStorage(EventStorage realMemEventStorage) {
            mRealMemEventStorage = realMemEventStorage;
        }

        @Override
        public void writeEvent(Event event) {
            try {
                mRealMemEventStorage.writeEvent(event);
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Impossible(writeEvent) IOException on MemoryStorage", e);
            }
        }

        @Override
        public void writeEvents(List<Event> events) {
            try {
                mRealMemEventStorage.writeEvents(events);
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Impossible(writeEvents) IOException on MemoryStorage", e);
            }
        }

        @Override
        public int getEventCount() {
            try {
                return mRealMemEventStorage.getEventCount();
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Impossible(getEventCount) IOException on MemoryStorage", e);
            }
        }

        @Override
        public List<Event> readEvents(int count) {
            try {
                return mRealMemEventStorage.readEvents(count);
            } catch (IOException e) {
                throw new IllegalStateException("Impossible IOException on MemoryStorage", e);
            }
        }

        @Override
        public void removeEvents(int count) {
            try {
                mRealMemEventStorage.removeEvents(count);
            } catch (IOException e) {
                throw new IllegalStateException("Impossible IOException on MemoryStorage", e);
            }
        }
    }
}
