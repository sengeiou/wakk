package com.ubtrobot.speech;

import android.os.Parcel;
import android.os.Parcelable;

import com.ubtrobot.validate.Preconditions;

public class SpeechInteraction implements Parcelable {

    private Recognizer.RecognizeResult mRecognizeResult;
    private UnderstandResult mUnderstandResult;

    private SpeechInteraction() {

    }

    private SpeechInteraction(Parcel in) {
        readFromParcel(in);
    }

    public Recognizer.RecognizeResult getRecognizeResult() {
        return mRecognizeResult;
    }

    public UnderstandResult getUnderstandResult() {
        return mUnderstandResult;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mRecognizeResult, flags);
        dest.writeParcelable(mUnderstandResult, flags);
    }

    @SuppressWarnings("unchecked")
    private void readFromParcel(Parcel in) {
        mRecognizeResult = in.readParcelable(Recognizer.RecognizeResult.class.getClassLoader());
        mUnderstandResult = in.readParcelable(UnderstandResult.class.getClassLoader());
    }

    public static final Parcelable.Creator<SpeechInteraction> CREATOR = new Parcelable
            .Creator<SpeechInteraction>() {
        @Override
        public SpeechInteraction createFromParcel(Parcel source) {
            return new SpeechInteraction(source);
        }

        @Override
        public SpeechInteraction[] newArray(int size) {
            return new SpeechInteraction[size];
        }
    };

    public static class Builder {

        private Recognizer.RecognizeResult mRecognizeResult = Recognizer.RecognizeResult.NULL;
        private UnderstandResult mUnderstandResult = UnderstandResult.NULL;

        public void setRecognizeResult(Recognizer.RecognizeResult recognizeResult) {
            Preconditions.checkNotNull(recognizeResult,
                    "SpeechInteraction.Builder refuse null Recognizer.RecognizeResult");
            mRecognizeResult = recognizeResult;
        }

        public void setUnderstandResult(UnderstandResult understandResult) {
            Preconditions.checkNotNull(understandResult,
                    "SpeechInteraction.Builder refuse null UnderstandResult");
            mUnderstandResult = understandResult;
        }

        public SpeechInteraction build() {
            SpeechInteraction speechInteraction = new SpeechInteraction();
            speechInteraction.mRecognizeResult = mRecognizeResult;
            speechInteraction.mUnderstandResult = mUnderstandResult;
            return speechInteraction;
        }
    }
}
