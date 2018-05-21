package com.ubtrobot.play;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceCompetingItemException;
import com.ubtrobot.exception.AccessServiceException;

public class PlayException extends AccessServiceCompetingItemException {

    protected PlayException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public static class Factory extends AccessServiceException.GenericFactory<PlayException> {

        @Override
        protected PlayException createException(int code, String message, Bundle detail, Throwable cause) {
            return new PlayException(code, message, detail, cause);
        }

    }
}
