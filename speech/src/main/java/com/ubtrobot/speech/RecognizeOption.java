package com.ubtrobot.speech;

public class RecognizeOption {

    public static final RecognizeOption DEFAULT = new RecognizeOption.Builder().build();

    public static final int MODE_UNKNOWN = 0;
    public static final int MODE_SINGLE = 1;
    public static final int MODE_CONTINUOUS = 2;

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
            if (mode != MODE_UNKNOWN && mode != MODE_SINGLE && mode != MODE_CONTINUOUS) {
                throw new IllegalArgumentException("Invalid mode value, verify for " +
                        "MODE_UNKNOWNRecognizeOption.MODE_UNKNOWN,RecognizeOption.MODE__MODE_SINGLE or RecognizeOption.MODE_CONTINUOUS");
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
