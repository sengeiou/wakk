package com.ubtrobot.analytics;

import java.util.List;

public interface EventReporter {

    void reportEvents(List<Event> events) throws ReportException;
}
