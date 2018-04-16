package com.ubtrobot.exception;

import android.os.Bundle;

import com.ubtrobot.master.competition.ActivateException;

public class AccessServiceCompetingItemException extends AccessServiceException {

    private static final int CODE_INTERRUPTED = 420;
    private static final int CODE_OCCUPIED = 422;

    protected AccessServiceCompetingItemException(
            int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public boolean causedByInterrupted() {
        return getCode() == CODE_INTERRUPTED;
    }

    public boolean causedByOccupied() {
        return getCode() == CODE_OCCUPIED;
    }

    public static abstract class Factory<E extends AccessServiceCompetingItemException>
            extends AccessServiceException.Factory<E> {

        public E interrupted(String message) {
            return createException(CODE_INTERRUPTED, message);
        }

        public E occupied(String message) {
            return createException(CODE_OCCUPIED, message);
        }

        public E occupied(ActivateException e) {
            // TODO 待 Master 更新后，从 ActivateException 中获取相关信息
            return createException(CODE_OCCUPIED, "The competing item is occupied.", null, e);
        }
    }
}