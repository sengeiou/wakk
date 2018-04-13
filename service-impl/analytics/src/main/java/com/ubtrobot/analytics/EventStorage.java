package com.ubtrobot.analytics;

import java.io.IOException;
import java.util.List;

public interface EventStorage {

    void writeEvent(Event event) throws IOException;

    void writeEvents(List<Event> events) throws IOException;

    int getEventCount() throws IOException;

    List<Event> readEvents(int count) throws IOException;

    void removeEvents(int count) throws IOException;
}
