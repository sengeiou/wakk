package com.ubtrobot.power.sal;

import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.power.ShutdownOption;

public interface PowerService {

    Promise<Boolean, AccessServiceException> sleep();

    Promise<Boolean, AccessServiceException> isSleeping();

    Promise<Boolean, AccessServiceException> wakeUp();

    Promise<Void, AccessServiceException> shutdown(ShutdownOption shutdownOption);
}
