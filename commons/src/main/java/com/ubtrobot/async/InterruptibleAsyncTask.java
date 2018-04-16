package com.ubtrobot.async;

public abstract class InterruptibleAsyncTask<D, F, P> extends CancelableAsyncTask<D, F, P> {

    private boolean interrupted;

    public void interrupt(F fail) {
        synchronized (this) {
            if (interrupted) {
                return;
            }

            if (!isPending()) {
                return;
            }

            interrupted = true;
            onCancel();
            reject(fail);
        }
    }

    public boolean isInterrupted() {
        synchronized (this) {
            return interrupted;
        }
    }
}
