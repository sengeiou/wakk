package com.ubtrobot.analytics;

public class EventTable {

    public static final int RAN_SHUTDOWN = 1;

    private Event event;
    private int ranShutdown;

    public EventTable setEvent(Event event) {
        this.event = event;
        return this;
    }

    public EventTable setRanShutdown(int ranShutdown) {
        this.ranShutdown = ranShutdown;
        return this;
    }

    public Event getEvent() {
        return event;
    }

    public int getRanShutdown() {
        return ranShutdown;
    }

    public boolean causedByRanShutdown() {
        return ranShutdown == RAN_SHUTDOWN;
    }

    @Override
    public String toString() {
        return "EventTable{" +
                "event=" + event.toString() +
                ", ranShutdown=" + ranShutdown +
                '}';
    }
}
