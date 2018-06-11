package com.ubtrobot.dance.music;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;

import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.io.IOException;

/**
 * 封装MediaPlayer播放音乐，封装一个Visualizer获取FFT数据
 */
public class MusicPlay {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("MusicPlay");

    private Context mContext;

    /**
     * 音乐播放器,播放器
     */
    private MediaPlayer mPlayer;
    /**
     * 音频数据采样可视化
     */
    private Visualizer mVisualizer;
    /**
     * 均衡器
     */
    private Equalizer mEqualizer;
    /**
     * 采样数据最小长度
     */
    private int mMinCaptureSize;
    /**
     * 采样数据最大长度
     */
    private int mMaxCaptureSize;
    /**
     * 数据采样频率
     */
    private int mMaxCaptureRate;

    public MusicPlay(Context context){
        mContext = context.getApplicationContext();
        mPlayer = new MediaPlayer();
        mVisualizer = new Visualizer(mPlayer.getAudioSessionId());
        mEqualizer = new Equalizer(0, mPlayer.getAudioSessionId());
        initVisualizerParam();
    }

    private void initVisualizerParam(){
        mMinCaptureSize = Visualizer.getCaptureSizeRange()[0];
        mMaxCaptureSize = Visualizer.getCaptureSizeRange()[1];
        mMaxCaptureRate = Visualizer.getMaxCaptureRate();

        LOGGER.d("Visualizer Capture Range: " + mMinCaptureSize + " ~ " + mMaxCaptureSize);
        LOGGER.d("Visualizer Max CaptureRate: " + mMaxCaptureRate / 1000);
    }

    private boolean checkCaptureRate(int rate){
        if(rate <= 0 && rate > mMaxCaptureRate){
            LOGGER.d("Illegal capture rate ---> " + rate);
            return false;
        }
        return true;
    }

    /**
     * play specified path music source
     * @param path music path
     * @param listener listen the actions of playing
     * @param rate the rate of capture FFT data. (Hz)
     * @param effectByVolume whether it is effected by the volume.
     * @return the duration of the file related to the path.
     */
    public int play(String path, OnMusicPlayListener listener, int rate, boolean effectByVolume){
        LOGGER.w("Internal MediaPlayer play " + path);
        if(mPlayer.isPlaying()){
            mPlayer.stop();
        }
        mPlayer.reset();
        mPlayer.setOnCompletionListener(listener);

        try {
            AssetFileDescriptor descriptor = mContext.getAssets().openFd(path);
            mPlayer.setDataSource(descriptor.getFileDescriptor(),
                    descriptor.getStartOffset(),descriptor.getLength());
            mPlayer.prepare();
            int duration = mPlayer.getDuration();

            if(!checkCaptureRate(rate)){
                rate = mMaxCaptureRate / 2;
            }

            //设置数据FFT数据监听
            mVisualizer.setDataCaptureListener(listener, rate * 1000,false,true);
            mVisualizer.setEnabled(true);

            //设置FFT数据是否受系统音量控制
            mEqualizer.setEnabled(!effectByVolume);
            mPlayer.start();

            return duration;
        } catch (IOException e) {
            // TODO
            LOGGER.e("Internal MediaPlayer play fail.");
            return -1;
        }
    }

    public boolean isPlaying(){
        if(mPlayer != null){
            return mPlayer.isPlaying();
        }
        return false;
    }

    public void stop(){
        LOGGER.d("Stop internal MediaPlayer...");

        if(mPlayer != null && mPlayer.isPlaying()){
            mPlayer.stop();
        }

        if(mVisualizer != null){
            LOGGER.d("Stop internal mVisualizer...");
            mVisualizer.setEnabled(false);
            mVisualizer.setDataCaptureListener(null,
                    mMaxCaptureRate / 2,false,false);
        }

        if(mEqualizer != null){
            LOGGER.d("Stop internal mEqualizer...");
            mEqualizer.setEnabled(false);
        }
    }

    public void release(){
        LOGGER.d("Release internal MediaPlayer...");
        stop();

        if(mPlayer != null){
            mPlayer.release();
            mPlayer = null;
        }

        if(mVisualizer != null){
            mVisualizer.release();
            mVisualizer = null;
        }

        if(mEqualizer != null){
            mEqualizer.release();
            mEqualizer = null;
        }
    }

    public interface OnMusicPlayListener extends MediaPlayer.OnCompletionListener,
            Visualizer.OnDataCaptureListener{

        @Override
        void onCompletion(MediaPlayer mp);

        @Override
        void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate);

        @Override
        void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate);
    }

}
