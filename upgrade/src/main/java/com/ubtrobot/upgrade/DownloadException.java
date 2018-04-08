package com.ubtrobot.upgrade;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceException;

public class DownloadException extends AccessServiceException {

    protected DownloadException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public static class Factory extends AccessServiceException.Factory<DownloadException> {

        @Override
        protected DownloadException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return new DownloadException(code, message, detail, cause);
        }
    }
}