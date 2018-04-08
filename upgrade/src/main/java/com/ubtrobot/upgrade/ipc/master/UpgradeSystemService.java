package com.ubtrobot.upgrade.ipc.master;

import android.app.Application;

import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.master.service.MasterSystemService;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;
import com.ubtrobot.upgrade.ipc.UpgradeConstants;
import com.ubtrobot.upgrade.ipc.UpgradeConverters;
import com.ubtrobot.upgrade.sal.AbstractUpgradeService;
import com.ubtrobot.upgrade.sal.UpgradeFactory;
import com.ubtrobot.upgrade.sal.UpgradeService;

public class UpgradeSystemService extends MasterSystemService {

    private UpgradeService mUpgradeService;

    @Override
    protected void onServiceCreate() {
        Application application = getApplication();
        if (!(application instanceof UpgradeFactory)) {
            throw new IllegalStateException(
                    "Your application should implement UpgradeFactory interface.");
        }

        mUpgradeService = ((UpgradeFactory) application).createUpgradeService();
        if (mUpgradeService == null || !(mUpgradeService instanceof AbstractUpgradeService)) {
            throw new IllegalStateException("Your application 's createUpgradeService returns " +
                    "null or does not return a instance of AbstractUpgradeService.");
        }
    }

    @Call(path = UpgradeConstants.CALL_PATH_GET_FIRMWARE_LIST)
    public void onGetFirmwareList(Request request, Responder responder) {
        responder.respondSuccess(ProtoParam.create(
                UpgradeConverters.toFirmwareListProto(mUpgradeService.getFirmwareList())
        ));
    }
}