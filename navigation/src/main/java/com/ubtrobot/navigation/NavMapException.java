package com.ubtrobot.navigation;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceException;

public class NavMapException extends AccessServiceException {

    private NavMapException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public static class Factory extends GenericFactory<NavMapException> {

        @Override
        protected NavMapException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return new NavMapException(code, message, detail, cause);
        }
    }
}
