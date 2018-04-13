package com.ubtrobot.analytics;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.ubtrobot.analytics.Event;
import com.ubtrobot.analytics.EventStorage;

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
            Cursor cursor = db.query(Scheme.TABLE_EVENT, null, null,
                    null, null, null, Scheme.EVENT_COLUMN_ID + " asc", count + "");
            try {
                while (cursor.moveToNext()) {
                    String eventId = cursor.getString(cursor.getColumnIndex(Scheme.EVENT_COLUMN_EVENT_ID));
                    String category = cursor.getString(cursor.getColumnIndex(Scheme.EVENT_COLUMN_CATEGORY));
                    long recordAt = cursor.getLong(cursor.getColumnIndex(Scheme.EVENT_COLUMN_RECORDED_AT));
                    String segmentation = cursor.getString(cursor.getColumnIndex(Scheme.EVENT_COLUMN_SEGMENTATION));
                    String customSegmentation = cursor.getString(cursor.getColumnIndex(Scheme.EVENT_COLUMN_CUSTOM_SEGMENTATION));

                    Event event = new Event.Builder(eventId, category)
                            .setRecordedAt(recordAt)
                            .setSegmentation(readJson(segmentation))
                            .setCustomSegmentation(readJson(customSegmentation))
                            .build();

                    events.add(event);
                }
            } finally {
                cursor.close();
            }

            return events;
        }
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

    private static class Scheme {

        static final String DATABASE = "uanalytics";
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
