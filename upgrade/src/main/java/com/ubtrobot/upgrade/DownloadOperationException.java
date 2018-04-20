package com.ubtrobot.upgrade;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceException;

public class DownloadOperationException extends AccessServiceException {

    private static final int CODE_ILLEGAL_OPERATION = 130000;

    private DownloadOperationException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public boolean causedByIllegalOperation() {
        return getCode() == CODE_ILLEGAL_OPERATION;
    }

    public static class Factory extends GenericFactory<DownloadOperationException> {

        @Override
        protected DownloadOperationException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return new DownloadOperationException(code, message, detail, cause);
        }

        public DownloadOperationException illegalOperation(String message) {
            return createException(CODE_ILLEGAL_OPERATION, message);
        }
    }
}
