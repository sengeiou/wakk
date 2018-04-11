package com.ubtrobot.analytics.sal;

import com.ubtrobot.analytics.Event;
import com.ubtrobot.analytics.Strategy;


public abstract class AbstractAnalyticsService implements AnalyticsService {

    private boolean mEnable = true;
    private Strategy mStrategy;

    public AbstractAnalyticsService(Strategy strategy) {
        mStrategy = strategy;
    }

    @Override
    public void enable(boolean enable) {
        mEnable = enable;
    }

    @Override
    public void setStrategy(Strategy strategy) {
        mStrategy = strategy;
        doSetStrategy(strategy);
    }

    protected abstract void doSetStrategy(Strategy strategy);

    @Override
    public Strategy getStrategy() {
        mStrategy = mStrategy != null ? mStrategy : doGetStrategy();
        return mStrategy;
    }

    protected abstract Strategy doGetStrategy();

    @Override
    public void recordEvent(Event event) {
        if (!mEnable || event == null) {
            return;
        }

        doRecordEvent(event);
    }

    protected abstract void doRecordEvent(Event event);
}
