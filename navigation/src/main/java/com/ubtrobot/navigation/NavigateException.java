package com.ubtrobot.navigation;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceCompetingItemException;

public class NavigateException extends AccessServiceCompetingItemException {

    private static final int CODE_BLOCKED = 424;

    private NavigateException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public boolean causedByBlocked() { return getCode() == CODE_BLOCKED; }

    public static class Factory extends GenericFactory<NavigateException> {

        @Override
        protected NavigateException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return new NavigateException(code, message, detail, cause);
        }

        public NavigateException blocked(String message) {
            return createException(CODE_BLOCKED, message);
        }
    }
}