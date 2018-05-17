package com.ubtrobot.speech.understand;

import android.os.Parcel;
import android.os.Parcelable;

public class LegacyUnderstandResult extends UnderstandResult {

    private LegacyData mLegacyData;

    public LegacyUnderstandResult(Builder builder) {
        super(builder);
    }

    public LegacyUnderstandResult(Parcel in) {
        super(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(mLegacyData, flags);
    }

    @SuppressWarnings("unchecked")
    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mLegacyData = in.readParcelable(LegacyData.class.getClassLoader());
    }

    public LegacyData getLegacyData() {
        return mLegacyData;
    }

    public static class Builder extends UnderstandResult.Builder {

        private LegacyData legacyData;

        public Builder() {
        }

        public void setLegacyData(LegacyData legacyData) {
            this.legacyData = legacyData;
        }

        public LegacyUnderstandResult build() {
            LegacyUnderstandResult legacyUnderstandResult = new LegacyUnderstandResult(this);
            legacyUnderstandResult.mLegacyData = legacyData;
            return legacyUnderstandResult;
        }
    }

    public static class LegacyData implements Parcelable {

        private String intentName;
        private int appId;
        private String dataValue;

        private LegacyData() {
        }

        public LegacyData(Parcel in) {
            readFromParcel(in);
        }

        public String getIntentName() {
            return intentName;
        }

        public int getAppId() {
            return appId;
        }

        public String getDataValue() {
            return dataValue;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(intentName);
            dest.writeInt(appId);
            dest.writeString(dataValue);
        }

        @SuppressWarnings("unchecked")
        private void readFromParcel(Parcel in) {
            intentName = in.readString();
            appId = in.readInt();
            dataValue = in.readString();
        }

        public static final Parcelable.Creator<LegacyData> CREATOR = new Parcelable
                .Creator<LegacyData>() {
            @Override
            public LegacyData createFromParcel(Parcel source) {
                return new LegacyData(source);
            }

            @Override
            public LegacyData[] newArray(int size) {
                return new LegacyData[size];
            }
        };

        public static class Builder {

            private String intentName;
            private int appId;
            private String dataValue;

            //uncheck/
            public Builder setIntentName(String intentName) {
                this.intentName = intentName;
                return this;
            }

            public Builder setAppId(int appId) {
                this.appId = appId;
                return this;
            }

            //uncheck/
            public Builder setDataValue(String dataValue) {
                this.dataValue = dataValue;
                return this;
            }

            public LegacyData build() {
                LegacyData legacyData = new LegacyData();
                legacyData.intentName = intentName;
                legacyData.appId = appId;
                legacyData.dataValue = dataValue;
                return legacyData;
            }
        }

    }

}
