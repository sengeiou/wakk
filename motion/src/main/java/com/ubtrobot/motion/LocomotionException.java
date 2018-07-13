package com.ubtrobot.motion;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceCompetingItemException;

public class LocomotionException extends AccessServiceCompetingItemException {

    private static final int CODE_BLOCKED = 425;

    protected LocomotionException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public boolean causedByBlocked() { return getCode() == CODE_BLOCKED; }

    public static class Factory extends GenericFactory<LocomotionException> {

        @Override
        protected LocomotionException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return new LocomotionException(code, message, detail, cause);
        }

        public LocomotionException blocked(String message) {
            return createException(CODE_BLOCKED, message);
        }
    }
}
