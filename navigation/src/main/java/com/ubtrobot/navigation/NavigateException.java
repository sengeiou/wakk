package com.ubtrobot.navigation;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceCompetingItemException;

public class NavigateException extends AccessServiceCompetingItemException {

    private NavigateException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public static class Factory extends GenericFactory<NavigateException> {

        @Override
        protected NavigateException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return new NavigateException(code, message, detail, cause);
        }
    }
}