package com.ubtrobot.motion;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceCompetingItemException;

public class ExecuteException extends AccessServiceCompetingItemException {

    protected ExecuteException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public static class Factory
            extends AccessServiceCompetingItemException.GenericFactory<ExecuteException> {

        @Override
        protected ExecuteException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return new ExecuteException(code, message, detail, cause);
        }
    }
}