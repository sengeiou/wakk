package com.ubtrobot.play;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SegmentGroup<O> extends Segment<O> {

    private List<Segment<O>> children;

    private SegmentGroup(Builder<O> builder) {
        super(builder);
        children = Collections.unmodifiableList(builder.children);
    }

    public List<Segment<O>> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return "SegmentGroup{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", loops=" + getLoops() +
                ", duration=" + getDuration() +
                ", blank=" + isBlank() +
                ", option=" + getOption() +
                ", children=" + getChildren() +
                '}';
    }

    public static class Builder<O> extends GenericBuilder<O, Builder<O>> {

        private List<Segment<O>> children = new LinkedList<>();

        public Builder<O> addChildren(Collection<Segment<O>> children) {
            if (children == null || children.isEmpty()) {
                throw new IllegalArgumentException("Argument children is null or empty.");
            }

            this.children.addAll(children);
            return this;
        }

        public Builder<O> addChild(Segment<O> childSegment) {
            if (childSegment == null) {
                throw new IllegalArgumentException("Argument childSegment is null.");
            }

            this.children.add(childSegment);
            return this;
        }

        public Builder<O> addChild(SegmentGroup<O> childSegmentGroup) {
            if (childSegmentGroup == null) {
                throw new IllegalArgumentException("Argument childSegmentGroup is null.");
            }

            this.children.add(childSegmentGroup);
            return this;
        }

        @Override
        public SegmentGroup<O> build() {
            return new SegmentGroup<>(this);
        }
    }
}