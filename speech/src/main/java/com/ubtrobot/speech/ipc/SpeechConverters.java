package com.ubtrobot.speech.ipc;

import android.text.TextUtils;

import com.ubtrobot.speech.RecognizeOption;
import com.ubtrobot.speech.Recognizer;
import com.ubtrobot.speech.SynthesizeOption;
import com.ubtrobot.speech.Synthesizer;

public class SpeechConverters {

    private SpeechConverters() {
    }

    public static SpeechProto.SynthesizingProgress toSynthesizingProgressProto(
            Synthesizer.SynthesizingProgress progress) {
        return SpeechProto.SynthesizingProgress.newBuilder()
                .setState(progress.getState())
                .setProgress(progress.getProgress())
                .build();
    }

    public static Synthesizer.SynthesizingProgress toSynthesizingProgressPojo(
            SpeechProto.SynthesizingProgress progress) {
        return new Synthesizer.SynthesizingProgress(progress.getState(), progress.getProgress());
    }

    public static SpeechProto.SynthesizeOption toSynthesizeOptionProto(
            SynthesizeOption option, String sentence) {
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

    public static SpeechProto.RecognizingProgress toRecognizingProgressProto(
            Recognizer.RecognizingProgress progress) {
        if (progress.getState() == Recognizer.RecognizingProgress.STATE_RESULT) {
            return SpeechProto.RecognizingProgress.newBuilder()
                    .setState(progress.getState())
                    .setVolume(progress.getVolume())
                    .setResult(toRecognizeResultProto(progress.getResult()))
                    .build();
        } else {
            return SpeechProto.RecognizingProgress.newBuilder()
                    .setState(progress.getState())
                    .setVolume(progress.getVolume())
                    .build();
        }
    }

    public static Recognizer.RecognizingProgress toRecognizingProgressPojo
            (SpeechProto.RecognizingProgress progress) {
        Recognizer.RecognizingProgress.Builder builder = new Recognizer.RecognizingProgress.Builder(progress.getState());
        if (progress.getState() == Recognizer.RecognizingProgress.STATE_RESULT) {
            builder.setResult(toRecognizeResultPojo(progress.getResult()));

        }
        builder.setVolume(progress.getVolume());

        return builder.build();
    }

    public static Recognizer.RecognizeResult toRecognizeResultPojo(SpeechProto.RecognizeResult result) {
        return new Recognizer.RecognizeResult.Builder(result.getText()).build();
    }

    public static SpeechProto.RecognizeResult toRecognizeResultProto(Recognizer.RecognizeResult result) {
        return SpeechProto.RecognizeResult.newBuilder()
                .setText(result.getText())
                .build();
    }

    public static RecognizeOption toRecognizeOptionPojo(SpeechProto.RecognizeOption option) {
        return new RecognizeOption.Builder(option.getMode())
                .build();
    }

    public static SpeechProto.RecognizeOption toRecognizeOptionProto(RecognizeOption option) {
        return SpeechProto.RecognizeOption.newBuilder()
                .setMode(option.getMode())
                .build();
    }
}
