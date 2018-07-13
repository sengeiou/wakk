package com.ubtrobot.speech;

import android.text.TextUtils;

import com.ubtrobot.validate.Preconditions;

public class Configuration {

    private String speakerId;
    private int speakingSpeed;
    private int speakingVolume;

    private int recognizingMode;

    private float understandTimeout;

    private Configuration() {
    }

    public String getSpeakerId() {
        return speakerId;
    }

    public int getSpeakingSpeed() {
        return speakingSpeed;
    }

    public int getSpeakingVolume() {
        return speakingVolume;
    }

    public int getRecognizingMode() {
        return recognizingMode;
    }

    public float getUnderstandTimeout() {
        return understandTimeout;
    }

    public static class Builder {

        private String speakerId = SynthesizeOption.DEFAULT.getSpeakerId();
        private int speakingSpeed = SynthesizeOption.DEFAULT.getSpeakingSpeed();
        private int speakingVolume = SynthesizeOption.DEFAULT.getSpeakingVolume();

        private int recognizingMode = RecognizeOption.DEFAULT.getMode();

        private float understandTimeout = UnderstandOption.DEFAULT.getTimeout();

        public Builder() {
        }

        public Builder(Configuration configuration) {
            Preconditions.checkNotNull(configuration,
                    "Configuration.Builder refuse null Configuration");
            speakerId = configuration.getSpeakerId();
            speakingSpeed = configuration.getSpeakingSpeed();
            speakingVolume = configuration.getSpeakingVolume();
            recognizingMode = configuration.getRecognizingMode();
            understandTimeout = configuration.getUnderstandTimeout();
        }

        public Builder setSpeakerId(String speakerId) {
            Preconditions.checkArgument(!TextUtils.isEmpty(speakerId),
                    "Configuration.Builder refuse null speakerId");
            this.speakerId = speakerId;
            return this;
        }

        public Builder setSpeakingSpeed(int speakingSpeed) {
            Preconditions.checkArgument((speakingSpeed >= SynthesizeOption.DEFAULT_SPEAKING_SPEED)
                            && (speakingSpeed <= SynthesizeOption.SPEAKING_SPEED_MAX),
                    "Configuration.Builder setSpeakingSpeed value must in" +
                            "[SynthesizeOption.SPEAKING_SPEED_MIN,SynthesizeOption"
                            + ".SPEAKING_SPEED_MAX]");
            this.speakingSpeed = speakingSpeed;
            return this;
        }

        public Builder setSpeakingVolume(int speakingVolume) {
            Preconditions.checkArgument((speakingVolume >= SynthesizeOption.DEFAULT_SPEAKING_VOLUME)
                            && (speakingVolume <= SynthesizeOption.SPEAKING_VOLUME_MAX),
                    "Configuration.Builder speakingVolume value must in" +
                            "[SynthesizeOption.SPEAKING_VOLUME_MIN,SynthesizeOption"
                            + ".SPEAKING_VOLUME_MAX]");
            this.speakingVolume = speakingVolume;
            return this;
        }

        public Builder setRecognizingMode(int recognizingMode) {
            Preconditions.checkArgument(recognizingMode == RecognizeOption.MODE_UNKNOWN
                            || recognizingMode == RecognizeOption.MODE_SINGLE
                            || recognizingMode == RecognizeOption.MODE_CONTINUOUS,
                    "Invalid mode value, verify for " +
                            "RecognizeOption.MODE__MODE_SINGLE or RecognizeOption.MODE_CONTINUOUS");
            this.recognizingMode = recognizingMode;
            return this;
        }

        public Builder setUnderstandTimeout(float understandTimeout) {
            Preconditions.checkArgument(understandTimeout >= 0, "Timeout value must not be < 0");
            this.understandTimeout = understandTimeout;
            return this;
        }

        public Configuration build() {
            Configuration configuration = new Configuration();
            configuration.speakerId = speakerId;
            configuration.speakingSpeed = speakingSpeed;
            configuration.speakingVolume = speakingVolume;
            configuration.recognizingMode = recognizingMode;
            configuration.understandTimeout = understandTimeout;
            return configuration;
        }
    }
}
