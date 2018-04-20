package com.ubtrobot.speech;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceCompetingItemException;

public class RecognizeException extends AccessServiceCompetingItemException {

    public RecognizeException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public static class Factory extends GenericFactory<RecognizeException> {
        @Override
        protected RecognizeException createException(
                int code, String message, Bundle detail, Throwable cause) {
            return new RecognizeException(code, message, detail, cause);
        }
    }
}