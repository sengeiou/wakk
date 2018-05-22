package com.ubtrobot.async;

public abstract class InterruptibleProgressiveAsyncTask<D, F extends Throwable, P>
        extends ProgressiveAsyncTask<D, F, P> {

    private boolean interrupted;

    public void interrupt(F fail) {
        synchronized (this) {
            if (interrupted) {
                return;
            }

            if (!promise().isPending()) {
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