package com.ubtrobot.async;

import android.os.Handler;
import android.os.Looper;

public abstract class AsyncTask<D, F, P> extends AbstractPromise<D, F, P> {

    protected AsyncTask() {
        this(new Handler(Looper.getMainLooper()));
    }

    protected AsyncTask(Handler handler) {
        this(handler, false);
    }

    AsyncTask(Handler handler, boolean cancelable) {
        super(handler, cancelable);
    }

    public void start() {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                onStart();
            }
        });
    }

    protected abstract void onStart();

    @Override
    protected AsyncTask<D, F, P> resolve(D resolve) {
        super.resolve(resolve);
        return this;
    }

    @Override
    protected AsyncTask<D, F, P> reject(F reject) {
        super.reject(reject);
        return this;
    }

    @Override
    protected AsyncTask<D, F, P> notify(P progress) {
        super.notify(progress);
        return this;
    }

    @Override
    protected CancelHandler getCancelHandler() {
        return null;
    }
}
