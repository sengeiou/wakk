package com.ubtrobot.dance;

import com.ubtrobot.play.Track;

import java.util.List;

public class Dance {

    private String name;
    private String mCategory;
    private String mainType;
    private List<Track> tracks;

    public Dance(String name, String category, String mainType, List<Track> tracks) {
        if (name == null || name.length() == 0 ||
                mainType == null || mainType.length() == 0 ||
                tracks == null || tracks.isEmpty()) {
            throw new IllegalArgumentException(
                    "Argument name or mainType or tracks is null or empty.");
        }

        this.name = name;
        this.mCategory = category;
        this.mainType = mainType;
        this.tracks = tracks;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return mCategory;
    }

    public String getMainType() {
        return mainType;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    @Override
    public String toString() {
        StringBuffer trackBuffer = new StringBuffer();
        trackBuffer.append("[");
        for (Track track : tracks) {
            trackBuffer.append(track.toString());
            trackBuffer.append(",");
        }
        trackBuffer.append("]");

        return "Dance{" +
                "name='" + name + '\'' +
                ", mCategory='" + mCategory + '\'' +
                ", mainType='" + mainType + '\'' +
                ", tracks=" + trackBuffer.toString() +
                '}';
    }

}
