package com.ubtrobot.navigation;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceCompetingItemException;

public class LocateException extends AccessServiceCompetingItemException {

    private LocateException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public static class Factory extends AccessServiceCompetingItemException.Factory<LocateException> {

        @Override
        protected LocateException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return new LocateException(code, message, detail, cause);
        }
    }
}
