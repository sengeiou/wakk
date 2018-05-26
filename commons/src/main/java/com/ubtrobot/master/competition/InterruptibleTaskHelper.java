package com.ubtrobot.master.competition;

import com.ubtrobot.async.DoneCallback;
import com.ubtrobot.async.FailCallback;
import com.ubtrobot.async.InterruptibleAsyncTask;
import com.ubtrobot.async.InterruptibleProgressiveAsyncTask;
import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class InterruptibleTaskHelper {

    private final HashMap<String, TaskEnv<?, ?, ?>> mTasks = new HashMap<>();
    private final HashMap<String, TaskEnv<?, ?, ?>> mSessionTaskMap = new HashMap<>();

    private static final String SESSION_PREFIX = UUID.randomUUID().toString().substring(0, 4) + "-";
    private final AtomicInteger mSessionCount = new AtomicInteger(0);

    public <D, F extends Throwable, P> ProgressivePromise<D, F, P> start(
            Collection<String> receivers,
            String name,
            Session outSession,
            InterruptibleProgressiveAsyncTask<D, F, P> task,
            InterruptedExceptionCreator<F> creator) {
        synchronized (mTasks) {
            outSession.id = nextSessionId();

            Set<String> receiverSet = Collections.unmodifiableSet(new HashSet<>(receivers));
            for (TaskEnv<?, ?, ?> taskEnv : mTasks.values()) {
                for (String receiver : receiverSet) {
                    if (taskEnv.receivers.contains(receiver)) {
                        taskEnv.interrupt(receiverSet);
                    }
                }
            }

            final TaskEnv<D, F, P> newTaskEnv = new TaskEnv<>(receiverSet, name, outSession,
                    task, creator);
            mTasks.put(newTaskEnv.receiverSeq, newTaskEnv);
            mSessionTaskMap.put(outSession.id, newTaskEnv);

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

    private String nextSessionId() {
        return SESSION_PREFIX + mSessionCount.incrementAndGet();
    }

    private void removeTask(long id) {
        synchronized (mTasks) {
            Iterator<Map.Entry<String, TaskEnv<?, ?, ?>>> iterator = mTasks.entrySet().iterator();
            while (iterator.hasNext()) {
                TaskEnv<?, ?, ?> taskEnv = iterator.next().getValue();
                if (taskEnv.id == id) {
                    iterator.remove();
                    mSessionTaskMap.remove(taskEnv.session.id);
                    break;
                }
            }
        }
    }

    private static String receiverSeq(Collection<String> receivers) {
        LinkedList<String> receiverList = new LinkedList<>(receivers);
        Collections.sort(receiverList);
        return receiverList.toString();
    }

    public <D, F extends Throwable, P> ProgressivePromise<D, F, P> start(
            String receiver,
            String name,
            InterruptibleProgressiveAsyncTask<D, F, P> task,
            InterruptedExceptionCreator<F> creator) {
        return start(Collections.singleton(receiver), name, new Session(), task, creator);
    }

    public <D, F extends Throwable> Promise<D, F> start(
            Collection<String> receivers,
            String name,
            Session outSession,
            InterruptibleAsyncTask<D, F> task,
            InterruptedExceptionCreator<F> creator) {
        return start(receivers, name, outSession,
                (InterruptibleProgressiveAsyncTask<D, F, Void>) task, creator);
    }

    public <D, F extends Throwable> Promise<D, F> start(
            String receiver,
            String name,
            InterruptibleAsyncTask<D, F> task,
            InterruptedExceptionCreator<F> creator) {
        return start(Collections.singleton(receiver), name, new Session(),
                (InterruptibleProgressiveAsyncTask<D, F, Void>) task, creator);
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
            String receiverSeq = receiverSeq(receivers);
            TaskEnv<?, ?, ?> taskEnv = mTasks.get(receiverSeq);
            return taskEnv != null && taskEnv.name.equals(name);
        }
    }

    @SuppressWarnings("unchecked")
    public <D> boolean resolve(Collection<String> receivers, String name, D done) {
        synchronized (mTasks) {
            String receiverSeq = receiverSeq(receivers);
            TaskEnv<?, ?, ?> taskEnv = mTasks.get(receiverSeq);
            if (taskEnv != null && taskEnv.name.equals(name)) {
                boolean ret = taskEnv.task.promise().isPending();
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
    public <D> boolean resolve(String sessionId, D done) {
        synchronized (mTasks) {
            TaskEnv<?, ?, ?> taskEnv = mSessionTaskMap.get(sessionId);
            if (taskEnv != null) {
                boolean ret = taskEnv.task.promise().isPending();
                ((TaskEnv<D, ?, ?>) taskEnv).task.resolve(done);
                return ret;
            }

            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public <F extends Throwable> boolean reject(Collection<String> receivers, String name, F fail) {
        synchronized (mTasks) {
            String receiverSeq = receiverSeq(receivers);
            TaskEnv<?, ?, ?> taskEnv = mTasks.get(receiverSeq);
            if (taskEnv != null && taskEnv.name.equals(name)) {
                boolean ret = taskEnv.task.promise().isPending();
                ((TaskEnv<?, F, ?>) taskEnv).task.reject(fail);
                return ret;
            }

            return false;
        }
    }

    public <F extends Throwable> boolean reject(String receiver, String name, F fail) {
        return reject(Collections.singleton(receiver), name, fail);
    }

    @SuppressWarnings("unchecked")
    public <F extends Throwable> boolean reject(String sessionId, F fail) {
        synchronized (mTasks) {
            TaskEnv<?, ?, ?> taskEnv = mSessionTaskMap.get(sessionId);
            if (taskEnv != null) {
                boolean ret = taskEnv.task.promise().isPending();
                ((TaskEnv<?, F, ?>) taskEnv).task.reject(fail);
                return ret;
            }

            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public <P> boolean report(Collection<String> receivers, String name, P progress) {
        synchronized (mTasks) {
            String receiverSeq = receiverSeq(receivers);
            TaskEnv<?, ?, ?> taskEnv = mTasks.get(receiverSeq);
            if (taskEnv != null && taskEnv.name.equals(name)) {
                boolean ret = taskEnv.task.promise().isPending();
                ((TaskEnv<?, ?, P>) taskEnv).task.report(progress);
                return ret;
            }

            return false;
        }
    }

    public <P> boolean report(String receiver, String name, P progress) {
        return report(Collections.singleton(receiver), name, progress);
    }

    @SuppressWarnings("unchecked")
    public <P> boolean report(String sessionId, P progress) {
        synchronized (mTasks) {
            TaskEnv<?, ?, ?> taskEnv = mSessionTaskMap.get(sessionId);
            if (taskEnv != null) {
                boolean ret = taskEnv.task.promise().isPending();
                ((TaskEnv<?, ?, P>) taskEnv).task.report(progress);
                return ret;
            }

            return false;
        }
    }

    private static class TaskEnv<D, F extends Throwable, P> {

        Set<String> receivers;
        String receiverSeq;
        String name;
        Session session;
        InterruptibleProgressiveAsyncTask<D, F, P> task;
        InterruptedExceptionCreator<F> creator;
        long id = System.nanoTime();

        public TaskEnv(
                Set<String> receivers,
                String name,
                Session session,
                InterruptibleProgressiveAsyncTask<D, F, P> task,
                InterruptedExceptionCreator<F> creator) {
            this.receivers = receivers;
            this.name = name;
            this.session = session;
            this.task = task;
            this.creator = creator;
            receiverSeq = receiverSeq(receivers);
        }

        void interrupt(Set<String> interrupters) {
            task.interrupt(creator.createInterruptedException(interrupters));
        }
    }

    public interface InterruptedExceptionCreator<E> {

        E createInterruptedException(Set<String> interrupters);
    }

    public static class Session {

        private String id;

        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return "Session{" +
                    "id='" + id + '\'' +
                    '}';
        }
    }
}
