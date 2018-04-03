package com.ubtrobot.exception;

import android.os.Bundle;

import com.ubtrobot.master.competition.ActivateException;
import com.ubtrobot.master.transport.message.CallGlobalCode;
import com.ubtrobot.transport.message.CallException;

public class AccessServiceException extends RichException {

    private static final int CODE_FORBIDDEN = CallGlobalCode.FORBIDDEN;
    private static final int CODE_INTERRUPTED = 420; // TODO 待 Master 更新 SDK 后，使用常量
    private static final int CODE_OCCUPIED = 422;
    private static final int CODE_INTERNAL_ERROR = CallGlobalCode.INTERNAL_ERROR;

    protected AccessServiceException(int code, String message, Bundle detail, Throwable cause) {
        super(code, message, detail, cause);
    }

    public boolean causedByForbidden() {
        return getCode() == CODE_FORBIDDEN;
    }

    public boolean causedByInterrupted() {
        return getCode() == CODE_INTERRUPTED;
    }

    public boolean causedByOccupied() {
        return getCode() == CODE_OCCUPIED;
    }

    public boolean causedByInternalError() {
        return getCode() == CODE_INTERNAL_ERROR;
    }

    public static abstract class Factory<E extends AccessServiceException>
            extends RichException.Factory<E> {

        public E from(CallException e) {
            // TODO TODO 待 Master 更新 SDK 后，使用 e.getDetail
            return createException(e.getCode(), e.getMessage(), null, e.getCause());
        }

        public E forbidden(String message, Bundle detail) {
            return createException(CODE_FORBIDDEN, message, detail, null);
        }

        public E interrupted(String message, Bundle detail) {
            return createException(CODE_INTERRUPTED, message, detail, null);
        }

        public E occupied(String message, Bundle detail) {
            return createException(CODE_OCCUPIED, message, detail, null);
        }

        public E occupied(ActivateException e) {
            // TODO 待 Master 更新后，从 ActivateException 中获取相关信息
            return createException(CODE_OCCUPIED, "The competing item is occupied.", null, e);
        }

        public E internalError(String message) {
            return createException(CODE_INTERNAL_ERROR, message, null, null);
        }

        public E internalError(String message, Throwable cause) {
            return createException(CODE_INTERNAL_ERROR, message, null, cause);
        }
    }
}