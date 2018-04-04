package com.ubtrobot.upgrade;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceException;

public class DetectException extends AccessServiceException {

    protected DetectException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public static class Factory extends AccessServiceException.Factory<DetectException> {

        @Override
        protected DetectException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return null;
        }
    }
}