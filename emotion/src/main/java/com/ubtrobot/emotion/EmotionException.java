package com.ubtrobot.emotion;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceException;

public class EmotionException extends AccessServiceException {

    private EmotionException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public static class Factory extends AccessServiceException.Factory<EmotionException> {

        @Override
        protected EmotionException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return new EmotionException(code, message, detail, cause);
        }
    }
}
