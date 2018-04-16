package com.ubtrobot.motion;

import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceCompetingItemException;

public class JointException extends AccessServiceCompetingItemException {

    private JointException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public static class Factory extends AccessServiceCompetingItemException.Factory<JointException> {

        @Override
        protected JointException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return new JointException(code, message, detail, cause);
        }
    }
}