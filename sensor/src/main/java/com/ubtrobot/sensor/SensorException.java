package com.ubtrobot.sensor;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceException;

public class SensorException extends AccessServiceException {

    protected SensorException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public static class Factory extends AccessServiceException.GenericFactory<SensorException> {

        @Override
        protected SensorException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return new SensorException(code, message, detail, cause);
        }
    }
}
