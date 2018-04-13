package com.ubtrobot.upgrade;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceException;

public class DownloadException extends AccessServiceException {

    private static final int CODE_ILLEGAL_OPERATION = 120000;
    private static final int CODE_NETWORK_DISCONNECTED = 120001;
    private static final int CODE_FILE_SERVER_ERROR = 120002;
    private static final int CODE_FILE_SYSTEM_ERROR = 120003;
    private static final int CODE_INSUFFICIENT_SPACE = 120004;
    private static final int CODE_NETWORK_TIMEOUT = 120005;

    protected DownloadException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public boolean causedByIllegalOperation() {
        return getCode() == CODE_ILLEGAL_OPERATION;
    }

    public boolean causedByNetworkDisconnected() {
        return getCode() == CODE_NETWORK_DISCONNECTED;
    }

    public boolean causedByFileServerError() {
        return getCode() == CODE_FILE_SERVER_ERROR;
    }

    public boolean causedByFileSystemError() {
        return getCode() == CODE_FILE_SYSTEM_ERROR;
    }

    public boolean causedByInsufficientSpace() {
        return getCode() == CODE_INSUFFICIENT_SPACE;
    }

    public boolean causedByNetworkTimeout() {
        return getCode() == CODE_NETWORK_TIMEOUT;
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

        public DownloadException networkDisconnected(String message, Throwable cause) {
            return createException(CODE_NETWORK_DISCONNECTED, message, cause);
        }

        public DownloadException networkDisconnected(String message) {
            return createException(CODE_NETWORK_DISCONNECTED, message);
        }

        public DownloadException fileServerError(String message, Throwable cause) {
            return createException(CODE_FILE_SERVER_ERROR, message, cause);
        }

        public DownloadException fileServerError(String message) {
            return createException(CODE_FILE_SERVER_ERROR, message);
        }

        public DownloadException fileSystemError(String message, Throwable cause) {
            return createException(CODE_FILE_SYSTEM_ERROR, message, cause);
        }

        public DownloadException fileSystemError(String message) {
            return createException(CODE_FILE_SYSTEM_ERROR, message);
        }

        public DownloadException insufficientSpace(String message, Throwable cause) {
            return createException(CODE_INSUFFICIENT_SPACE, message, cause);
        }

        public DownloadException insufficientSpace(String message) {
            return createException(CODE_INSUFFICIENT_SPACE, message);
        }

        public DownloadException networkTimeout(String message, Throwable cause) {
            return createException(CODE_NETWORK_TIMEOUT, message, cause);
        }

        public DownloadException networkTimeout(String message) {
            return createException(CODE_NETWORK_TIMEOUT, message);
        }
    }
}