package com.ubtrobot.dance;

import android.content.Context;

import com.ubtrobot.cache.CachedField;
import com.ubtrobot.dance.parser.DanceParserImp;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.util.Collections;
import java.util.List;

public class DanceList {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("DanceList");

    private CachedField<List<Dance>> mDanceList;

    public DanceList(final Context context) {
        mDanceList = new CachedField<>(new CachedField.FieldGetter<List<Dance>>() {
            @Override
            public List<Dance> get() {
                DanceParserImp parserImp = new DanceParserImp(context);
                return parserImp.parser();
            }
        });
    }

    public List<Dance> all() {
        List<Dance> dances = mDanceList.get();
        return dances == null ? Collections.<Dance>emptyList() : dances;
    }

    public Dance get(String danceName) {
        for (Dance dance : all()) {
            if (dance.getName().equals(danceName)) {
                return dance;
            }
        }
        LOGGER.e("Dance name:" + danceName);
        throw new DanceNotFoundException();
    }

    public static class DanceNotFoundException extends RuntimeException {
    }
}
