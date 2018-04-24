package com.ubtrobot.async;

import android.os.Handler;
import android.os.Looper;

public abstract class CancelableAsyncTask<D, F, P> extends AsyncTask<D, F, P> {

    protected CancelableAsyncTask() {
        this(new Handler(Looper.getMainLooper()));
    }

    protected CancelableAsyncTask(Handler handler) {
        super(handler, true);
    }

    protected abstract void onCancel();

    @Override
    protected final CancelHandler getCancelHandler() {
        return new CancelHandler() {
            @Override
            public void onCancel() {
                CancelableAsyncTask.this.onCancel();
            }
        };
    }
}
