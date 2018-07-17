package com.ubtrobot.diagnosis;

public class RepairProgress {
    public static final int STATE_BEGAN = 0;
    public static final int STATE_ENDED = 1;

    private int state;

    public RepairProgress(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public boolean isBegan() {
        return state == STATE_BEGAN;
    }

    public boolean isEnded() {
        return state == STATE_ENDED;
    }

    public static class Builder {

        private int state;

        public Builder(int state) {
            this.state = state;

            if (state < STATE_BEGAN || state > STATE_ENDED) {
                throw new IllegalArgumentException("Argument state < " + STATE_BEGAN + " || " +
                        "state > " + STATE_ENDED + ".");
            }
        }

        public RepairProgress build() {
            RepairProgress progress = new RepairProgress(state);
            return progress;
        }
    }
}
