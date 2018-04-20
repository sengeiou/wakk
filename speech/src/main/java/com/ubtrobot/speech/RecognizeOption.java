package com.ubtrobot.speech;

public class RecognizeOption {

    public static final RecognizeOption DEFAULT = new RecognizeOption.Builder().build();

    public static final int MODE_UNKNOWN = -1;
    public static final int MODE_SINGLE = 0;
    public static final int MODE_CONTINUOUS = 1;

    private int mode;

    private RecognizeOption(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    public static class Builder {

        private int mode = MODE_UNKNOWN;

        public Builder() {
        }

        public Builder(int mode) {
            checkMode(mode);
            this.mode = mode;
        }

        private void checkMode(int mode) {
            if (mode != MODE_SINGLE && mode != MODE_CONTINUOUS) {
                throw new IllegalArgumentException("Invalid mode value, verify for " +
                        "RecognizeOption.MODE__MODE_SINGLE or RecognizeOption.MODE_CONTINUOUS");
            }
        }

        public Builder(RecognizeOption option) {
            if (null == option) {
                throw new IllegalArgumentException("RecognizeOption.Builder refuse null option.");
            }

            this.mode = option.mode;
        }

        public RecognizeOption build() {
            return new RecognizeOption(mode);
        }
    }
}
