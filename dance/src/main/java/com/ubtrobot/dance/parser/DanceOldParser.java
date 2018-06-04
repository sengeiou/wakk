package com.ubtrobot.dance.parser;

import android.content.Context;

import com.ubtrobot.dance.Dance;
import com.ubtrobot.dance.DanceFileHelper;
import com.ubtrobot.play.Track;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DanceOldParser {

    private Context mContext;
    private String mDanceFileName;

    private List<Dance> mDanceList = new ArrayList<>();

    public DanceOldParser(Context context, String danceFileName) {
        if (danceFileName == null || danceFileName.length() == 0) {
            throw new IllegalArgumentException("Argument danceFileName is null.");
        }

        mContext = context;
        mDanceFileName = danceFileName;
    }

    public List<Dance> parser() {
        if (mDanceList.size() == 0) {
//            mDanceList.addAll(analysisDanceJson());
        }

        return mDanceList;
    }

    private void analysisDanceJson(LinkedList<Dance> danceList) {
        LinkedList<DanceOld> danceOlds = new LinkedList<>();
        JSONObject jsonObject = getJsonObject();
        try {
            JSONArray jsonArray = jsonObject.getJSONArray(DanceOldKey.DANCE_LIST);
            for (int i = 0, len = jsonArray.length(); i < len; i++) {
                JSONObject danceJson = jsonArray.getJSONObject(i);
                danceOlds.add(getDanceOld(danceJson));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        toDanceNew(danceList,danceOlds);
    }

    private void toDanceNew(LinkedList<Dance> danceList, LinkedList<DanceOld> danceOlds) {
        // todo 原来的格式，转换为我们自己的格式
        for (DanceOld danceOld : danceOlds) {
            LinkedList<Track> tracks = new LinkedList<>();

            // todo 有些值有待确认，目前写死
//            Dance dance = new Dance(danceOld.getName(), danceOld.getName(), "music", tracks);
        }
    }

    private DanceOld getDanceOld(JSONObject danceJson) {
        try {
            JSONArray armActionJsonArray = danceJson.getJSONArray(DanceOldKey.ARM_ACTIONS);
            int armActionLen = armActionJsonArray.length();
            String[] armActions = new String[armActionLen];
            for (int i = 0, len = armActionJsonArray.length(); i < len; i++) {
                armActions[i] = armActionJsonArray.getString(i);
            }

            return new DanceOld().setName(danceJson.getString(DanceOldKey.NAME)).
                    setArmActions(armActions).
                    setExpression(DanceOldKey.EXPRESSION).
                    setLight(DanceOldKey.LIGHT).
                    setMusic(DanceOldKey.MUSIC).
                    setTts(DanceOldKey.TTS);
        } catch (JSONException e) {
            throw new IllegalStateException("Please check json file.");
        }
    }

    private JSONObject getJsonObject() {
        DanceFileHelper helper = new DanceFileHelper(mContext, mDanceFileName);
        try {
            return new JSONObject(helper.read());
        } catch (JSONException e) {
            throw new IllegalStateException("Please check json file.");
        }
    }

    private class DanceOld {
        private String name;
        private String tts;
        private String[] armActions;
        private String music;
        private String expression;
        private String light;

        public String getName() {
            return name;
        }

        public DanceOld setName(String name) {
            this.name = name;
            return this;
        }

        public String getTts() {
            return tts;
        }

        public DanceOld setTts(String tts) {
            this.tts = tts;
            return this;
        }

        public String[] getArmActions() {
            return armActions;
        }

        public DanceOld setArmActions(String[] armActions) {
            this.armActions = armActions;
            return this;
        }

        public String getMusic() {
            return music;
        }

        public DanceOld setMusic(String music) {
            this.music = music;
            return this;
        }

        public String getExpression() {
            return expression;
        }

        public DanceOld setExpression(String expression) {
            this.expression = expression;
            return this;
        }

        public String getLight() {
            return light;
        }

        public DanceOld setLight(String light) {
            this.light = light;
            return this;
        }

        @Override
        public String toString() {
            return "DanceOld{" +
                    "name='" + name + '\'' +
                    ", tts='" + tts + '\'' +
                    ", armActions=" + Arrays.toString(armActions) +
                    ", music='" + music + '\'' +
                    ", expression='" + expression + '\'' +
                    ", light='" + light + '\'' +
                    '}';
        }
    }

    private final class DanceOldKey {
        private static final String DANCE_LIST = "danceList";

        private static final String NAME = "name";
        private static final String TTS = "tts";
        private static final String ARM_ACTIONS = "armActions";
        private static final String MUSIC = "music";
        private static final String EXPRESSION = "expression";
        private static final String LIGHT = "light";
    }
}
