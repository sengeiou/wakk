package com.ubtrobot.power.sal;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.async.DefaultPromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.power.ShutdownOption;

import java.util.LinkedList;

public abstract class AbstractPowerService implements PowerService {

    private static final String OP_SLEEP = "sleep";
    private static final String OP_WAKE_UP = "wake-up";

    private final LinkedList<Operation> mOperationQueue = new LinkedList<>();

    private DefaultPromise<Void, AccessServiceException> mShutdownPromise;

    @Override
    public Promise<Boolean, AccessServiceException> sleep() {
        synchronized (mOperationQueue) {
            Operation operation = new Operation(OP_SLEEP) {
                @Override
                public void run() {
                    startSleeping();
                }
            };
            mOperationQueue.add(operation);

            if (mOperationQueue.size() == 1) {
                operateNextLocked();
            }
            return operation.promise;
        }
    }

    protected abstract void startSleeping();

    private void operateNextLocked() {
        final Operation operation = mOperationQueue.peek();
        if (operation != null) {
            operation.run();
        }
    }

    protected boolean resolveSleeping(boolean sleepingPrevious) {
        synchronized (mOperationQueue) {
            Operation operation = nextMatchedOperationLocked(OP_SLEEP);
            if (operation == null) {
                return false;
            }

            operation.promise.resolve(!sleepingPrevious);
            operateNextLocked();
            return true;
        }
    }

    private Operation nextMatchedOperationLocked(String operation) {
        Operation first = mOperationQueue.peek();
        if (first != null && first.name.equals(operation)) {
            return mOperationQueue.poll();
        }

        return null;
    }

    protected boolean rejectSleeping(AccessServiceException e) {
        if (e == null) {
            throw new IllegalArgumentException("Argument e is null.");
        }

        synchronized (mOperationQueue) {
            Operation operation = nextMatchedOperationLocked(OP_SLEEP);
            if (operation == null) {
                return false;
            }

            operation.promise.reject(e);
            operateNextLocked();
            return true;
        }
    }

    @Override
    public Promise<Boolean, AccessServiceException> isSleeping() {
        AsyncTask<Boolean, AccessServiceException> task = createGettingIfSleepingTask();
        if (task == null) {
            throw new IllegalStateException("createGettingIfSleepingTask returns null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<Boolean, AccessServiceException> createGettingIfSleepingTask();

    @Override
    public Promise<Boolean, AccessServiceException> wakeUp() {
        synchronized (mOperationQueue) {
            Operation operation = new Operation(OP_WAKE_UP) {
                @Override
                public void run() {
                    startWakingUp();
                }
            };
            mOperationQueue.add(operation);

            if (mOperationQueue.size() == 1) {
                operateNextLocked();
            }
            return operation.promise;
        }
    }

    protected abstract void startWakingUp();

    protected boolean resolveWakingUp(boolean wakingPrevious) {
        synchronized (mOperationQueue) {
            Operation operation = nextMatchedOperationLocked(OP_WAKE_UP);
            if (operation == null) {
                return false;
            }

            operation.promise.resolve(!wakingPrevious);
            operateNextLocked();
            return true;
        }
    }

    protected boolean rejectWakingUp(AccessServiceException e) {
        if (e == null) {
            throw new IllegalArgumentException("Argument e is null.");
        }

        synchronized (mOperationQueue) {
            Operation operation = nextMatchedOperationLocked(OP_WAKE_UP);
            if (operation == null) {
                return false;
            }

            operation.promise.reject(e);
            operateNextLocked();
            return true;
        }
    }

    @Override
    public Promise<Void, AccessServiceException> shutdown(ShutdownOption shutdownOption) {
        synchronized (this) {
            DefaultPromise<Void, AccessServiceException> ret = mShutdownPromise;
            if (mShutdownPromise == null) {
                mShutdownPromise = new DefaultPromise<>();
                ret = mShutdownPromise;

                startShutdown();
            }

            return ret;
        }
    }

    protected abstract void startShutdown();

    protected boolean resolveShutdown() {
        synchronized (this) {
            if (mShutdownPromise == null) {
                return false;
            }

            mShutdownPromise.resolve(null);
            return true;
        }
    }

    protected boolean rejectShutdown(AccessServiceException e) {
        if (e == null) {
            throw new IllegalArgumentException("Argument e is null.");
        }

        synchronized (this) {
            if (mShutdownPromise == null) {
                return false;
            }

            mShutdownPromise.reject(e);
            mShutdownPromise = null;
            return true;
        }
    }

    private abstract static class Operation implements Runnable {

        String name;
        final DefaultPromise<Boolean, AccessServiceException> promise;

        public Operation(String name) {
            this.name = name;
            this.promise = new DefaultPromise<>();
        }
    }
}