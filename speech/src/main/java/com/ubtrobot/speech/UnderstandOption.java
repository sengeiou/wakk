package com.ubtrobot.speech;

public class UnderstandOption {

    public static final UnderstandOption DEFAULT = new UnderstandOption.Builder().build();

    private float timeout;

    private UnderstandOption() {
    }

    public float getTimeout() {
        return timeout;
    }

    public static class Builder {

        private float timeout = 15000;

        public Builder() {
        }

        public Builder setTimeout(float timeout) {
            checkTimeout(timeout);
            this.timeout = timeout;
            return this;
        }

        private void checkTimeout(float timeout) {
            if (timeout < 0) {
                throw new IllegalArgumentException("Timeout value must not be < 0");
            }
        }

        public UnderstandOption build() {
            UnderstandOption understandOption = new UnderstandOption();
            understandOption.timeout = timeout;
            return understandOption;
        }
    }
}
