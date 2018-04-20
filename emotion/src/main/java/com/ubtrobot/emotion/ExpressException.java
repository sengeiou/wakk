package com.ubtrobot.emotion;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceCompetingItemException;

public class ExpressException extends AccessServiceCompetingItemException {

    private ExpressException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public static class Factory extends GenericFactory<ExpressException> {

        @Override
        protected ExpressException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return new ExpressException(code, message, detail, cause);
        }
    }
}
