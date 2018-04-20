package com.ubtrobot.master.competition;

import com.ubtrobot.async.DoneCallback;
import com.ubtrobot.async.FailCallback;
import com.ubtrobot.async.InterruptibleAsyncTask;
import com.ubtrobot.async.Promise;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class InterruptibleTaskHelper {

    private final HashMap<String, TaskEnv<?, ?, ?>> mTasks = new HashMap<>();

    public <D, F, P> Promise<D, F, P> start(
            Collection<String> receivers,
            String name,
            InterruptibleAsyncTask<D, F, P> task,
            InterruptedExceptionCreator<F> creator) {
        synchronized (mTasks) {
            Set<String> receiverSet = Collections.unmodifiableSet(new HashSet<>(receivers));
            Iterator<Map.Entry<String, TaskEnv<?, ?, ?>>> iterator = mTasks.entrySet().iterator();
            while (iterator.hasNext()) {
                TaskEnv<?, ?, ?> taskEnv = iterator.next().getValue();
                for (String receiver : receiverSet) {
                    if (taskEnv.receivers.contains(receiver)) {
                        taskEnv.interrupt(receiverSet);
                    }

                    iterator.remove();
                    break;
                }
            }

            final TaskEnv<D, F, P> newTaskEnv = new TaskEnv<>(receiverSet, name, task, creator);
            mTasks.put(newTaskEnv.receiverSeq, newTaskEnv);

            task.start();
            return task.promise().done(new DoneCallback<D>() {
                @Override
                public void onDone(D result) {
                    removeTask(newTaskEnv.id);
                }
            }).fail(new FailCallback<F>() {
                @Override
                public void onFail(F result) {
                    removeTask(newTaskEnv.id);
                }
            });
        }
    }

    private void removeTask(long id) {
        synchronized (mTasks) {
            Iterator<Map.Entry<String, TaskEnv<?, ?, ?>>> iterator = mTasks.entrySet().iterator();
            while (iterator.hasNext()) {
                TaskEnv<?, ?, ?> taskEnv = iterator.next().getValue();
                if (taskEnv.id == id) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    private static String reciverSeq(Collection<String> receivers) {
        LinkedList<String> receiverList = new LinkedList<>(receivers);
        Collections.sort(receiverList);
        return receiverList.toString();
    }

    public <D, F, P> Promise<D, F, P> start(
            String receiver,
            String name,
            InterruptibleAsyncTask<D, F, P> task,
            InterruptedExceptionCreator<F> creator) {
        return start(Collections.singleton(receiver), name, task, creator);
    }

    public boolean isRunning(String receiver, String name) {
        synchronized (mTasks) {
            for (TaskEnv<?, ?, ?> taskEnv : mTasks.values()) {
                if (taskEnv.receivers.contains(receiver) && taskEnv.name.equals(name)) {
                    return true;
                }
            }

            return false;
        }
    }

    public boolean isAllRunning(Collection<String> receivers, String name) {
        synchronized (mTasks) {
            String receiverSeq = reciverSeq(receivers);
            TaskEnv<?, ?, ?> taskEnv = mTasks.get(receiverSeq);
            return taskEnv != null && taskEnv.name.equals(name);
        }
    }

    @SuppressWarnings("unchecked")
    public <D> boolean resolve(Collection<String> receivers, String name, D done) {
        synchronized (mTasks) {
            String receiverSeq = reciverSeq(receivers);
            TaskEnv<?, ?, ?> taskEnv = mTasks.get(receiverSeq);
            if (taskEnv != null && taskEnv.name.equals(name)) {
                boolean ret = taskEnv.task.isPending();
                ((TaskEnv<D, ?, ?>) taskEnv).task.resolve(done);
                return ret;
            }

            return false;
        }
    }

    public <D> boolean resolve(String receiver, String name, D done) {
        return resolve(Collections.singleton(receiver), name, done);
    }

    @SuppressWarnings("unchecked")
    public <F> boolean reject(Collection<String> receivers, String name, F fail) {
        synchronized (mTasks) {
            String receiverSeq = reciverSeq(receivers);
            TaskEnv<?, ?, ?> taskEnv = mTasks.get(receiverSeq);
            if (taskEnv != null && taskEnv.name.equals(name)) {
                boolean ret = taskEnv.task.isPending();
                ((TaskEnv<?, F, ?>) taskEnv).task.reject(fail);
                return ret;
            }

            return false;
        }
    }

    public <F> boolean reject(String receiver, String name, F fail) {
        return reject(Collections.singleton(receiver), name, fail);
    }

    @SuppressWarnings("unchecked")
    public <P> boolean notify(Collection<String> receivers, String name, P progress) {
        synchronized (mTasks) {
            String receiverSeq = reciverSeq(receivers);
            TaskEnv<?, ?, ?> taskEnv = mTasks.get(receiverSeq);
            if (taskEnv != null && taskEnv.name.equals(name)) {
                boolean ret = taskEnv.task.isPending();
                ((TaskEnv<?, ?, P>) taskEnv).task.notify(progress);
                return ret;
            }

            return false;
        }
    }

    public <P> boolean notify(String receiver, String name, P progress) {
        return notify(Collections.singleton(receiver), name, progress);
    }

    private static class TaskEnv<D, F, P> {

        Set<String> receivers;
        String receiverSeq;
        String name;
        InterruptibleAsyncTask<D, F, P> task;
        InterruptedExceptionCreator<F> creator;
        long id = System.nanoTime();

        public TaskEnv(
                Set<String> receivers,
                String name,
                InterruptibleAsyncTask<D, F, P> task,
                InterruptedExceptionCreator<F> creator) {
            this.receivers = receivers;
            this.name = name;
            this.task = task;
            this.creator = creator;
            receiverSeq = reciverSeq(receivers);
        }

        void interrupt(Set<String> interrupters) {
            task.interrupt(creator.createInterruptedException(interrupters));
        }
    }

    public interface InterruptedExceptionCreator<E> {

        E createInterruptedException(Set<String> interrupters);
    }
}
