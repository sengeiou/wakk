package com.ubtrobot.speech;

import com.ubtrobot.validate.Preconditions;

public class SynthesizeOption {

    public static final SynthesizeOption DEFAULT = new SynthesizeOption.Builder().build();

    public static final int SPEAKING_SPEED_MAX = 100;
    public static final int SPEAKING_SPEED_MIN = 0;

    public static final int SPEAKING_VOLUME_MAX = 100;
    public static final int SPEAKING_VOLUME_MIN = 0;


    public static final int DEFAULT_SPEAKING_SPEED = -1;
    public static final int DEFAULT_SPEAKING_VOLUME = -1;

    private String speakerId;
    private int speakingSpeed;
    private int speakingVolume;

    private SynthesizeOption() {
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

    @Override
    public String toString() {
        return "SynthesizeOption{" +
                "speakerId='" + speakerId + '\'' +
                ", speakingSpeed=" + speakingSpeed +
                ", speakingVolume=" + speakingVolume +
                '}';
    }

    public static class Builder {

        private String speakerId = "";
        private int speakingSpeed = DEFAULT_SPEAKING_SPEED;
        private int speakingVolume = DEFAULT_SPEAKING_VOLUME;

        public Builder() {
        }

        public Builder(SynthesizeOption option) {
            if (option == null) {
                throw new IllegalArgumentException("Option should not be null.");
            }

            speakerId = option.getSpeakerId();
            speakingSpeed = option.getSpeakingSpeed();
            speakingVolume = option.getSpeakingVolume();
        }

        public Builder setSpeakingSpeed(int speakingSpeed) {
            checkSpeakSpeed(speakingSpeed);
            this.speakingSpeed = speakingSpeed;
            return this;
        }

        private void checkSpeakSpeed(int speakSpeed) {
            if (speakSpeed < DEFAULT_SPEAKING_SPEED || speakSpeed > SPEAKING_SPEED_MAX) {
                throw new IllegalArgumentException("Invalid speakingSpeed value, verify for [0,100].");
            }
        }

        public Builder setSpeakingVolume(int speakingVolume) {
            checkSpeakVolume(speakingVolume);
            this.speakingVolume = speakingVolume;
            return this;
        }

        private void checkSpeakVolume(int speakVolume) {
            if (speakVolume < DEFAULT_SPEAKING_VOLUME || speakingSpeed > SPEAKING_VOLUME_MAX) {
                throw new IllegalArgumentException("Invalid speakingVolume value, verify for [0,100].");
            }
        }

        public Builder setSpeakerId(String speakerId) {
            this.speakerId = Preconditions.checkNotNull(speakerId, "Invalid speakerId value, verify for not be null.");
            return this;
        }

        public SynthesizeOption build() {
            SynthesizeOption option = new SynthesizeOption();
            option.speakerId = speakerId;
            option.speakingSpeed = speakingSpeed;
            option.speakingVolume = speakingVolume;
            return option;
        }
    }
}
