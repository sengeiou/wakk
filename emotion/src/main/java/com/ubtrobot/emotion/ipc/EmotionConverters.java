package com.ubtrobot.emotion.ipc;

import com.ubtrobot.emotion.Emotion;
import com.ubtrobot.emotion.EmotionResource;
import com.ubtrobot.emotion.ExpressOption;

import java.util.LinkedList;
import java.util.List;

public class EmotionConverters {

    private EmotionConverters() {
    }

    public static List<Emotion> toEmotionListPojo(EmotionProto.EmotionList emotionListProto) {
        LinkedList<Emotion> emotionList = new LinkedList<>();
        for (EmotionProto.Emotion emotion : emotionListProto.getEmotionList()) {
            emotionList.add(toEmotionPojo(emotion));
        }

        return emotionList;
    }

    public static EmotionProto.EmotionList toEmotionListProto(List<Emotion> emotions) {
        EmotionProto.EmotionList.Builder builder = EmotionProto.EmotionList.newBuilder();
        for (Emotion emotion : emotions) {
            builder.addEmotion(toEmotionProto(emotion));
        }

        return builder.build();
    }

    public static EmotionProto.Emotion toEmotionProto(Emotion emotion) {
        return EmotionProto.Emotion.newBuilder().setId(emotion.getId()).
                setResource(EmotionProto.EmotionResource.newBuilder().
                        setPackageName(emotion.getResource().getPackageName()).
                        setNameResource(emotion.getResource().getNameResource()).
                        setIconUri(emotion.getResource().getIconUri()).
                        setName(emotion.getResource().getName()).
                        build()).
                build();
    }

    public static Emotion toEmotionPojo(EmotionProto.Emotion emotionProto) {
        return new Emotion(
                emotionProto.getId(),
                new EmotionResource.Builder(
                        emotionProto.getResource().getPackageName(),
                        emotionProto.getResource().getNameResource(),
                        emotionProto.getResource().getName(),
                        emotionProto.getResource().getIconUri()
                ).build()
        );
    }

    public static EmotionProto.ExpressOption
    toExpressOptionProto(String emotionId, ExpressOption option) {
        return EmotionProto.ExpressOption.newBuilder().
                setEmotionId(emotionId).
                setLoops(option.getLoops()).
                setDismissAfterEnd(option.isDismissAfterEnd()).
                setLoopDefaultAfterEnd(option.isLoopDefaultAfterEnd()).
                build();
    }

    public static ExpressOption
    toExpressOptionPojo(EmotionProto.ExpressOption option) {
        return new ExpressOption.Builder().setLoops(option.getLoops()).
                setDismissAfterEnd(option.getDismissAfterEnd()).
                setLoopDefaultAfterEnd(option.getLoopDefaultAfterEnd()).
                build();
    }
}
