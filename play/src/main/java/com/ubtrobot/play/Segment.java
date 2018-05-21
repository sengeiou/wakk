package com.ubtrobot.play;

public class Segment<O> {

    private String name;
    private String description;
    private int loops;
    private long duration;
    private boolean blank;
    private O option;

    protected Segment(GenericBuilder<O, ?> builder) {
        name = builder.name == null ? "" : builder.name;
        description = builder.description == null ? "" : builder.description;
        loops = builder.loops;
        duration = builder.duration;
        blank = builder.blank;
        option = builder.option;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getLoops() {
        return loops;
    }

    public long getDuration() {
        return duration;
    }

    public boolean isBlank() {
        return blank;
    }

    public O getOption() {
        return option;
    }

    @Override
    public String toString() {
        return "Segment{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", loops=" + loops +
                ", duration=" + duration +
                ", blank=" + blank +
                ", option=" + option +
                '}';
    }

    public static class Builder<O> extends GenericBuilder<O, Builder<O>> {

    }

    protected static class GenericBuilder<O, T extends GenericBuilder<O, T>> {

        private String name;
        private String description;
        private int loops;
        private long duration;
        private boolean blank;
        private O option;

        @SuppressWarnings("unchecked")
        public T setName(String name) {
            this.name = name;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T setDescription(String description) {
            this.description = description;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T setLoops(int loops) {
            if (loops < 0) {
                throw new IllegalArgumentException("Argument loops < 0");
            }

            this.loops = loops;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T setDuration(long duration) {
            if (duration < 0) {
                throw new IllegalArgumentException("Argument duration < 0");
            }

            this.duration = duration;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T setBlank(boolean blank) {
            this.blank = blank;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T setOption(O option) {
            this.option = option;
            return (T) this;
        }

        public Segment<O> build() {
            if (blank && duration <= 0) {
                throw new IllegalStateException("Duration should > 0 when blank is true.");
            }

            return new Segment<>(this);
        }
    }
}