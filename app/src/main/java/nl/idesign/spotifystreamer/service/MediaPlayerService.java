package nl.idesign.spotifystreamer.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import java.io.IOException;

public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener
{

    private static final int RUNNABLE_TIME = 100;

    private final ServiceBinder mBinder = new ServiceBinder();

    private String mMediaUrl;
    private MediaPlayer mMediaPlayer;

    private Handler mProgressHandler = new Handler();

    public MediaPlayerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void loadMediaPlayer(String url){
        if(mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnBufferingUpdateListener(this);
            mMediaPlayer.setOnCompletionListener(this);

        }

        if(!url.equals(mMediaUrl)){
            mMediaPlayer.reset();
            try {
                mMediaPlayer.setDataSource(url);
                mMediaPlayer.prepareAsync();
            }catch(IOException ex){
                if(mOnErrorListener != null){
                    mOnErrorListener.onError(mMediaPlayer,99,-1);
                }
            }
        }else {
            if(mOnPreparedListener != null){
                mOnPreparedListener.onPrepared(mMediaPlayer);
            }
        }


        mMediaUrl = url;
    }

    public String getMediaUrl(){
        return mMediaUrl;
    }

    public void resume(){
        mMediaPlayer.start();
    }

    public void pause(){
        mMediaPlayer.pause();
    }


    public class ServiceBinder extends Binder {
        public MediaPlayerService getService(){
            return MediaPlayerService.this;
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if(mOnBufferingUpdateListener != null){
            mOnBufferingUpdateListener.onBufferingUpdate(mp,percent);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(mOnCompletionListener != null){
            mOnCompletionListener.onCompletion(mp);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if(mOnErrorListener != null){
            mOnErrorListener.onError(mp,what,extra);
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mMediaPlayer.start();
        mProgressHandler.postDelayed(mProgressRunnable, RUNNABLE_TIME);
        if(mOnPreparedListener != null){
            mOnPreparedListener.onPrepared(mp);
        }
    }

    private OnPreparedListener mOnPreparedListener;
    public interface OnPreparedListener{
        void onPrepared(MediaPlayer mp);
    }

    public void setOnPreparedListener(OnPreparedListener listener){
        mOnPreparedListener = listener;
    }

    private OnErrorListener mOnErrorListener;
    public interface OnErrorListener{
        void onError(MediaPlayer mp, int what, int extra);
    }

    public void setOnErrorListener(OnErrorListener listener){
        mOnErrorListener = listener;
    }

    private OnCompletionListener mOnCompletionListener;
    public interface OnCompletionListener{
        void onCompletion(MediaPlayer mp);
    }

    public void setOnCompletionListener(OnCompletionListener listener){
        mOnCompletionListener = listener;
    }

    private OnBufferingUpdateListener mOnBufferingUpdateListener;
    public interface OnBufferingUpdateListener{
        void onBufferingUpdate(MediaPlayer mp, int percent);
    }

    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener){
        mOnBufferingUpdateListener = listener;
    }

    private OnPlaybackUpdateListener mOnPlaybackUpdateListener;
    public interface OnPlaybackUpdateListener{
        void onPlaybackUpdate(int duration);
    }

    public void setOnPlaybackUpdateListener(OnPlaybackUpdateListener listener){
        mOnPlaybackUpdateListener = listener;
    }


    private Runnable mProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if(mOnPlaybackUpdateListener != null){
                mOnPlaybackUpdateListener.onPlaybackUpdate(mMediaPlayer.getCurrentPosition());
            }
            mProgressHandler.postDelayed(mProgressRunnable, RUNNABLE_TIME);
        }
    };

}
