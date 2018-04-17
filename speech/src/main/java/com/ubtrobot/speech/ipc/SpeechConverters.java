package com.ubtrobot.speech.ipc;

import android.text.TextUtils;

import com.ubtrobot.speech.SynthesizeOption;
import com.ubtrobot.speech.Synthesizer;

public class SpeechConverters {

    private SpeechConverters() {
    }

    public static SpeechProto.SynthesizingProgress toSynthesizingProgressProto(Synthesizer.SynthesizingProgress progress) {
        return SpeechProto.SynthesizingProgress.newBuilder()
                .setState(progress.getState())
                .setProgress(progress.getProgress())
                .build();
    }

    public static Synthesizer.SynthesizingProgress toSynthesizingProgressPojo(SpeechProto.SynthesizingProgress progress) {
        return new Synthesizer.SynthesizingProgress(progress.getState(), progress.getProgress());
    }

    public static SpeechProto.SynthesizeOption toSynthesizeOptionProto(SynthesizeOption option, String sentence) {
        if (TextUtils.isEmpty(sentence)) {
            throw new IllegalArgumentException("SpeechProto.SynthesizeOption refuse null sentence.");
        }

        return SpeechProto.SynthesizeOption.newBuilder()
                .setSentence(sentence)
                .setSpeakerId(option.getSpeakerId())
                .setSpeakSpeed(option.getSpeakingSpeed())
                .setSpeakVolume(option.getSpeakingVolume())
                .build();
    }

    public static SynthesizeOption toSynthesizeOptionPojo(SpeechProto.SynthesizeOption option) {
        return new SynthesizeOption.Builder()
                .setSpeakerId(option.getSpeakerId())
                .setSpeakingSpeed(option.getSpeakSpeed())
                .setSpeakingVolume(option.getSpeakVolume())
                .build();
    }
}
