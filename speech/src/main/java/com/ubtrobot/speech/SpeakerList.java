package com.ubtrobot.speech;

import com.ubtrobot.cache.CachedField;
import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.speech.ipc.SpeechConstant;
import com.ubtrobot.speech.ipc.SpeechConverters;
import com.ubtrobot.speech.ipc.SpeechProto;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.util.Collections;
import java.util.List;

public class SpeakerList {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("SpeakerList");

    private ProtoCallAdapter mSpeechService;
    private final CachedField<List<Speaker>> mSpeakers;

    public SpeakerList(final ProtoCallAdapter mSpeechService) {
        this.mSpeechService = mSpeechService;
        mSpeakers = new CachedField<>(new CachedField.FieldGetter<List<Speaker>>() {
            @Override
            public List<Speaker> get() {
                try {
                    SpeechProto.Speakers speakers = mSpeechService.syncCall(
                            SpeechConstant.CALL_PATH_SPEAKER_LIST, SpeechProto.Speakers.class);
                    return Collections.unmodifiableList(
                            SpeechConverters.toSpeakersPojo(speakers));
                } catch (CallException e) {
                    LOGGER.e(e, "Framework error when getting the action list.");
                }
                return null;
            }
        });
    }

    public List<Speaker> all() {
        List<Speaker> speakerList = mSpeakers.get();
        return speakerList == null ? Collections.<Speaker>emptyList() : speakerList;
    }
}
