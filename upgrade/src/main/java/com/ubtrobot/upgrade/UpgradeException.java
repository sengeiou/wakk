package com.ubtrobot.upgrade;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.exception.RichException;

public class UpgradeException extends AccessServiceException {

    protected UpgradeException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public static class Factory extends AccessServiceException.Factory {

        @Override
        protected RichException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return null;
        }
    }
}
