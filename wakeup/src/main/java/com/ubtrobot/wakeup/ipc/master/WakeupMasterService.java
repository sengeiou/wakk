package com.ubtrobot.wakeup.ipc.master;

import android.app.Application;

import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.master.service.MasterSystemService;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;
import com.ubtrobot.wakeup.WakeupEvent;
import com.ubtrobot.wakeup.WakeupListener;
import com.ubtrobot.wakeup.ipc.WakeupConstants;
import com.ubtrobot.wakeup.ipc.WakeupConverter;
import com.ubtrobot.wakeup.sal.AbstractWakeupService;
import com.ubtrobot.wakeup.sal.WakeupFactory;
import com.ubtrobot.wakeup.sal.WakeupService;

public class WakeupMasterService extends MasterSystemService {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("WakeupMasterService");

    private WakeupService mWakeupService;

    @Override
    protected void onServiceCreate() {
        Application application = getApplication();
        if (!(application instanceof WakeupFactory)) {
            throw new RuntimeException("Application must implement SpeechFactory");
        }

        mWakeupService = ((WakeupFactory) application).createWakeupService();
        if (mWakeupService == null) {
            throw new RuntimeException("Application must return service by createService()");
        }

        if (!(mWakeupService instanceof AbstractWakeupService)) {
            throw new IllegalStateException(
                    "Application must return a AbstractWakeupService instance");
        }

        mWakeupService.registerWakeupListener(mListener);
    }

    @Override
    protected void onServiceDestroy() {
        mWakeupService.unregisterWakeupListener(mListener);
    }

    private WakeupListener mListener = new WakeupListener() {

        @Override
        public void onWakeup(WakeupEvent event) {
            LOGGER.i("Publish wakeup event. event=%s", event.toString());
            publishCarefully(
                    WakeupConstants.ACTION_WAKEUP,
                    ProtoParam.create(
                            WakeupConverter.toWakeUpEventProto(event)
                    )
            );
        }
    };
}
