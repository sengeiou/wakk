package com.ubtrobot.speech;


import android.os.Bundle;

import com.ubtrobot.exception.AccessServiceCompetingItemException;

public class SynthesizeException extends AccessServiceCompetingItemException {

    public SynthesizeException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public static class Factory extends GenericFactory<SynthesizeException> {

        @Override
        protected SynthesizeException
        createException(int code, String message, Bundle detail, Throwable cause) {
            return new SynthesizeException(code, message, detail, cause);
        }
    }
}
