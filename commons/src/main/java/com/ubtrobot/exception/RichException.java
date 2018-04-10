package com.ubtrobot.exception;

import android.os.Bundle;

public abstract class RichException extends Exception {

    private final int code;
    private volatile Bundle detail;

    protected RichException(int code, String message, Bundle detail, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.detail = detail;
    }

    public int getCode() {
        return code;
    }

    public Bundle getDetail() {
        if (detail == null) {
            synchronized (this) {
                if (detail == null) {
                    detail = new Bundle();
                }
            }
        }

        return detail;
    }

    public abstract static class Factory<E extends RichException> {

        protected abstract E createException(
                int code, String message,
                Bundle detail, Throwable cause);

        protected E createException(int code, String message) {
            return createException(code, message, null, null);
        }

        protected E createException(int code, String message, Throwable throwable) {
            return createException(code, message, null, throwable);
        }

        public E from(int code, String message, Bundle detail, Throwable cause) {
            return createException(code, message, detail, cause);
        }

        public E from(int code, String message, Bundle detail) {
            return createException(code, message, detail, null);
        }

        public E from(int code, String message) {
            return createException(code, message, null, null);
        }
    }
}
