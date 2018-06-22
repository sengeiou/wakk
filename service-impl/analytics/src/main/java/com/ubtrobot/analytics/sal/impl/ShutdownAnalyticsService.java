package com.ubtrobot.analytics.sal.impl;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ubtrobot.analytics.DiskEventStorage;
import com.ubtrobot.analytics.Event;
import com.ubtrobot.analytics.EventReporter;
import com.ubtrobot.analytics.EventTable;
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
            LinkedList<EventTable> events = new LinkedList<>();
            updateRanShutdown(events);
            while (events.size() > 0) {
                boolean isReportEventsSuccess = reportEvents(events);

                removeShutdownEvents(events);

                if (!isReportEventsSuccess) {
                    try {
                        mDiskStorage.writeEventTables(events);
                    } catch (IOException e) {
                        Log.e("ShutdownAnalytics", "Update shutdown event fail.");
                    }
                }

                events.clear();
                updateRanShutdown(events);
            }
        }
    }

    private void updateRanShutdown(LinkedList<EventTable> events) {
        try {
            events.addAll(mDiskStorage.readShutdownEvents(COUNT_REPORT_SHUTDOWN));
        } catch (IOException e) {
            Log.w("ShutdownAnalytics", "Operate shutdown event disk storage failed.");
        }

        Iterator<EventTable> iterator = events.iterator();
        while (iterator.hasNext()) {
            EventTable eventTable = iterator.next();
            eventTable.setRanShutdown(EventTable.RAN_SHUTDOWN);
        }
    }

    private void removeShutdownEvents(List<EventTable> events) {
        long firstRecordedAt = events.get(0).getEvent().getRecordedAt();
        long lastRecordedAt = events.get(events.size() - 1).getEvent().getRecordedAt();
        mDiskStorage.removeEvents(AnalyticsConstants.SHUTDOWN_EVENT,
                Math.min(firstRecordedAt, lastRecordedAt),
                Math.max(firstRecordedAt, lastRecordedAt));
    }

    private boolean reportEvents(List<EventTable> eventTables) {
        LinkedList<Event> events = new LinkedList<>();
        for (EventTable eventTable : eventTables) {
            events.add(eventTable.getEvent());
        }

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
            long duration = System.nanoTime() / 1000;
            updateEvent(new Event.Builder(AnalyticsConstants.SHUTDOWN_EVENT_ID,
                    AnalyticsConstants.SHUTDOWN_EVENT).
                    setDuration(duration).
                    build());
            mShutdownHandler.postDelayed(this, UPDATE_SHUTDOWN_DURATION);
        }

        private void updateEvent(Event event) {
            try {
                mDiskStorage.updateShutdownEventDuration(event);
            } catch (IOException e) {
                Log.e("ShutdownAnalytics", "Update shutdown event fail.");
            }
        }
    }
}
