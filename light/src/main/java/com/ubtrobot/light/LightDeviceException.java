package com.ubtrobot.light;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceException;

public class LightDeviceException extends AccessServiceException {

    private LightDeviceException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public static class Factory extends AccessServiceException.Factory<LightDeviceException> {

        @Override
        protected LightDeviceException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return new LightDeviceException(code, message, detail, cause);
        }
    }
}
