package com.ubtrobot.power;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceCompetingItemException;

public class ChargeException extends AccessServiceCompetingItemException {

    protected ChargeException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public static final class Factory extends AccessServiceCompetingItemException
            .GenericFactory<ChargeException> {
        @Override
        protected ChargeException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return new ChargeException(code, message, detail, cause);
        }
    }
}
