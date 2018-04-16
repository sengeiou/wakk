package com.ubtrobot.emotion;

public class ExpressOption {

    public static final ExpressOption DEFAULT = new ExpressOption.Builder().build();

    private int loops;
    private boolean dismissAfterEnd;
    private boolean loopDefaultAfterEnd;

    private ExpressOption() {
    }

    public int getLoops() {
        return loops;
    }

    public boolean isDismissAfterEnd() {
        return dismissAfterEnd;
    }

    public boolean isLoopDefaultAfterEnd() {
        return loopDefaultAfterEnd;
    }

    @Override
    public String toString() {
        return "ExpressOption{" +
                "loops=" + loops +
                ", dismissAfterEnd=" + dismissAfterEnd +
                ", loopDefaultAfterEnd=" + loopDefaultAfterEnd +
                '}';
    }

    public static class Builder {

        private int loops;
        private boolean dismissAfterEnd;
        private boolean loopDefaultAfterEnd;

        public Builder() {
        }

        public Builder setLoops(int loops) {
            if (loops < 0) {
                throw new IllegalArgumentException("Argument loops is null.");
            }

            this.loops = loops;
            return this;
        }

        public Builder setDismissAfterEnd(boolean dismissAfterEnd) {
            this.dismissAfterEnd = dismissAfterEnd;
            return this;
        }

        public Builder setLoopDefaultAfterEnd(boolean loopDefaultAfterEnd) {
            this.loopDefaultAfterEnd = loopDefaultAfterEnd;
            return this;
        }

        public ExpressOption build() {
            ExpressOption option = new ExpressOption();
            option.loops = loops;
            option.dismissAfterEnd = dismissAfterEnd;
            option.loopDefaultAfterEnd = loopDefaultAfterEnd;
            return option;
        }
    }
}