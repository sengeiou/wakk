package com.ubtrobot.speech;

import android.text.TextUtils;

public class SynthesizeOption {

    public static final SynthesizeOption DEFAULT = new SynthesizeOption.Builder().build();

    private static final int SPEAKING_SPEED_MAX = 100;
    private static final int SPEAKING_SPEED_MIN = 0;

    private static final int SPEAKING_VOLUME_MAX = 100;
    private static final int SPEAKING_VOLUME_MIN = 0;

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
        private int speakingSpeed = -1;
        private int speakingVolume = -1;

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
            if (speakSpeed < SPEAKING_SPEED_MIN || speakSpeed > SPEAKING_SPEED_MAX) {
                throw new IllegalArgumentException("Invalid speakingSpeed value, verify for [0,100].");
            }
        }

        public Builder setSpeakingVolume(int speakingVolume) {
            checkSpeakVolume(speakingVolume);
            this.speakingVolume = speakingVolume;
            return this;
        }

        private void checkSpeakVolume(int speakVolume) {
            if (speakVolume < SPEAKING_VOLUME_MIN || speakingSpeed > SPEAKING_VOLUME_MAX) {
                throw new IllegalArgumentException("Invalid speakingVolume value, verify for [0,100].");
            }
        }

        public Builder setSpeakerId(String speakerId) {
            checkSpeakerId(speakerId);
            this.speakerId = speakerId;
            return this;
        }

        private void checkSpeakerId(String speakId) {
            if (TextUtils.isEmpty(speakId)) {
                throw new IllegalArgumentException("Invalid speakerId value, verify for not be null.");
            }
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
