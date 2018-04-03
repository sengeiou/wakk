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
    }
}
