package com.ubtrobot.motion;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceCompetingItemException;

public class LocomotionException extends AccessServiceCompetingItemException {

    protected LocomotionException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public static class Factory extends GenericFactory<LocomotionException> {

        @Override
        protected LocomotionException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return new LocomotionException(code, message, detail, cause);
        }
    }
}
