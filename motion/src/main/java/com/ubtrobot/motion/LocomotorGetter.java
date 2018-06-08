package com.ubtrobot.motion;

import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.ubtrobot.cache.CachedField;
import com.ubtrobot.device.ipc.DeviceProto;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.motion.ipc.MotionConstants;
import com.ubtrobot.motion.ipc.MotionConverters;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

public class LocomotorGetter {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("LocomotorGetter");

    private final CachedField<Locomotor> mLocomotor;

    LocomotorGetter(final ProtoCallAdapter motionService) {
        mLocomotor = new CachedField<>(new CachedField.FieldGetter<Locomotor>() {
            @Override
            public Locomotor get() {
                try {
                    DeviceProto.Device device = motionService.syncCall(
                            MotionConstants.CALL_PATH_GET_LOCOMOTOR, DeviceProto.Device.class);
                    if (TextUtils.isEmpty(device.getId())) {
                        return null;
                    }

                    return new Locomotor(motionService, MotionConverters.toLocomotorDevicePojo(device));
                } catch (CallException e) {
                    LOGGER.e(e, "Framework error when getting the locomotor device.");
                } catch (InvalidProtocolBufferException e) {
                    LOGGER.e(e, "Framework error when getting the locomotor device.");
                }

                return null;
            }
        });
    }

    public Locomotor get() {
        Locomotor locomotor = mLocomotor.get();
        if (locomotor == null) {
            throw new LocomotorNotFoundException();
        }

        return locomotor;
    }

    public boolean exists() {
        return mLocomotor.get() != null;
    }

    public static class LocomotorNotFoundException extends RuntimeException {
    }
}