package com.ubtrobot.wakeup.sal;

import com.ubtrobot.wakeup.WakeupListener;

public interface WakeupService {

    void registerWakeupListener(WakeupListener listener);

    void unregisterWakeupListener(WakeupListener listener);

}
