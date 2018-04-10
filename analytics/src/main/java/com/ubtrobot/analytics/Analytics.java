package com.ubtrobot.analytics;

public interface Analytics {

    void enable(boolean enable);

    void setStrategy(Strategy strategy);

    Strategy getStrategy();

    void recordEvent(Event event);
}