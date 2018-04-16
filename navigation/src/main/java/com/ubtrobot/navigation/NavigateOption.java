package com.ubtrobot.navigation;

public class NavigateOption {

    public static final NavigateOption DEFAULT = new NavigateOption.Builder().build();

    private float maxSpeed;
    private int retryCount;

    private NavigateOption() {
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public static final class Builder {

        private float maxSpeed;
        private int retryCount;

        public Builder() {
        }

        public Builder setMaxSpeed(float maxSpeed) {
            if (maxSpeed < 0) {
                throw new IllegalArgumentException("Argument maxSpeed < 0.");
            }

            this.maxSpeed = maxSpeed;
            return this;
        }

        public Builder setRetryCount(int retryCount) {
            if (retryCount < 0) {
                throw new IllegalArgumentException("Argument retryCount < 0.");
            }

            this.retryCount = retryCount;
            return this;
        }

        public NavigateOption build() {
            NavigateOption option = new NavigateOption();
            option.maxSpeed = maxSpeed;
            option.retryCount = retryCount;
            return option;
        }
    }
}
