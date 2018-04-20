package com.ubtrobot.light;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceCompetingItemException;

public class DisplayException extends AccessServiceCompetingItemException {

    private DisplayException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public static class Factory extends GenericFactory<DisplayException> {

        @Override
        protected DisplayException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return new DisplayException(code, message, detail, cause);
        }
    }
}
