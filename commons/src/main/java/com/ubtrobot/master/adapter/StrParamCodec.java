package com.ubtrobot.master.adapter;

import android.os.Parcel;
import android.os.Parcelable;

import com.ubtrobot.master.transport.message.parcel.ParcelableParam;
import com.ubtrobot.transport.message.Param;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

public class StrParamCodec {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("StrParamCodec");

    public static Param encode(String str) {
        return ParcelableParam.create(new StringParcelable(str));
    }

    public static String decode(Param param) {
        try {
            return ParcelableParam.from(param, StringParcelable.class).getParcelable().getContent();
        } catch (ParcelableParam.InvalidParcelableParamException e) {
            LOGGER.e("Illegal param which NOT encoded by StrParamCodec.encode(...)");
            return null;
        }
    }

    public static class StringParcelable implements Parcelable {

        public static final Creator<StringParcelable> CREATOR = new Creator<StringParcelable>() {
            @Override
            public StringParcelable createFromParcel(Parcel in) {
                return new StringParcelable(in);
            }

            @Override
            public StringParcelable[] newArray(int size) {
                return new StringParcelable[size];
            }
        };

        private String content;

        public StringParcelable(String content) {
            this.content = content;
        }

        private StringParcelable(Parcel in) {
            content = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(content);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public String getContent() {
            return content;
        }

        @Override
        public String toString() {
            return "StringParcelable{" +
                    "content='" + content + '\'' +
                    '}';
        }
    }
}
