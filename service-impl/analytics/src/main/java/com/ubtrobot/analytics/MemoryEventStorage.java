package com.ubtrobot.analytics;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MemoryEventStorage implements EventStorage {

    private static final int DEFAULT_MAX_EVENT_CAPACITY = 1024 * 1024 * 1024 / 60;  //1条记录:60bytes

    private final LinkedList<Event> EVENTS = new LinkedList<>();
    private final int MAX_EVENT_CAPACITY;

    public MemoryEventStorage() {
        this(DEFAULT_MAX_EVENT_CAPACITY);
    }

    /**
     * 内存里允许的最大Event数量
     *
     * @param maxEventCapacity
     */
    public MemoryEventStorage(int maxEventCapacity) {
        MAX_EVENT_CAPACITY = maxEventCapacity;
    }

    @Override
    public void writeEvent(Event event) throws IOException {
        synchronized (EVENTS) {
            EVENTS.add(event);
        }
    }

    @Override
    public void writeEvents(List<Event> events) throws IOException {
        synchronized (EVENTS) {
            EVENTS.addAll(events);

            if (EVENTS.size() <= MAX_EVENT_CAPACITY) {
                return;
            }

            removeEvents(EVENTS.size() - MAX_EVENT_CAPACITY);
        }
    }

    @Override
    public int getEventCount() throws IOException {
        synchronized (EVENTS) {
            return EVENTS.size();
        }
    }

    @Override
    public List<Event> readEvents(int count) throws IOException {
        synchronized (EVENTS) {
            // 内部保证 count > 0
            return new LinkedList<>(EVENTS.subList(0, Math.min(count, EVENTS.size())));
        }
    }

    @Override
    public void removeEvents(int count) throws IOException {
        synchronized (EVENTS) {
            int deleted = 0;
            Iterator<Event> iterator = EVENTS.iterator();

            while (iterator.hasNext() && deleted < count) {
                iterator.next();
                iterator.remove();

                deleted++;
            }
        }
    }
}
