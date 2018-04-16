package com.ubtrobot.light;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceException;

public class LightException extends AccessServiceException {

    private LightException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public static class Factory extends AccessServiceException.Factory<LightException> {

        @Override
        protected LightException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return new LightException(code, message, detail, cause);
        }
    }
}
