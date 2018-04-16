package com.ubtrobot.motion;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceException;

public class JointDeviceException extends AccessServiceException {

    private JointDeviceException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public static class Factory extends AccessServiceException.Factory<JointDeviceException> {
        @Override
        protected JointDeviceException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return new JointDeviceException(code, message, detail, cause);
        }
    }
}
