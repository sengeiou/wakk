package com.ubtrobot.power.sal;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.async.DefaultPromise;
import com.ubtrobot.async.InterruptibleAsyncTask;
import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.Master;
import com.ubtrobot.master.competition.InterruptibleTaskHelper;
import com.ubtrobot.master.context.ContextRunnable;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.power.BatteryProperties;
import com.ubtrobot.power.ChargeException;
import com.ubtrobot.power.ConnectOption;
import com.ubtrobot.power.ipc.PowerConstants;
import com.ubtrobot.power.ipc.PowerConverters;
import com.ubtrobot.power.ipc.master.PowerSystemService;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.util.LinkedList;
import java.util.Set;

public abstract class AbstractPowerService implements PowerService {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("AbstractPowerService");

    private static final String TASK_RECEIVER_CHARGING_STATION_CONNECTION = "charging-station-connection";
    private static final String TASK_NAME_CONNECT = "connect";
    private static final String TASK_NAME_DISCONNECT = "disconnect";

    private static final String OP_SLEEP = "sleep";
    private static final String OP_WAKE_UP = "wake-up";

    private final LinkedList<SleepWakeUpOperation> mSleepWakeUpOperationQueue = new LinkedList<>();

    private DefaultPromise<Void, AccessServiceException> mShutdownPromise;

    private final InterruptibleTaskHelper mInterruptibleTaskHelper = new InterruptibleTaskHelper();

    @Override
    public Promise<Boolean, AccessServiceException> sleep() {
        synchronized (mSleepWakeUpOperationQueue) {
            SleepWakeUpOperation operation = new SleepWakeUpOperation(OP_SLEEP) {
                @Override
                public void run() {
                    startSleeping();
                }
            };
            mSleepWakeUpOperationQueue.add(operation);

            if (mSleepWakeUpOperationQueue.size() == 1) {
                operateNextLocked();
            }
            return operation.promise;
        }
    }

    protected abstract void startSleeping();

    private void operateNextLocked() {
        final SleepWakeUpOperation operation = mSleepWakeUpOperationQueue.peek();
        if (operation != null) {
            operation.run();
        }
    }

    protected boolean resolveSleeping(boolean sleepingPrevious) {
        synchronized (mSleepWakeUpOperationQueue) {
            SleepWakeUpOperation operation = nextMatchedOperationLocked(OP_SLEEP);
            if (operation == null) {
                return false;
            }

            operation.promise.resolve(!sleepingPrevious);
            operateNextLocked();
            return true;
        }
    }

    private SleepWakeUpOperation nextMatchedOperationLocked(String operation) {
        SleepWakeUpOperation first = mSleepWakeUpOperationQueue.peek();
        if (first != null && first.name.equals(operation)) {
            return mSleepWakeUpOperationQueue.poll();
        }

        return null;
    }

    protected boolean rejectSleeping(AccessServiceException e) {
        if (e == null) {
            throw new IllegalArgumentException("Argument e is null.");
        }

        synchronized (mSleepWakeUpOperationQueue) {
            SleepWakeUpOperation operation = nextMatchedOperationLocked(OP_SLEEP);
            if (operation == null) {
                return false;
            }

            operation.promise.reject(e);
            operateNextLocked();
            return true;
        }
    }

    @Override
    public Promise<Boolean, AccessServiceException> isSleeping() {
        AsyncTask<Boolean, AccessServiceException> task = createGettingIfSleepingTask();
        if (task == null) {
            throw new IllegalStateException("createGettingIfSleepingTask returns null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<Boolean, AccessServiceException> createGettingIfSleepingTask();

    @Override
    public Promise<Boolean, AccessServiceException> wakeUp() {
        synchronized (mSleepWakeUpOperationQueue) {
            SleepWakeUpOperation operation = new SleepWakeUpOperation(OP_WAKE_UP) {
                @Override
                public void run() {
                    startWakingUp();
                }
            };
            mSleepWakeUpOperationQueue.add(operation);

            if (mSleepWakeUpOperationQueue.size() == 1) {
                operateNextLocked();
            }
            return operation.promise;
        }
    }

    protected abstract void startWakingUp();

    protected boolean resolveWakingUp(boolean wakingPrevious) {
        synchronized (mSleepWakeUpOperationQueue) {
            SleepWakeUpOperation operation = nextMatchedOperationLocked(OP_WAKE_UP);
            if (operation == null) {
                return false;
            }

            operation.promise.resolve(!wakingPrevious);
            operateNextLocked();
            return true;
        }
    }

    protected boolean rejectWakingUp(AccessServiceException e) {
        if (e == null) {
            throw new IllegalArgumentException("Argument e is null.");
        }

        synchronized (mSleepWakeUpOperationQueue) {
            SleepWakeUpOperation operation = nextMatchedOperationLocked(OP_WAKE_UP);
            if (operation == null) {
                return false;
            }

            operation.promise.reject(e);
            operateNextLocked();
            return true;
        }
    }

    @Override
    public Promise<Void, AccessServiceException> shutdown() {
        synchronized (this) {
            DefaultPromise<Void, AccessServiceException> ret = mShutdownPromise;
            if (mShutdownPromise == null) {
                mShutdownPromise = new DefaultPromise<>();
                ret = mShutdownPromise;

                startShutdown();
            }

            return ret;
        }
    }

    protected abstract void startShutdown();

    protected boolean resolveShutdown() {
        synchronized (this) {
            if (mShutdownPromise == null) {
                return false;
            }

            mShutdownPromise.resolve(null);
            return true;
        }
    }

    protected boolean rejectShutdown(AccessServiceException e) {
        if (e == null) {
            throw new IllegalArgumentException("Argument e is null.");
        }

        synchronized (this) {
            if (mShutdownPromise == null) {
                return false;
            }

            mShutdownPromise.reject(e);
            mShutdownPromise = null;
            return true;
        }
    }

    @Override
    public Promise<Void, AccessServiceException> scheduleStartup(int waitSecondsToStartup) {
        AsyncTask<Void, AccessServiceException> task = createSchedulingStartupTask(waitSecondsToStartup);
        if (task == null) {
            throw new IllegalStateException("createSchedulingStartupTask returns null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<Void, AccessServiceException>
    createSchedulingStartupTask(int waitSecondsToStartup);

    @Override
    public Promise<Boolean, AccessServiceException> cancelStartupSchedule() {
        AsyncTask<Boolean, AccessServiceException> task = createCancelingStartupScheduleTask();
        if (task == null) {
            throw new IllegalStateException("createCancelingStartupScheduleTask returns null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<Boolean, AccessServiceException> createCancelingStartupScheduleTask();

    @Override
    public Promise<BatteryProperties, AccessServiceException> getBatteryProperties() {
        AsyncTask<BatteryProperties, AccessServiceException> task
                = createGettingBatteryPropertiesTask();
        if (task == null) {
            throw new IllegalStateException("createGettingBatteryPropertiesTask returns null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<BatteryProperties, AccessServiceException>
    createGettingBatteryPropertiesTask();

    protected void notifyBatteryChanged(final BatteryProperties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("Argument properties is null.");
        }

        boolean powerServiceStarted = Master.get().execute(
                PowerSystemService.class,
                new ContextRunnable<PowerSystemService>() {
                    @Override
                    public void run(PowerSystemService powerSystemService) {
                        powerSystemService.publish(
                                PowerConstants.ACTION_BATTERY_CHANGE,
                                ProtoParam.create(PowerConverters.toBatteryPropertiesProto(properties))
                        );
                    }
                }
        );

        if (!powerServiceStarted) {
            LOGGER.e("Publish sensor event failed. Pls start SensorSystemService first.");
        }
    }

    @Override
    public Promise<Boolean, ChargeException> connectToChargingStation(final ConnectOption option) {
        return mInterruptibleTaskHelper.start(
                TASK_RECEIVER_CHARGING_STATION_CONNECTION,
                TASK_NAME_CONNECT,
                new InterruptibleAsyncTask<Boolean, ChargeException>() {
                    @Override
                    protected void onStart() {
                        startConnectingToChargingStation(option);
                    }

                    @Override
                    protected void onCancel() {
                        stopConnectingToChargingStation();
                    }
                },
                new InterruptibleTaskHelper.InterruptedExceptionCreator<ChargeException>() {
                    @Override
                    public ChargeException createInterruptedException(Set<String> interrupters) {
                        return new ChargeException.Factory().interrupted("Interrupted by "
                                + interrupters + ".");
                    }
                }
        );
    }

    protected abstract void startConnectingToChargingStation(ConnectOption option);

    protected abstract void stopConnectingToChargingStation();

    protected boolean resolveConnectingToChargingStation(boolean disconnectedPrevious) {
        return mInterruptibleTaskHelper.resolve(TASK_RECEIVER_CHARGING_STATION_CONNECTION,
                TASK_NAME_CONNECT, disconnectedPrevious);
    }

    protected boolean rejectConnectingToChargingStation(ChargeException e) {
        if (e == null) {
            throw new IllegalArgumentException("Argument e is null.");
        }

        return mInterruptibleTaskHelper.reject(TASK_RECEIVER_CHARGING_STATION_CONNECTION,
                TASK_NAME_CONNECT, e);
    }

    @Override
    public Promise<Boolean, AccessServiceException> isConnectedToChargingStation() {
        AsyncTask<Boolean, AccessServiceException> task =
                createGettingIfConnectedToChargingStationTask();
        if (task == null) {
            throw new IllegalStateException("createGettingIfConnectedToChargingStationTask returns null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<Boolean, AccessServiceException>
    createGettingIfConnectedToChargingStationTask();

    @Override
    public Promise<Boolean, ChargeException> disconnectFromChargingStation() {
        return mInterruptibleTaskHelper.start(
                TASK_RECEIVER_CHARGING_STATION_CONNECTION,
                TASK_NAME_DISCONNECT,
                new InterruptibleAsyncTask<Boolean, ChargeException>() {
                    @Override
                    protected void onStart() {
                        startDisconnectingFromChargingStation();
                    }

                    @Override
                    protected void onCancel() {
                        stopDisconnectingFromChargingStation();
                    }
                },
                new InterruptibleTaskHelper.InterruptedExceptionCreator<ChargeException>() {
                    @Override
                    public ChargeException createInterruptedException(Set<String> interrupters) {
                        return new ChargeException.Factory().interrupted("Interrupted by "
                                + interrupters + ".");
                    }
                }
        );
    }

    protected abstract void startDisconnectingFromChargingStation();

    protected abstract void stopDisconnectingFromChargingStation();

    protected boolean resolveDisconnectingFromChargingStation(boolean connectedPrevious) {
        return mInterruptibleTaskHelper.resolve(TASK_RECEIVER_CHARGING_STATION_CONNECTION,
                TASK_NAME_DISCONNECT, connectedPrevious);
    }

    protected boolean rejectDisconnectingFromChargingStation(ChargeException e) {
        if (e == null) {
            throw new IllegalArgumentException("Argument e is null.");
        }

        return mInterruptibleTaskHelper.reject(TASK_RECEIVER_CHARGING_STATION_CONNECTION,
                TASK_NAME_DISCONNECT, e);
    }

    private abstract static class SleepWakeUpOperation implements Runnable {

        String name;
        final DefaultPromise<Boolean, AccessServiceException> promise;

        public SleepWakeUpOperation(String name) {
            this.name = name;
            this.promise = new DefaultPromise<>();
        }
    }
}
