package com.ubtrobot.dance.player;

import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;

import com.ubtrobot.dance.music.MusicPlay;
import com.ubtrobot.play.AbstractSegmentPlayer;
import com.ubtrobot.play.Segment;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import org.json.JSONException;
import org.json.JSONObject;

public class MusicSegmentPlayer extends AbstractSegmentPlayer<MusicSegmentPlayer.MusicOption>
        implements MusicPlay.OnMusicPlayListener {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("MusicSegmentPlayer");

    private MusicPlay mPlay;

    public MusicSegmentPlayer(MusicPlay musicPlay, Segment<MusicOption> segment) {
        super(segment);
        mPlay = musicPlay;
    }

    @Override
    protected void onLoopStart(MusicOption option) {
        LOGGER.w("Music onLoopStart:" + option.toString());
        mPlay.play(option.getPath(), this, 10, false);
    }

    @Override
    protected void onLoopStop() {
        LOGGER.w("Music onLoopStop.");
//        notifyLoopStopped();
        mPlay.stop();
    }

    @Override
    protected void onEnd() {
        LOGGER.w("Music onEnd.");
        mPlay.stop();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        LOGGER.w("Music: onCompletion()");
        notifyLoopStopped();
    }

    @Override
    public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
        LOGGER.w("Music: onWaveFormDataCapture()");
    }

    @Override
    public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
        LOGGER.i("Music: onFftDataCapture()");
    }

    public static MusicOption parser(JSONObject optionJson) {
        try {
            return new MusicOption(optionJson.getString(MusicOptionKey.ID),
                    optionJson.getString(MusicOptionKey.NAME),
                    optionJson.getString(MusicOptionKey.PATH));
        } catch (JSONException e) {
            throw new IllegalStateException("Please check json: music track.");
        }
    }

    private final class MusicOptionKey {
        private static final String ID = "id";
        private static final String NAME = "name";
        private static final String PATH = "path";

        private MusicOptionKey() {
        }
    }

    public static class MusicOption {
        public String id;
        public String name;
        public String path;

        private MusicOption(String id, String name, String path) {
            this.id = id;
            this.name = name;
            this.path = path;
        }

        private String getId() {
            return id;
        }

        private String getName() {
            return name;
        }

        private String getPath() {
            return path;
        }

        @Override
        public String toString() {
            return "MusicOption{" +
                    "ID='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", path='" + path + '\'' +
                    '}';
        }
    }
}

