package com.ubtrobot.analytics.sal;

import com.ubtrobot.analytics.Event;
import com.ubtrobot.analytics.Strategy;

import java.util.Map;

public abstract class AbstractAnalyticsService implements AnalyticsService {

    private Strategy mStrategy;

    public AbstractAnalyticsService(Strategy strategy) {
        mStrategy = strategy;
    }

    @Override
    public void enable(boolean enable) {
    }

    @Override
    public void setStrategy(Strategy strategy) {
        mStrategy = strategy;
    }

    @Override
    public Strategy getStrategy() {
        return null;
    }

    @Override
    public void recordEvent(Event event) {

    }
}
