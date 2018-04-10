package com.ubtrobot.upgrade;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceException;

public class UpgradeException extends AccessServiceException {

    private static final int CODE_VERIFY_PACKAGE_ERROR = 100000;

    protected UpgradeException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public boolean causedByVerifyingPackageError() {
        return getCode() == CODE_VERIFY_PACKAGE_ERROR;
    }

    public static class Factory extends AccessServiceException.Factory<UpgradeException> {

        @Override
        protected UpgradeException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return new UpgradeException(code, message, detail, cause);
        }

        public UpgradeException verifyPackageError(String message) {
            return createException(CODE_VERIFY_PACKAGE_ERROR, message);
        }

        public UpgradeException verifyPackageError(String message, Throwable cause) {
            return createException(CODE_VERIFY_PACKAGE_ERROR, message, cause);
        }
    }
}
