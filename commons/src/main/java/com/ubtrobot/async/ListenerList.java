package com.ubtrobot.async;

import android.os.Handler;

import java.util.Iterator;
import java.util.LinkedList;

public class ListenerList<L> {

    private final Handler mHandler;

    private final LinkedList<L> mListeners = new LinkedList<>();

    public ListenerList(Handler handler) {
        this.mHandler = handler;
    }

    public void register(L listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Argument listener is null.");
        }

        synchronized (mListeners) {
            mListeners.add(listener);
        }
    }

    public void unregister(L listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Argument listener is null.");
        }

        synchronized (mListeners) {
            Iterator<L> iterator = mListeners.iterator();
            while (iterator.hasNext()) {
                L l = iterator.next();
                if (l == listener) {
                    iterator.remove();
                }
            }
        }
    }

    public boolean isEmpty() {
        synchronized (mListeners) {
            return mListeners.isEmpty();
        }
    }

    public void forEach(final Consumer<L> consumer) {
        if (consumer == null) {
            throw new IllegalArgumentException("Argument consumer is null.");
        }

        synchronized (mListeners) {
            for (final L listener : mListeners) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        consumer.accept(listener);
                    }
                });
            }
        }
    }
}
