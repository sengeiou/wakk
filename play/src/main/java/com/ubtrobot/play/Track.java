package com.ubtrobot.play;

public class Track<O> {

    private String type;
    private SegmentGroup<O> segmentGroup;
    private String description;

    protected Track(GenericBuilder<O, ?> builder) {
        type = builder.type;
        segmentGroup = builder.segmentGroup;
        description = builder.description;
        description = description == null ? "" : description;
    }

    public String getType() {
        return type;
    }

    public SegmentGroup<O> getSegmentGroup() {
        return segmentGroup;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Track{" +
                "type='" + type + '\'' +
                ", segmentGroup=" + segmentGroup.toString() +
                ", description='" + description + '\'' +
                '}';
    }

    public static class Builder<O> extends GenericBuilder<O, Builder<O>> {

        public Builder(String type, SegmentGroup<O> segmentGroup) {
            super(type, segmentGroup);
        }
    }

    protected static class GenericBuilder<O, T extends GenericBuilder<O, T>> {

        private String type;
        private SegmentGroup<O> segmentGroup;
        private String description;

        public GenericBuilder(String type, SegmentGroup<O> segmentGroup) {
            if (type == null || type.length() == 0) {
                throw new IllegalArgumentException("Argument type is null or empty.");
            }
            if (segmentGroup == null) {
                throw new IllegalArgumentException("Argument segmentGroup is null.");
            }

            this.type = type;
            this.segmentGroup = segmentGroup;
        }

        public T setDescription(String description) {
            this.description = description;
            return (T) this;
        }

        public Track<O> build() {
            return new Track<>(this);
        }
    }
}
