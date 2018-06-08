package com.ubtrobot.dance.parser;

import android.content.Context;

import com.ubtrobot.dance.player.ArmMotionSegmentPlayer;
import com.ubtrobot.dance.player.ChassisMotionSegmentPlayer;
import com.ubtrobot.dance.player.EmotionSegmentPlayer;
import com.ubtrobot.dance.player.MusicSegmentPlayer;
import com.ubtrobot.exception.RichException;

import org.json.JSONObject;

import static com.ubtrobot.dance.ipc.DanceConstants.TYPE_ARM_MOTION;
import static com.ubtrobot.dance.ipc.DanceConstants.TYPE_CHASSIS_MOTION;
import static com.ubtrobot.dance.ipc.DanceConstants.TYPE_EMOTION;
import static com.ubtrobot.dance.ipc.DanceConstants.TYPE_MUSIC;

public class DanceParserImp extends DanceParser {

    public DanceParserImp(Context context) {
        super(context, "dance/dance.json");
    }

    @Override
    protected <O> O parserOption(String type, JSONObject optionJson) {
        if (optionJson == null) {
            return null;
        }

        switch (type) {
            case TYPE_MUSIC:
                return (O) MusicSegmentPlayer.parser(optionJson);
            case TYPE_EMOTION:
                return (O) EmotionSegmentPlayer.parser(optionJson);
            case TYPE_ARM_MOTION:
                return (O) ArmMotionSegmentPlayer.parser(optionJson);
            case TYPE_CHASSIS_MOTION:
                return (O) ChassisMotionSegmentPlayer.parser(optionJson);
            default:
                throw new IllegalStateException("Type:" + type + " is not exits.");
        }
    }

}
