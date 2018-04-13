package com.ubtrobot.analytics;

public class ReportException extends Exception {

    private static final int CODE_NETWORK_DISCONNECTED = 1;
    private static final int CODE_INTERNAL_SERVER_ERROR = 2;
    private static final int CODE_TIMEOUT = 3;

    private final int code;

    private ReportException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public boolean causedByNetworkDisconnected() {
        return code == CODE_NETWORK_DISCONNECTED;
    }

    public boolean causedByInternalServerError() {
        return code == CODE_INTERNAL_SERVER_ERROR;
    }

    public boolean causedByTimeOut() {
        return code == CODE_TIMEOUT;
    }

    public static class Factory {

        public static ReportException networkDisconnected(Throwable throwable) {
            return new ReportException(ReportException.CODE_NETWORK_DISCONNECTED,
                    "Network disconnected.", throwable);
        }

        public static ReportException internalServerError(Throwable throwable) {
            return new ReportException(ReportException.CODE_INTERNAL_SERVER_ERROR,
                    "Can NOT connect the server.", throwable);
        }

        public static ReportException timeout(Throwable throwable) {
            return new ReportException(ReportException.CODE_TIMEOUT,
                    "Report timeout.", throwable);
        }

    }

}