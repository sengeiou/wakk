package com.ubtrobot.analytics;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class Event implements Parcelable {

    private String eventId;
    private String category;
    private long recordedAt;
    private Map<String, String> segmentation;   // 系统定义
    private Map<String, String> customSegmentation; // 客户定义

    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    private Event(String eventId, String category) {
        this.eventId = eventId;
        this.category = category;
    }

    private Event(Parcel in) {
        eventId = in.readString();
        category = in.readString();
        recordedAt = in.readLong();

        boolean hasSegmentation = in.readByte() != 0;
        if (hasSegmentation) {
            segmentation = new HashMap<>();
            in.readMap(segmentation, String.class.getClassLoader());
        }

        boolean hasCustomSegmentation = in.readByte() != 0;
        if (hasCustomSegmentation) {
            customSegmentation = new HashMap<>();
            in.readMap(customSegmentation, String.class.getClassLoader());
        }
    }

    public String getEventId() {
        return eventId;
    }

    public String getCategory() {
        return category;
    }

    public long getRecordedAt() {
        return recordedAt;
    }

    public Map<String, String> getSegmentation() {
        return segmentation;
    }

    public Map<String, String> getCustomSegmentation() {
        return customSegmentation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(eventId);
        dest.writeString(category);
        dest.writeLong(recordedAt);

        dest.writeByte((byte) (segmentation == null ? 0 : 1));
        if (segmentation != null) {
            dest.writeMap(segmentation);
        }

        dest.writeByte((byte) (customSegmentation == null ? 0 : 1));
        if (customSegmentation != null) {
            dest.writeMap(customSegmentation);
        }
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventId='" + eventId + '\'' +
                ", category='" + category + '\'' +
                ", recordedAt=" + recordedAt +
                ", segmentation=" + segmentation +
                ", customSegmentation=" + customSegmentation +
                '}';
    }

    public static class Builder {

        private String eventId;
        private String category;
        // 单位：s
        private long recordedAt = System.currentTimeMillis() / 1000;
        private Map<String, String> segmentation;
        private Map<String, String> customSegmentation;

        public Builder(String eventId, String category) {
            this.eventId = eventId;
            this.category = category;
        }

        public Builder setRecordedAt(long recordedAt) {
            this.recordedAt = recordedAt;
            return this;
        }

        public Builder setSegmentation(Map<String, String> segmentation) {
            this.segmentation = segmentation;
            return this;
        }

        public Builder setCustomSegmentation(Map<String, String> customSegmentation) {
            this.customSegmentation = customSegmentation;
            return this;
        }

        public Event build() {
            Event event = new Event(eventId, category);
            event.recordedAt = recordedAt;
            event.segmentation = segmentation;
            event.customSegmentation = customSegmentation;

            return event;
        }

        @Override
        public String toString() {
            return "Event.Builder{" +
                    "eventId='" + eventId + '\'' +
                    ", category='" + category + '\'' +
                    ", recordedAt=" + recordedAt +
                    ", segmentation=" + segmentation +
                    ", customSegmentation=" + customSegmentation +
                    '}';
        }
    }

}
