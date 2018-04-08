package com.ubtrobot.upgrade;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceException;

public class UpgradeException extends AccessServiceException {

    protected UpgradeException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public static class Factory extends AccessServiceException.Factory<UpgradeException> {

        @Override
        protected UpgradeException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return new UpgradeException(code, message, detail, cause);
        }
    }
}
