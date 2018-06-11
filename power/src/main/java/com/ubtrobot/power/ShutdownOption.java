package com.ubtrobot.power;

public class ShutdownOption {

    public static final ShutdownOption DEFAULT = new Builder().build();

    private long waitSecondsToStartup;

    private ShutdownOption() {
    }

    public long getWaitSecondsToStartup() {
        return waitSecondsToStartup;
    }

    @Override
    public String toString() {
        return "ShutdownOption{" +
                "waitSecondsToStartup=" + waitSecondsToStartup +
                '}';
    }

    public static final class Builder {

        private long waitSecondsToStartup;

        public Builder setWaitSecondsToStartup(long waitSecondsToStartup) {
            if (waitSecondsToStartup < 0) {
                throw new IllegalArgumentException("Argument waitSecondsToStartup < 0.");
            }

            this.waitSecondsToStartup = waitSecondsToStartup;
            return this;
        }

        public ShutdownOption build() {
            ShutdownOption option = new ShutdownOption();
            option.waitSecondsToStartup = waitSecondsToStartup;
            return option;
        }
    }
}