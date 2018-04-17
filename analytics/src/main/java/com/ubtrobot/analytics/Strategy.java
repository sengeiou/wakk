package com.ubtrobot.analytics;

import android.os.Parcel;
import android.os.Parcelable;

public class Strategy implements Parcelable {

    public static final Strategy DEFAULT = new Strategy.Builder().build();

    private static final int DEFAULT_REPORT_INTERVAL_SECONDS = 300;
    private static final int DEFAULT_REPORT_COUNT = 500;

    private long reportIntervalSeconds;
    private boolean reportAtStartup;
    private int reportCount;

    public static final Parcelable.Creator<Strategy> CREATOR = new Parcelable.Creator<Strategy>() {
        @Override
        public Strategy createFromParcel(Parcel in) {
            return new Strategy(in);
        }

        @Override
        public Strategy[] newArray(int size) {
            return new Strategy[size];
        }
    };

    private Strategy() {
    }


    private Strategy(Parcel in) {
        reportIntervalSeconds = in.readLong();
        reportAtStartup = in.readByte() != 0;
        reportCount = in.readInt();
    }

    public long getReportIntervalSeconds() {
        return reportIntervalSeconds;
    }

    public boolean isReportAtStartup() {
        return reportAtStartup;
    }

    public int getReportCount() {
        return reportCount;
    }

    @Override
    public String toString() {
        return "Strategy{" +
                "reportIntervalSeconds=" + reportIntervalSeconds +
                ", reportAtStartup=" + reportAtStartup +
                ", reportCount=" + reportCount +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(reportIntervalSeconds);
        dest.writeByte((byte) (reportAtStartup ? 1 : 0));
        dest.writeInt(reportCount);
    }

    public static class Builder {

        private long reportIntervalSeconds;
        private boolean reportAtStartup;
        private int reportCount;

        public Builder() {
            reportIntervalSeconds = DEFAULT_REPORT_INTERVAL_SECONDS;
            reportAtStartup = true;
            reportCount = DEFAULT_REPORT_COUNT;
        }

        public Builder(Strategy strategy) {
            reportIntervalSeconds = strategy.getReportIntervalSeconds();
            reportAtStartup = strategy.isReportAtStartup();
            reportCount = strategy.getReportCount();
        }

        public Builder setReportIntervalSeconds(long reportIntervalSeconds) {
            this.reportIntervalSeconds = reportIntervalSeconds;
            return this;
        }

        public Builder setReportAtStartup(boolean reportAtStartup) {
            this.reportAtStartup = reportAtStartup;
            return this;
        }

        public Builder setReportCount(int reportCount) {
            this.reportCount = reportCount;
            return this;
        }

        public Strategy build() {
            Strategy strategy = new Strategy();
            strategy.reportIntervalSeconds = reportIntervalSeconds;
            strategy.reportAtStartup = reportAtStartup;
            strategy.reportCount = reportCount;
            return strategy;
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "reportIntervalSeconds=" + reportIntervalSeconds +
                    ", reportAtStartup=" + reportAtStartup +
                    ", reportCount=" + reportCount +
                    '}';
        }
    }
}
