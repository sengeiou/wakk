package com.ubtrobot.dance.parser;

import android.content.Context;
import android.content.res.AssetManager;

import com.ubtrobot.dance.Dance;
import com.ubtrobot.dance.DanceFileHelper;
import com.ubtrobot.play.Segment;
import com.ubtrobot.play.SegmentGroup;
import com.ubtrobot.play.Track;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class DanceParser {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("DanceParser");

    private Context mContext;
    private String mDanceFileName;

    private List<Dance> mDanceList = new ArrayList<>();

    public DanceParser(Context context, String danceFileName) {
        if (danceFileName == null || danceFileName.length() == 0) {
            throw new IllegalArgumentException("Argument danceFileName is null.");
        }

        mContext = context;
        mDanceFileName = danceFileName;
    }

    public List<Dance> parser() {
        if (mDanceList.size() == 0) {
            mDanceList.addAll(analysisDanceJson());
        }

        return mDanceList;
    }

    private LinkedList<Dance> analysisDanceJson() {
        LinkedList<Dance> dances = new LinkedList<>();

        JSONArray jsonArray = getDanceJson();
        for (int i = 0, len = jsonArray.length(); i < len; i++) {
            JSONObject jsonObject;
            try {
                jsonObject = jsonArray.getJSONObject(i);
            } catch (JSONException e) {
                throw new IllegalStateException("Please check json file.");
            }

            try {
                String mainType = jsonObject.getString(DanceJsonKey.MAIN_TYPE);
                String name = jsonObject.getString(DanceJsonKey.DANCE_NAME);
                String category = jsonObject.optString(DanceJsonKey.CATEGORY);

                JSONArray tracksJson = jsonObject.getJSONArray(DanceJsonKey.TRACKS);
                LinkedList<Track> tracks = new LinkedList<>();
                addTracks(tracksJson, tracks);

                Dance dance = new Dance(name, category, mainType, tracks);
                dances.add(dance);
            } catch (JSONException e) {
                throw new IllegalStateException("Please check json object.");
            }
        }

        return dances;
    }

    private void addTracks(JSONArray trackJson, List<Track> tracks) {
        for (int i = 0, len = trackJson.length(); i < len; i++) {
            JSONObject jsonObject;
            try {
                jsonObject = trackJson.getJSONObject(i);
            } catch (JSONException e) {
                throw new IllegalStateException("Please check json file.");
            }

            try {
                String type = jsonObject.getString(DanceJsonKey.TYPE);
                String trackDescription = jsonObject.getString(DanceJsonKey.TRACK_DESCRIPTION);
                JSONObject segmentGroupJson = jsonObject.getJSONObject(DanceJsonKey.SEGMENT_GROUP);

                Track track = ((Track.Builder) new Track.Builder(
                        type, getSegmentGroup(segmentGroupJson, type)).
                        setDescription(trackDescription)).build();
                tracks.add(track);
            } catch (JSONException e) {
                throw new IllegalStateException("Please check json object.");
            }
        }
    }

    private SegmentGroup getSegmentGroup(JSONObject segmentGroupJson, String type) {
        SegmentGroup.Builder segmentGroupBuilder = new SegmentGroup.Builder();

        String name = segmentGroupJson.optString(DanceJsonKey.NAME);
        String description = segmentGroupJson.optString(DanceJsonKey.DESCRIPTION);
        int loops = segmentGroupJson.optInt(DanceJsonKey.LOOPS, 1);
        long duration = segmentGroupJson.optLong(DanceJsonKey.DURATION);
        boolean isBlank = segmentGroupJson.optBoolean(DanceJsonKey.IS_BLANK, false);
        JSONObject optionJson = segmentGroupJson.optJSONObject(DanceJsonKey.OPTION);
        JSONArray childrenJson = segmentGroupJson.optJSONArray(DanceJsonKey.CHILDREN);

        segmentGroupBuilder.setName(name);
        segmentGroupBuilder.setDescription(description);
        segmentGroupBuilder.setLoops(loops);
        segmentGroupBuilder.setDuration(duration);
        segmentGroupBuilder.setBlank(isBlank);
        segmentGroupBuilder.setOption(parserOption(type, optionJson));

        LinkedList<Segment> children = new LinkedList<>();
        addChildren(type, childrenJson, children);
        if (children.size() > 0) {
            segmentGroupBuilder.addChildren(children);
        }

        return segmentGroupBuilder.build();
    }

    protected abstract <O> O parserOption(String type, JSONObject optionJson);

    private void addChildren(String type, JSONArray childrenJson, LinkedList<Segment> children) {
        if (childrenJson == null) {
            return;
        }

        for (int i = 0, len = childrenJson.length(); i < len; i++) {
            JSONObject jsonObject;
            try {
                jsonObject = childrenJson.getJSONObject(i);
            } catch (JSONException e) {
                throw new IllegalStateException("Please check json file.");
            }

            children.add(getSegmentGroup(jsonObject, type));
        }
    }

    private JSONArray getDanceJson() {
        DanceFileHelper helper = new DanceFileHelper(mContext, mDanceFileName);
        try {
            return new JSONArray(helper.read());
        } catch (JSONException e) {
            throw new IllegalStateException("Please check dance file is jsonArray.");
        }
    }

    private final class DanceJsonKey {
        private static final String MAIN_TYPE = "mainType";
        private static final String DANCE_NAME = "danceName";
        private static final String CATEGORY = "category";
        private static final String TRACKS = "tracks";

        private static final String TYPE = "type";
        private static final String TRACK_DESCRIPTION = "trackDescription";
        private static final String SEGMENT_GROUP = "segmentGroup";

        private static final String NAME = "name";
        private static final String DESCRIPTION = "description";
        private static final String LOOPS = "loops";
        private static final String DURATION = "duration";
        private static final String IS_BLANK = "isBlank";
        private static final String OPTION = "option";

        private static final String CHILDREN = "children";

        private DanceJsonKey() {
        }
    }
}
