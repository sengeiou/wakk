package com.ubtrobot.analytics;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.ubtrobot.analytics.ipc.AnalyticsConstants;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DiskEventStorage implements EventStorage {

    private static final String TAG = "DiskEventStorage";
    private static final int EVENT_COUNT_NOT_CACHE = -1;

    private final Context mContext;
    private final Gson mGson;

    private UAnalyticsDBHelper mHelper;
    private int mEventCount = EVENT_COUNT_NOT_CACHE;

    public DiskEventStorage(Context context) {
        mContext = context;
        mHelper = new UAnalyticsDBHelper(mContext);
        mGson = new Gson();
    }

    @Override
    public void writeEvent(Event event) throws IOException {
        synchronized (this) {
            writeEvents(Collections.singletonList(event));
        }
    }

    @Override
    public void writeEvents(List<Event> events) throws IOException {
        synchronized (this) {
            SQLiteDatabase db = mHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                for (Event event : events) {
                    ContentValues values = new ContentValues();
                    values.put(Scheme.EVENT_COLUMN_EVENT_ID, event.getEventId());
                    values.put(Scheme.EVENT_COLUMN_CATEGORY, event.getCategory());
                    values.put(Scheme.EVENT_COLUMN_RECORDED_AT, event.getRecordedAt());
                    values.put(Scheme.EVENT_COLUMN_SEGMENTATION, mGson.toJson(event.getSegmentation()));
                    values.put(Scheme.EVENT_COLUMN_CUSTOM_SEGMENTATION, mGson.toJson(event.getCustomSegmentation()));

                    db.insert(Scheme.TABLE_EVENT, null, values);
                }
                db.setTransactionSuccessful();

                mEventCount = EVENT_COUNT_NOT_CACHE;
            } finally {
                db.endTransaction();
            }
        }
    }

    @Override
    public int getEventCount() throws IOException {
        synchronized (this) {
            if (mEventCount >= 0) {
                return mEventCount;
            }

            SQLiteDatabase db = mHelper.getWritableDatabase();
            String query = "SELECT COUNT(*) FROM " + Scheme.TABLE_EVENT;
            Cursor cursor = db.rawQuery(query, null);
            try {
                cursor.moveToFirst();
                mEventCount = (int) cursor.getLong(0);
            } finally {
                cursor.close();
            }

            return mEventCount;
        }
    }

    @Override
    public List<Event> readEvents(int count) throws IOException {
        synchronized (this) {
            SQLiteDatabase db = mHelper.getReadableDatabase();
            LinkedList<Event> events = new LinkedList<>();
            Cursor cursor = db.query(Scheme.TABLE_EVENT, null,
                    Scheme.EVENT_COLUMN_CATEGORY + " != ?",
                    new String[]{AnalyticsConstants.SHUTDOWN_EVENT},
                    null, null,
                    Scheme.EVENT_COLUMN_ID + " asc", count + "");
            try {
                while (cursor.moveToNext()) {
                    events.add(getEvent(cursor));
                }
            } finally {
                cursor.close();
            }

            return events;
        }
    }

    private Event getEvent(Cursor cursor) {
        String eventId = cursor.getString(cursor.getColumnIndex(Scheme.EVENT_COLUMN_EVENT_ID));
        String category = cursor.getString(cursor.getColumnIndex(Scheme.EVENT_COLUMN_CATEGORY));
        long recordAt = cursor.getLong(cursor.getColumnIndex(Scheme.EVENT_COLUMN_RECORDED_AT));
        String segmentation = cursor.getString(
                cursor.getColumnIndex(Scheme.EVENT_COLUMN_SEGMENTATION));
        String customSegmentation = cursor.getString(
                cursor.getColumnIndex(Scheme.EVENT_COLUMN_CUSTOM_SEGMENTATION));

        return new Event.Builder(eventId, category)
                .setRecordedAt(recordAt)
                .setSegmentation(readJson(segmentation))
                .setCustomSegmentation(readJson(customSegmentation))
                .build();
    }

    private Map<String, String> readJson(String jsonString) {
        Map<String, String> map;
        try {
            map = mGson.fromJson(jsonString,
                    new TypeToken<Map<String, String>>() {
                    }.getType());

        } catch (JsonSyntaxException e) {
            Log.e(TAG, "Convert to map failed:" + jsonString);
            return new HashMap<>();
        }

        return map;
    }

    @Override
    public void removeEvents(int count) throws IOException {
        synchronized (this) {
            SQLiteDatabase db = mHelper.getWritableDatabase();
            String whereClause = Scheme.EVENT_COLUMN_ID + " IN (" + " SELECT "
                    + Scheme.EVENT_COLUMN_ID + " FROM " + Scheme.TABLE_EVENT + " LIMIT " + count + " )";
            db.delete(Scheme.TABLE_EVENT, whereClause, null);

            mEventCount = EVENT_COUNT_NOT_CACHE;
        }
    }

    public void removeEvents(String category, long beginRecordedAt, long endRecordedAt) {
        synchronized (this) {
            SQLiteDatabase db = mHelper.getWritableDatabase();
            String deleteWhere = String.format("%s = '%s' AND %s >= '%s' AND %s <= '%s'",
                    Scheme.EVENT_COLUMN_CATEGORY, category,
                    Scheme.EVENT_COLUMN_RECORDED_AT, beginRecordedAt,
                    Scheme.EVENT_COLUMN_RECORDED_AT, endRecordedAt);
            db.delete(Scheme.TABLE_EVENT, deleteWhere, null);
        }
    }

    public List<Event> readShutdownEvents(int count) throws IOException {
        synchronized (this) {
            SQLiteDatabase db = mHelper.getReadableDatabase();
            LinkedList<Event> events = new LinkedList<>();
            Cursor cursor = db.query(Scheme.TABLE_EVENT, null,
                    Scheme.EVENT_COLUMN_CATEGORY + " == ?",
                    new String[]{AnalyticsConstants.SHUTDOWN_EVENT},
                    null, null,
                    Scheme.EVENT_COLUMN_ID + " asc", count + "");
            try {
                while (cursor.moveToNext()) {
                    events.add(getEvent(cursor));
                }
            } finally {
                cursor.close();
            }

            return events;
        }
    }

    public void updateEvent(Event event) throws IOException {
        synchronized (this) {
            SQLiteDatabase db = mHelper.getWritableDatabase();
            String sql = String.format("SELECT * FROM %s where %s = '%s' ORDER BY %s DESC LIMIT 1",
                    Scheme.TABLE_EVENT, Scheme.EVENT_COLUMN_CATEGORY,
                    AnalyticsConstants.SHUTDOWN_EVENT, Scheme.EVENT_COLUMN_RECORDED_AT);
            long recordedAt = -1;
            Cursor cursor = db.rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                Event existEvent = getEvent(cursor);
                Log.i(TAG, "select event: " + existEvent.toString());
                String isShutdown = existEvent.getCustomSegmentation().
                        get(AnalyticsConstants.IS_SHUTDOWN);
                boolean isUpdate = TextUtils.isEmpty(isShutdown) || isShutdown.equals("false");
                recordedAt = isUpdate ? existEvent.getRecordedAt() : -1;
            }

            if (recordedAt <= 0) {
                writeEvent(event);
                return;
            }

            String updateSql = String.format(
                    "UPDATE %s SET %s = '%s', %s = '%s' WHERE %s = '%s' AND %s = '%s'",
                    Scheme.TABLE_EVENT,
                    Scheme.EVENT_COLUMN_RECORDED_AT, event.getRecordedAt(),
                    Scheme.EVENT_COLUMN_CUSTOM_SEGMENTATION,
                    mGson.toJson(event.getCustomSegmentation()),
                    Scheme.EVENT_COLUMN_RECORDED_AT, recordedAt,
                    Scheme.EVENT_COLUMN_CATEGORY, AnalyticsConstants.SHUTDOWN_EVENT);
            Log.i("cj", "update sql: " + updateSql);
            db.execSQL(updateSql);
        }
    }

    private static class Scheme {

        static final String DATABASE = "analytics";
        static final int VERSION = 1;

        static final String TABLE_EVENT = "event";
        static final String EVENT_COLUMN_ID = "id";
        static final String EVENT_COLUMN_EVENT_ID = "event_id";
        static final String EVENT_COLUMN_CATEGORY = "category";
        static final String EVENT_COLUMN_RECORDED_AT = "recorded_at";
        static final String EVENT_COLUMN_SEGMENTATION = "segmentation";
        static final String EVENT_COLUMN_CUSTOM_SEGMENTATION = "custom_segmentation";
    }


    private static class UAnalyticsDBHelper extends SQLiteOpenHelper {

        UAnalyticsDBHelper(Context context) {
            super(context, Scheme.DATABASE, null, Scheme.VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            String createTable = "CREATE TABLE " + Scheme.TABLE_EVENT + " ("
                    + Scheme.EVENT_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + Scheme.EVENT_COLUMN_EVENT_ID + " VARCHAR(32),"
                    + Scheme.EVENT_COLUMN_CATEGORY + " VARCHAR(32),"
                    + Scheme.EVENT_COLUMN_RECORDED_AT + " INTEGER,"
                    + Scheme.EVENT_COLUMN_SEGMENTATION + " VARCHAR,"
                    + Scheme.EVENT_COLUMN_CUSTOM_SEGMENTATION + " VARCHAR"
                    + ")";

            sqLiteDatabase.execSQL(createTable);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            // Nothing
        }
    }
}
