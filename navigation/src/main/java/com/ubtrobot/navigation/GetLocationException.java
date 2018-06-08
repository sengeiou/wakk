package com.ubtrobot.navigation;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceException;

public class GetLocationException extends AccessServiceException {

    public static final int CODE_NEED_LOCATE_SELF = 140000;

    protected GetLocationException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public boolean causedByNeedToLocateSelf() {
        return getCode() == CODE_NEED_LOCATE_SELF;
    }

    public static class Factory extends GenericFactory<GetLocationException> {

        @Override
        protected GetLocationException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return new GetLocationException(code, message, detail, cause);
        }

        public GetLocationException needToLocateSelf(String message) {
            return createException(CODE_NEED_LOCATE_SELF, message);
        }
    }
}
