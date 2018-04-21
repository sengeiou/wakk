package com.ubtrobot.speech;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceException;

public class UnderstandException extends AccessServiceException {

    public UnderstandException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public static class Factory extends GenericFactory<UnderstandException> {

        @Override
        protected UnderstandException createException(int code, String message, Bundle detail, Throwable cause) {
            return new UnderstandException(code, message, detail, cause);
        }
    }
}
