package com.ubtrobot.upgrade;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceException;

public class DownloadException extends AccessServiceException {

    private static final int CODE_ILLEGAL_OPERATION = 120000;

    protected DownloadException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public boolean causedByIllegalOperation() {
        return getCode() == CODE_ILLEGAL_OPERATION;
    }

    public static class Factory extends AccessServiceException.Factory<DownloadException> {

        @Override
        protected DownloadException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return new DownloadException(code, message, detail, cause);
        }

        public DownloadException illegalOperation(String message) {
            return createException(CODE_ILLEGAL_OPERATION, message);
        }
    }
}