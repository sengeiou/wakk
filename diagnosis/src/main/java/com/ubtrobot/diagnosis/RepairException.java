package com.ubtrobot.diagnosis;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceException;

public class RepairException extends AccessServiceException {

    private static final int CODE_PROHIBIT_REENTRY = 100000;

    protected RepairException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public static class Factory extends GenericFactory<RepairException> {

        @Override
        protected RepairException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return new RepairException(code, message, detail, cause);
        }

        public RepairException prohibitReentry(String message) {
            return createException(CODE_PROHIBIT_REENTRY, message);
        }
    }
}
