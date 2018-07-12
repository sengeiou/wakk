package com.ubtrobot.motion;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceCompetingItemException;

public class PerformException extends AccessServiceCompetingItemException {

    protected PerformException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public static class Factory
            extends AccessServiceCompetingItemException.GenericFactory<PerformException> {

        @Override
        protected PerformException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return new PerformException(code, message, detail, cause);
        }
    }
}