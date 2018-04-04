package com.ubtrobot.upgrade;

import android.os.Bundle;

import com.ubtrobot.exception.RichException;

public class DownloadException extends RichException {

    protected DownloadException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public static class Factory extends RichException.Factory<DownloadException> {

        @Override
        protected DownloadException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return null;
        }
    }
}