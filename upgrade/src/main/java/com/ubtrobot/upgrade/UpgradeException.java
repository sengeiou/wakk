package com.ubtrobot.upgrade;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceException;

public class UpgradeException extends AccessServiceException {

    private static final int CODE_PROHIBIT_REENTRY = 100000;
    private static final int CODE_VERIFY_PACKAGE_ERROR = 100001;
    private static final int CODE_EXECUTING_UPGRADE_ERROR = 100002;

    protected UpgradeException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public boolean causedByVerifyingPackageError() {
        return getCode() == CODE_VERIFY_PACKAGE_ERROR;
    }

    public boolean causedByProhibitReentry() {
        return getCode() == CODE_PROHIBIT_REENTRY;
    }

    public boolean causedByExecutingUpgradeError() {
        return getCode() == CODE_EXECUTING_UPGRADE_ERROR;
    }

    public static class Factory extends GenericFactory<UpgradeException> {

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

        public UpgradeException prohibitReentry(String message) {
            return createException(CODE_PROHIBIT_REENTRY, message);
        }

        public UpgradeException executingUpgradeError(String message) {
            return createException(CODE_EXECUTING_UPGRADE_ERROR, message);
        }

        public UpgradeException executingUpgradeError(String message, Throwable cause) {
            return createException(CODE_EXECUTING_UPGRADE_ERROR, message, cause);
        }
    }
}
