package com.ubtrobot.play.ipc;

import com.ubtrobot.play.Segment;
import com.ubtrobot.play.SegmentGroup;
import com.ubtrobot.play.Track;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.util.List;

public class PlayConverters {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("PlayConverters");

    private PlayConverters() {
    }

    public static <O> PlayProto.Segment.Builder toSegmentBuilderProto(Segment<O> segment) {
        return PlayProto.Segment.newBuilder().
                setName(segment.getName()).
                setBlank(segment.isBlank()).
                setDescription(segment.getDescription()).
                setDuration(segment.getDuration()).
                setLoops(segment.getLoops());
    }

    public static <O> Segment.Builder<O> toSegmentBuilderPojo(PlayProto.Segment segmentProto) {
        return new Segment.Builder<O>().
                setName(segmentProto.getName()).
                setBlank(segmentProto.getBlank()).
                setDescription(segmentProto.getDescription()).
                setDuration(segmentProto.getDuration()).
                setLoops(segmentProto.getLoops());
    }

    public static PlayProto.SegmentGroup toSegmentGroupProto(
            PlayProto.Segment segmentProto, List<PlayProto.Segment> childrenProto) {
        return PlayProto.SegmentGroup.newBuilder().
                addAllChildren(childrenProto).
                setSegment(segmentProto).
                build();
    }

    public static <O> SegmentGroup<O> toSegmentGroupPojo(
            Segment<O> segment, List<Segment<O>> children) {
        return new SegmentGroup.Builder<O>().
                addChildren(children).
                setName(segment.getName()).
                setBlank(segment.isBlank()).
                setDescription(segment.getDescription()).
                setDuration(segment.getDuration()).
                setLoops(segment.getLoops()).
                setOption(segment.getOption()).
                build();
    }

    public static <O> PlayProto.Track toTrackProto(
            Track<O> track,PlayProto.Segment segmentProto, List<PlayProto.Segment> childrenProto) {
        return PlayProto.Track.newBuilder().
                setType(track.getType()).
                setDescription(track.getDescription()).
                setSegmentGroup(toSegmentGroupProto(segmentProto, childrenProto)).
                build();
    }

    public static <O> Track<O> toTrackPojo(
            PlayProto.Track trackProto, Segment<O> segment, List<Segment<O>> children) {
        return new Track.Builder<>(trackProto.getType(),toSegmentGroupPojo(segment, children)).
                build();
    }
}
