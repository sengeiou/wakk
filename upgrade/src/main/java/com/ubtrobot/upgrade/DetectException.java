package com.ubtrobot.upgrade;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceException;

public class DetectException extends AccessServiceException {

    private static final int CODE_FAILED_TO_ESTABLISH_CONNECTION = 110000;
    private static final int CODE_TIMEOUT = 110001;
    private static final int CODE_SERVER_ERROR = 110002;

    protected DetectException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public boolean causedByFailedToEstablishConnection() {
        return getCode() == CODE_FAILED_TO_ESTABLISH_CONNECTION;
    }

    public boolean causedByTimeout() {
        return getCode() == CODE_TIMEOUT;
    }

    public boolean causedByServerError() {
        return getCode() == CODE_SERVER_ERROR;
    }

    public static class Factory extends AccessServiceException.Factory<DetectException> {

        @Override
        protected DetectException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return new DetectException(code, message, detail, cause);
        }

        public DetectException failedToEstablishConnection(String message, Throwable cause) {
            return createException(CODE_FAILED_TO_ESTABLISH_CONNECTION, message, cause);
        }

        public DetectException timeout(String message, Throwable cause) {
            return createException(CODE_TIMEOUT, message, cause);
        }

        public DetectException serverError(String message, Throwable cause) {
            return createException(CODE_SERVER_ERROR, message, cause);
        }
    }
}