package com.ubtrobot.light;

public class DisplayOption {

    private int loops;

    private DisplayOption() {
    }

    protected DisplayOption(GenericsBuild<?> build) {
        loops = build.loops;
    }

    public int getLoops() {
        return loops;
    }

    @Override
    public String toString() {
        return "DisplayOption{" +
                "loops=" + loops +
                '}';
    }

    public static class Builder extends GenericsBuild<Builder> {
    }

    protected static class GenericsBuild<T extends GenericsBuild> {

        private int loops;

        public GenericsBuild() {
        }

        @SuppressWarnings("unchecked")
        public T setLoops(int loops) {
            if (loops < 0) {
                throw new IllegalArgumentException("Argument loops < 0.");
            }

            this.loops = loops;
            return (T) this;
        }

        public DisplayOption build() {
            DisplayOption option = new DisplayOption();
            option.loops = loops;
            return option;
        }
    }
}