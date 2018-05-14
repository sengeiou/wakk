package com.ubtrobot.speech.ipc;

import android.text.TextUtils;

import com.ubtrobot.speech.Configuration;
import com.ubtrobot.speech.RecognizeOption;
import com.ubtrobot.speech.Recognizer;
import com.ubtrobot.speech.Speaker;
import com.ubtrobot.speech.SynthesizeOption;
import com.ubtrobot.speech.Synthesizer;
import com.ubtrobot.speech.UnderstandOption;
import com.ubtrobot.speech.Understander;
import com.ubtrobot.speech.understand.UnderstandResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
            throw new IllegalArgumentException(
                    "SpeechProto.SynthesizeOption refuse null sentence.");
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
        Recognizer.RecognizingProgress.Builder builder = new Recognizer.RecognizingProgress.Builder(
                progress.getState());

        if (progress.getState() == Recognizer.RecognizingProgress.STATE_RESULT) {
            builder.setResult(toRecognizeResultPojo(progress.getResult()));

        }
        builder.setVolume(progress.getVolume());

        return builder.build();
    }

    public static Recognizer.RecognizeResult toRecognizeResultPojo(
            SpeechProto.RecognizeResult result) {
        return new Recognizer.RecognizeResult.Builder(result.getText()).build();
    }

    public static SpeechProto.RecognizeResult toRecognizeResultProto(
            Recognizer.RecognizeResult result) {
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

    public static UnderstandResult toUnderstandResultPojo(
            SpeechProto.UnderstandResult result) {
        UnderstandResult.Builder builder = new UnderstandResult.Builder();

        builder.setSessionId(result.getSessionId());
        builder.setLanguage(result.getLanguage());
        builder.setActionIncomplete(result.getActionIncomplete());
        builder.setSource(result.getSource());
        builder.setInputText(result.getInputText());

        //生成intent
        UnderstandResult.Intent.Builder intent = new UnderstandResult.Intent.Builder();
        intent.setName(result.getIntent().getName());
        intent.setDisplayName(result.getIntent().getDisplayName());
        intent.setScore(result.getIntent().getScore());
        builder.setIntent(intent.build());

        //生成context
        List<SpeechProto.Context> contextProtos = result.getContextsList();
        List<UnderstandResult.Context> contextBeans = new ArrayList<>();
        for (SpeechProto.Context context : contextProtos) {
            UnderstandResult.Context.Builder contextBuilder =
                    new UnderstandResult.Context.Builder();
            contextBuilder.setName(context.getName());
            try {
                contextBuilder.setParameters(new JSONObject(context.getParametersJson()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            contextBuilder.setLifespan(context.getLifespan());
            contextBeans.add(contextBuilder.build());
        }

        builder.setContexts(contextBeans);

        //生成fulfillment
        SpeechProto.Fulfillment fulfillment = result.getFulfillment();
        UnderstandResult.Fulfillment.Builder fulfillmentBuilder =
                new UnderstandResult.Fulfillment.Builder();
        fulfillmentBuilder.setSpeech(fulfillment.getSpeech());

        List<SpeechProto.Message> messageProtos = result.getFulfillment().getMessagesList();

        List<UnderstandResult.Message> messageBeans = new ArrayList<>();
        for (SpeechProto.Message message : messageProtos) {
            UnderstandResult.Message.Builder messageBuilder =
                    new UnderstandResult.Message.Builder();
            messageBuilder.setType(message.getType());
            try {
                messageBuilder.setParameters(new JSONObject(message.getParametersJson()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            messageBuilder.setPlatform(message.getPlatform());
            messageBeans.add(messageBuilder.build());
        }
        fulfillmentBuilder.setMessages(messageBeans);

        UnderstandResult.Status.Builder statusBuilder = new UnderstandResult.Status.Builder();
        statusBuilder.setCode(fulfillment.getStatus().getCode());
        statusBuilder.setErrorDetails(fulfillment.getStatus().getErrorDetails());
        statusBuilder.setErrorMessage(fulfillment.getStatus().getErrorMessage());
        fulfillmentBuilder.setStatus(statusBuilder.build());
        builder.setFulfillment(fulfillmentBuilder.build());
        
        return builder.build();
    }

    public static SpeechProto.UnderstandResult toUnderstandResultProto(
            UnderstandResult result) {
        SpeechProto.UnderstandResult.Builder builder = SpeechProto.UnderstandResult.newBuilder();

        builder.setActionIncomplete(result.isActionIncomplete());
        builder.setSessionId(result.getSessionId());
        builder.setLanguage(result.getLanguage());
        builder.setSource(result.getSource());
        builder.setInputText(result.getInputText());

        //生成intent
        SpeechProto.Intent.Builder intentBuilder = SpeechProto.Intent.newBuilder();
        intentBuilder.setName(result.getIntent().getName());
        intentBuilder.setDisplayName(result.getIntent().getDisplayName());
        intentBuilder.setScore(result.getIntent().getScore());
        builder.setIntent(intentBuilder.build());

        //生成context
        List<UnderstandResult.Context> contextBeans = result.getContexts();
        List<SpeechProto.Context> contextProtos = new ArrayList<>();
        for (UnderstandResult.Context context : contextBeans) {
            SpeechProto.Context.Builder contextBuilder = SpeechProto.Context.newBuilder();
            contextBuilder.setName(context.getName());
            contextBuilder.setParametersJson(context.getSlots().toString());
            contextBuilder.setLifespan(context.getLifespan());
            contextProtos.add(contextBuilder.build());
        }

        builder.addAllContexts(contextProtos);

        //生成fulfillment
        UnderstandResult.Fulfillment fulfillment =
                result.getFulfillment();
        SpeechProto.Fulfillment.Builder fulfillmentBuilder =
                SpeechProto.Fulfillment.newBuilder();

        fulfillmentBuilder.setSpeech(fulfillment.getSpeech());

        List<UnderstandResult.Message> messageBeans = result.getFulfillment().getMessages();

        List<SpeechProto.Message> messageProtos = new ArrayList<>();
        for (UnderstandResult.Message message : messageBeans) {
            SpeechProto.Message.Builder messageBuilder = SpeechProto.Message.newBuilder();
            messageBuilder.setType(message.getType());
            messageBuilder.setParametersJson(message.getParameters().toString());
            messageBuilder.setPlatform(message.getPlatform());
            messageProtos.add(messageBuilder.build());
        }
        fulfillmentBuilder.addAllMessages(messageProtos);

        SpeechProto.Status.Builder statusBuilder = SpeechProto.Status.newBuilder();
        statusBuilder.setCode(fulfillment.getStatus().getCode());
        statusBuilder.setErrorDetails(fulfillment.getStatus().getErrorDetails());
        statusBuilder.setErrorMessage(fulfillment.getStatus().getErrorMessage());
        fulfillmentBuilder.setStatus(statusBuilder.build());
        builder.setFulfillment(fulfillmentBuilder.build());

        return builder.build();
    }

    public static SpeechProto.UnderstandOption toUnderstandOptionProto(UnderstandOption option,
            String question) {
        return SpeechProto.UnderstandOption.newBuilder()
                .setQuestion(question)
                .setTimeOut(option.getTimeout())
                .build();
    }

    public static UnderstandOption toUnderstandOptionPojo(SpeechProto.UnderstandOption option) {
        return new UnderstandOption.Builder()
                .setTimeout(option.getTimeOut())
                .build();
    }

    public static Speaker toSpeakerPojo(SpeechProto.Speaker speaker) {
        return new Speaker.Builder(speaker.getId())
                .setName(speaker.getName())
                .setGender(speaker.getGender())
                .build();
    }

    public static SpeechProto.Speaker toSpeakerProto(Speaker speaker) {
        return SpeechProto.Speaker.newBuilder()
                .setName(speaker.getName())
                .setId(speaker.getId())
                .setGender(speaker.getGender())
                .build();
    }

    public static List<Speaker> toSpeakersPojo(SpeechProto.Speakers speakers) {
        LinkedList<Speaker> list = new LinkedList<>();
        for (SpeechProto.Speaker speaker : speakers.getSpeakersList()) {
            list.add(toSpeakerPojo(speaker));
        }
        return list;
    }

    public static SpeechProto.Speakers toSpeakersProto(List<Speaker> speakers) {
        if (null == speakers) {
            throw new IllegalArgumentException("SpeechConverters refuse null param");
        }

        SpeechProto.Speakers.Builder builder = SpeechProto.Speakers.newBuilder();
        for (Speaker speaker : speakers) {
            builder.addSpeakers(toSpeakerProto(speaker));
        }

        return builder.build();
    }

    public static SpeechProto.Configuration toConfigurationProto(Configuration configuration) {
        return SpeechProto.Configuration.newBuilder()
                .setSpeakerId(configuration.getSpeakerId())
                .setSpeakingSpeed(configuration.getSpeakingSpeed())
                .setSpeakingVolume(configuration.getSpeakingVolume())
                .setRecognizeMode(configuration.getRecognizingMode())
                .setUnderstandTimeout(configuration.getUnderstandTimeout())
                .build();
    }

    public static Configuration toConfigurationPojo(SpeechProto.Configuration configuration) {
        return new Configuration.Builder()
                .setSpeakerId(configuration.getSpeakerId())
                .setSpeakingSpeed(configuration.getSpeakingSpeed())
                .setSpeakingVolume(configuration.getSpeakingVolume())
                .setRecognizingMode(configuration.getRecognizeMode())
                .setUnderstandTimeout(configuration.getUnderstandTimeout())
                .build();

    }
}
