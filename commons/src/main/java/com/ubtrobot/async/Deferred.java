package com.ubtrobot.async;

import android.os.Handler;

public class Deferred<D, F, P> extends AbstractPromise<D, F, P> {

    private volatile CancelHandler mCancelHandler;

    public Deferred(Handler handler) {
        this(handler, true);
    }

    public Deferred(Handler handler, boolean cancelable) {
        super(handler, cancelable);
        if (handler == null) {
            throw new IllegalArgumentException("Argument handler is null.");
        }
    }

    @Override
    public Deferred<D, F, P> resolve(D resolve) {
        super.resolve(resolve);
        return this;
    }

    @Override
    public Deferred<D, F, P> reject(F reject) {
        super.reject(reject);
        return this;
    }

    @Override
    public Deferred<D, F, P> notify(P progress) {
        super.notify(progress);
        return this;
    }

    public Deferred<D, F, P> setCancelHandler(CancelHandler cancelHandler) {
        mCancelHandler = cancelHandler;
        return this;
    }

    @Override
    protected CancelHandler getCancelHandler() {
        return mCancelHandler;
    }
}