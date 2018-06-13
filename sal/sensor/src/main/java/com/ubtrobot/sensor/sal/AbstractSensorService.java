package com.ubtrobot.sensor.sal;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.Master;
import com.ubtrobot.master.context.ContextRunnable;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.sensor.SensorDevice;
import com.ubtrobot.sensor.SensorEvent;
import com.ubtrobot.sensor.SensorException;
import com.ubtrobot.sensor.ipc.SensorConstants;
import com.ubtrobot.sensor.ipc.SensorConverters;
import com.ubtrobot.sensor.ipc.SensorProto;
import com.ubtrobot.sensor.ipc.master.SensorSystemService;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.util.List;

public abstract class AbstractSensorService implements SensorService {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("AbstractSensorService");

    @Override
    public Promise<List<SensorDevice>, AccessServiceException> getSensorList() {
        AsyncTask<List<SensorDevice>, AccessServiceException> task = createGettingSensorListTask();
        if (task == null) {
            throw new IllegalStateException("createGettingSensorListTask return null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<List<SensorDevice>, AccessServiceException> createGettingSensorListTask();

    protected void publishSensorEvent(final SensorEvent sensorEvent) {
        if (sensorEvent == null) {
            throw new IllegalArgumentException("Argument sensorEvent is null.");
        }

        boolean sensorSystemServiceStarted = Master.get().execute(
                SensorSystemService.class,
                new ContextRunnable<SensorSystemService>() {
                    @Override
                    public void run(SensorSystemService sensorSystemService) {
                        ProtoParam<SensorProto.SensorEvent> param = ProtoParam.create(
                                SensorConverters.toSensorEventProto(sensorEvent));

                        sensorSystemService.publish(SensorConstants.ACTION_SENSOR_CHANGE
                                + sensorEvent.getSensorId(), param);
                    }
                }
        );

        if (!sensorSystemServiceStarted) {
            LOGGER.e("Publish sensor event failed. Pls start SensorSystemService first.");
        }
    }

    @Override
    public Promise<Boolean, SensorException> enableSensor(String sensorId) {
        AsyncTask<Boolean, SensorException> task = createEnablingSensorTask(sensorId);
        if (task == null) {
            throw new IllegalStateException("createEnablingSensorTask returns null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<Boolean, SensorException>
    createEnablingSensorTask(String sensorId);

    @Override
    public Promise<Boolean, AccessServiceException> isSensorEnable(String sensorId) {
        AsyncTask<Boolean, AccessServiceException> task = createGettingIfSensorEnableTask(sensorId);
        if (task == null) {
            throw new IllegalStateException("createGettingIfSensorEnableTask returns null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<Boolean, AccessServiceException>
    createGettingIfSensorEnableTask(String sensorId);

    @Override
    public Promise<Boolean, SensorException> disableSensor(String sensorId) {
        AsyncTask<Boolean, SensorException> task = createDisablingSensorTask(sensorId);
        if (task == null) {
            throw new IllegalStateException("createDisablingSensorTask returns null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<Boolean, SensorException>
    createDisablingSensorTask(String sensorId);
}
