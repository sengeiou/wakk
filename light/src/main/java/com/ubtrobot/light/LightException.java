package com.ubtrobot.light;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceCompetingItemException;

public class LightException extends AccessServiceCompetingItemException {

    private LightException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public static class Factory extends GenericFactory<LightException> {
        @Override
        protected LightException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return new LightException(code, message, detail, cause);
        }
    }
}
