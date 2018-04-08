package com.ubtrobot.upgrade.sal;

import com.ubtrobot.cache.CachedField;
import com.ubtrobot.upgrade.Firmware;

import java.util.List;

public abstract class AbstractUpgradeService implements UpgradeService {

    private final CachedField<List<Firmware>> mFirmwareList;

    public AbstractUpgradeService() {
        mFirmwareList = new CachedField<>(new CachedField.FieldGetter<List<Firmware>>() {
            @Override
            public List<Firmware> get() {
                List<Firmware> firmwareList = doGetFirmwareList();
                if (firmwareList == null || firmwareList.isEmpty()) {
                    throw new IllegalStateException("doGetFirmwareList returns null or " +
                            "return a empty list.");
                }

                return firmwareList;
            }
        });
    }

    @Override
    public List<Firmware> getFirmwareList() {
        return mFirmwareList.get();
    }

    protected abstract List<Firmware> doGetFirmwareList();
}