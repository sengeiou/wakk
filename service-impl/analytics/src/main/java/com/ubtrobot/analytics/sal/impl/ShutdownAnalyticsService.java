package com.ubtrobot.analytics.sal.impl;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ubtrobot.analytics.DiskEventStorage;
import com.ubtrobot.analytics.Event;
import com.ubtrobot.analytics.EventReporter;
import com.ubtrobot.analytics.HttpReport;
import com.ubtrobot.analytics.ReportException;
import com.ubtrobot.analytics.ipc.AnalyticsConstants;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ShutdownAnalyticsService {

    private static final int COUNT_REPORT_SHUTDOWN = 8;
    //    private static final long UPDATE_SHUTDOWN_DURATION = 1000 * 60 * 5;
    private static final long UPDATE_SHUTDOWN_DURATION = 1000 * 5;

    private final byte[] mReportLock = new byte[0];

    private DiskEventStorage mDiskStorage;
    private EventReporter mReporter;
    private Handler mShutdownHandler;

    public ShutdownAnalyticsService(Context context, String appId, String appKey, String deviceId) {
        if (context == null) {
            throw new IllegalArgumentException("Context is null.");
        }

        checkString("appId", appId);
        checkString("appKey", appKey);
        checkString("deviceId", deviceId);

        this.mDiskStorage = new DiskEventStorage(context.getApplicationContext());
        mReporter = new HttpReport(context.getApplicationContext(), appId, appKey, deviceId);

        mShutdownHandler = new Handler(Looper.getMainLooper());
        mShutdownHandler.post(new ShutdownRunnable());
    }

    private void checkString(String stringName, String stringValue) {
        if (stringValue == null || stringValue.length() == 0) {
            throw new IllegalArgumentException("Argument is null:" + stringName);
        }
    }

    public void reportShutdownEvent() {
        synchronized (mReportLock) {
            LinkedList<Event> events = new LinkedList<>();
            getEvents(events);
            while (events.size() > 0) {
                boolean isReportEventsSuccess = reportEvents(events);
                removeShutdownEvents(events);

                if (!isReportEventsSuccess) {
                    //  批量更新
                    try {
                        mDiskStorage.writeEvents(events);
                    } catch (IOException e) {
                        Log.e("ShutdownAnalytics", "Update shutdown event fail.");
                    }
                }

                events.clear();
                getEvents(events);
            }
        }
    }

    private void getEvents(LinkedList<Event> events) {
        LinkedList<Event> reportEvents = getEvents();
        Iterator<Event> iterator = reportEvents.iterator();
        while (iterator.hasNext()) {
            Event reportEvent = iterator.next();
            Map<String, String> customSegmentation = reportEvent.getCustomSegmentation();
            customSegmentation.put(AnalyticsConstants.IS_SHUTDOWN, "true");
            Event event = new Event.Builder(reportEvent.getEventId(), reportEvent.getCategory()).
                    toBuild(reportEvent).setCustomSegmentation(customSegmentation).build();
            events.add(event);
        }
    }

    private LinkedList<Event> getEvents() {
        LinkedList<Event> events = new LinkedList<>();
        try {
            events.addAll(mDiskStorage.readShutdownEvents(COUNT_REPORT_SHUTDOWN));
        } catch (IOException e) {
            Log.w("ShutdownAnalytics", "Operate shutdown event disk storage failed.");
        }

        return events;
    }

    private void removeShutdownEvents(List<Event> events) {
        long firstRecordedAt = events.get(0).getRecordedAt();
        long lastRecordedAt = events.get(events.size() - 1).getRecordedAt();
        mDiskStorage.removeEvents(AnalyticsConstants.SHUTDOWN_EVENT,
                Math.min(firstRecordedAt, lastRecordedAt),
                Math.max(firstRecordedAt, lastRecordedAt));
    }

    private boolean reportEvents(List<Event> events) {
        boolean isSuccess;

        try {
            mReporter.reportEvents(events);
            isSuccess = true;
        } catch (ReportException e) {
            if (e.causedByInternalServerError()) {
                Log.w("ShutdownAnalytics",
                        "Report(reportEvents) events failed due to server error.");
            }
            isSuccess = false;
        }

        return isSuccess;
    }

    private class ShutdownRunnable implements Runnable {

        @Override
        public void run() {
            long startTimeMillis = System.currentTimeMillis() - System.nanoTime() / 1000 / 1000;
            Map<String, String> segmentation = new HashMap<>();
            segmentation.put("startTimeMillis", String.valueOf(startTimeMillis / 1000));
            updateEvent(new Event.Builder(AnalyticsConstants.SHUTDOWN_EVENT_ID,
                    AnalyticsConstants.SHUTDOWN_EVENT).
                    setCustomSegmentation(segmentation).build());
            mShutdownHandler.postDelayed(this, UPDATE_SHUTDOWN_DURATION);
        }

        private void updateEvent(Event event) {
            try {
                mDiskStorage.updateEvent(event);
            } catch (IOException e) {
                Log.e("ShutdownAnalytics", "Update shutdown event fail.");
            }
        }
    }
}
