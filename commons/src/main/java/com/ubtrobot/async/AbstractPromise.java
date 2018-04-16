package com.ubtrobot.async;

import android.os.Handler;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class AbstractPromise<D, F, P> implements Promise<D, F, P> {

    private volatile int mState = STATE_PENDING;

    private final Handler mHandler;
    private final boolean mCancelable;

    private final List<DoneCallback<? super D>> mDoneCallbacks = new LinkedList<>();
    private final List<FailCallback<? super F>> mFailCallbacks = new LinkedList<>();
    private final List<ProgressCallback<? super P>> mProgressCallbacks = new LinkedList<>();

    private D mResolveResult;
    private F mRejectResult;

    protected AbstractPromise(Handler handler, boolean cancelable) {
        mHandler = handler;
        mCancelable = cancelable;
    }

    protected Handler getHandler() {
        return mHandler;
    }

    @Override
    public int state() {
        return mState;
    }

    protected void setState(int state) {
        mState = state;
    }

    @Override
    public boolean isPending() {
        return mState == STATE_PENDING;
    }

    @Override
    public boolean isResolved() {
        return mState == STATE_RESOLVED;
    }

    @Override
    public boolean isRejected() {
        return mState == STATE_REJECTED;
    }

    @Override
    public boolean isCanceled() {
        return mState == STATE_CANCELED;
    }

    protected Promise<D, F, P> resolve(final D resolve) {
        synchronized (this) {
            if (isPending()) {
                setState(STATE_RESOLVED);
                setResolveResult(resolve);
                triggerDone(resolve);
            }
        }

        return this;
    }

    private void setResolveResult(D resolveResult) {
        synchronized (this) {
            mResolveResult = resolveResult;
        }
    }

    protected void triggerDone(D resolved) {
        synchronized (this) {
            for (DoneCallback<? super D> callback : mDoneCallbacks) {
                triggerDoneLocked(callback, resolved);
            }

            clearCallbacksLocked();
            notifyAll();
        }
    }

    protected Promise<D, F, P> reject(final F reject) {
        synchronized (this) {
            if (isPending()) {
                setState(STATE_REJECTED);
                setRejectResult(reject);
                triggerFail(reject);
            }
        }

        return this;
    }

    private void setRejectResult(F rejectResult) {
        synchronized (this) {
            mRejectResult = rejectResult;
        }
    }

    protected void triggerFail(F rejected) {
        synchronized (this) {
            for (FailCallback<? super F> callback : mFailCallbacks) {
                triggerFailLocked(callback, rejected);
            }

            clearCallbacksLocked();
            notifyAll();
        }
    }

    protected Promise<D, F, P> notify(final P progress) {
        synchronized (this) {
            if (isPending()) {
                triggerProgress(progress);
            }
        }

        return this;
    }

    protected void triggerProgress(P progress) {
        synchronized (this) {
            for (ProgressCallback<? super P> callback : mProgressCallbacks) {
                triggerProgressLocked(callback, progress);
            }
        }
    }

    @Override
    public Promise<D, F, P> done(DoneCallback<? super D> callback) {
        synchronized (this) {
            if (isResolved()) {
                triggerDoneLocked(callback, mResolveResult);
            } else {
                mDoneCallbacks.add(callback);
            }
        }
        return this;
    }

    private void triggerDoneLocked(final DoneCallback<? super D> callback, final D resolved) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onDone(resolved);
            }
        });
    }

    private void clearCallbacksLocked() {
        mDoneCallbacks.clear();
        mFailCallbacks.clear();
        mProgressCallbacks.clear();
    }

    @Override
    public Promise<D, F, P> fail(FailCallback<? super F> callback) {
        synchronized (this) {
            if (isRejected()) {
                triggerFailLocked(callback, mRejectResult);
            } else {
                mFailCallbacks.add(callback);
            }
        }
        return this;
    }

    private void triggerFailLocked(final FailCallback<? super F> callback, final F rejected) {
        synchronized (this) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onFail(rejected);
                }
            });
        }
    }

    @Override
    public Promise<D, F, P> progress(ProgressCallback<? super P> callback) {
        synchronized (this) {
            mProgressCallbacks.add(callback);
        }
        return this;
    }

    private void triggerProgressLocked(final ProgressCallback<? super P> callback, final P progress) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onProgress(progress);
            }
        });
    }

    @Override
    public boolean cancel() {
        if (!mCancelable) {
            throw new UnsupportedOperationException();
        }

        synchronized (this) {
            if (!isPending()) {
                return false;
            }

            mState = STATE_CANCELED;
            CancelHandler cancelHandler = getCancelHandler();
            if (cancelHandler != null) {
                cancelHandler.onCancel();
            }

            return true;
        }
    }

    protected abstract CancelHandler getCancelHandler();

    private void waitSafely() throws InterruptedException {
        waitSafely(-1);
    }

    private void waitSafely(long timeout) throws InterruptedException {
        final long startTime = System.currentTimeMillis();
        synchronized (this) {
            while (this.isPending()) {
                try {
                    if (timeout <= 0) {
                        wait();
                    } else {
                        final long elapsed = (System.currentTimeMillis() - startTime);
                        final long waitTime = timeout - elapsed;
                        wait(waitTime);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw e;
                }

                if (timeout > 0 && ((System.currentTimeMillis() - startTime) >= timeout)) {
                    return;
                } else {
                    continue; // keep looping
                }
            }
        }
    }

    @Override
    public D getDone() throws InterruptedException {
        waitSafely();
        return mResolveResult;
    }

    @Override
    public D getDone(long timeout, TimeUnit unit) throws InterruptedException {
        waitSafely(unit.toMillis(timeout));
        return mResolveResult;
    }

    @Override
    public F getFail() throws InterruptedException {
        waitSafely();
        return mRejectResult;
    }

    @Override
    public F getFail(long timeout, TimeUnit unit) throws InterruptedException {
        waitSafely(unit.toMillis(timeout));
        return mRejectResult;
    }

    public Promise<D, F, P> promise() {
        return this;
    }
}