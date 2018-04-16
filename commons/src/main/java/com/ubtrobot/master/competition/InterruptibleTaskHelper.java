package com.ubtrobot.master.competition;

import com.ubtrobot.async.DoneCallback;
import com.ubtrobot.async.FailCallback;
import com.ubtrobot.async.InterruptibleAsyncTask;
import com.ubtrobot.async.Promise;

import java.util.HashMap;

public class InterruptibleTaskHelper {

    private final HashMap<String, TaskEnv<?, ?, ?>> mTasks = new HashMap<>();

    public <D, F, P> Promise<D, F, P> start(
            final String receiver,
            String name,
            InterruptibleAsyncTask<D, F, P> task,
            InterruptedExceptionCreator<F> creator) {
        synchronized (mTasks) {
            final TaskEnv<D, F, P> newTaskEnv = new TaskEnv<>(receiver, name, task, creator);
            TaskEnv<?, ?, ?> previousTaskEnv = mTasks.put(receiver, newTaskEnv);

            if (previousTaskEnv != null) {
                previousTaskEnv.interrupt(previousTaskEnv.name);
            }

            task.start();
            return task.promise().done(new DoneCallback<D>() {
                @Override
                public void onDone(D result) {
                    removeTask(receiver, newTaskEnv.id);
                }
            }).fail(new FailCallback<F>() {
                @Override
                public void onFail(F result) {
                    removeTask(receiver, newTaskEnv.id);
                }
            });
        }
    }

    private void removeTask(String receiver, long id) {
        synchronized (mTasks) {
            TaskEnv<?, ?, ?> taskEnv = mTasks.get(receiver);
            if (taskEnv != null && taskEnv.id == id) {
                mTasks.remove(receiver);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <D> boolean resolve(String receiver, String name, D done) {
        synchronized (mTasks) {
            TaskEnv<?, ?, ?> taskEnv = mTasks.get(receiver);
            if (taskEnv.name.equals(name)) {
                boolean ret = taskEnv.task.isPending();
                ((TaskEnv<D, ?, ?>) taskEnv).task.resolve(done);
                return ret;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public <F> boolean reject(String receiver, String name, F fail) {
        synchronized (mTasks) {
            TaskEnv<?, ?, ?> taskEnv = mTasks.get(receiver);
            if (taskEnv.name.equals(name)) {
                boolean ret = taskEnv.task.isPending();
                ((TaskEnv<?, F, ?>) taskEnv).task.reject(fail);
                return ret;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public <P> boolean notify(String receiver, String name, P progress) {
        synchronized (mTasks) {
            TaskEnv<?, ?, ?> taskEnv = mTasks.get(receiver);
            if (taskEnv.name.equals(name)) {
                boolean ret = taskEnv.task.isPending();
                ((TaskEnv<?, ?, P>) taskEnv).task.notify(progress);
                return ret;
            }
        }

        return false;
    }

    private static class TaskEnv<D, F, P> {

        String receiver;
        String name;
        InterruptibleAsyncTask<D, F, P> task;
        InterruptedExceptionCreator<F> creator;
        long id = System.nanoTime();

        public TaskEnv(
                String receiver,
                String name,
                InterruptibleAsyncTask<D, F, P> task,
                InterruptedExceptionCreator<F> creator) {
            this.receiver = receiver;
            this.name = name;
            this.task = task;
            this.creator = creator;
        }

        void interrupt(String interrupter) {
            task.interrupt(creator.createInterruptedException(interrupter));
        }
    }

    public interface InterruptedExceptionCreator<E> {

        E createInterruptedException(String interrupter);
    }
}
