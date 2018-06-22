package com.ubtrobot.power.sal;

import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.power.BatteryProperties;
import com.ubtrobot.power.ChargeException;
import com.ubtrobot.power.ConnectOption;

public interface PowerService {

    Promise<Boolean, AccessServiceException> sleep();

    Promise<Boolean, AccessServiceException> isSleeping();

    Promise<Boolean, AccessServiceException> wakeUp();

    Promise<Void, AccessServiceException> shutdown();

    Promise<Void, AccessServiceException> scheduleStartup(int waitSecondsToStartup);

    Promise<Boolean, AccessServiceException> cancelStartupSchedule();

    Promise<BatteryProperties, AccessServiceException> getBatteryProperties();

    Promise<Boolean, ChargeException> connectToChargingStation(ConnectOption option);

    Promise<Boolean, AccessServiceException> isConnectedToChargingStation();

    Promise<Boolean, ChargeException> disconnectFromChargingStation();
}