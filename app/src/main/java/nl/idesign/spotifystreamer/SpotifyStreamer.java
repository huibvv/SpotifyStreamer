package nl.idesign.spotifystreamer;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import nl.idesign.spotifystreamer.service.MediaPlayerService;

/**
 * Created by huib on 8/24/2015.
 */
public class SpotifyStreamer extends Application  implements Lifecycle.OnApplicationForegroundListener {

    private static final String LOG_TAG = SpotifyStreamer.class.getSimpleName();

    private MediaPlayerService mService;
    private boolean mIsBound = false;

    @Override
    public void onCreate() {
        super.onCreate();
        startService();
        doBindService();
        Lifecycle.init(this);
        Lifecycle.getInstance().setOnApplicationForegroundListener(this);
    }

    // Method to start the service
    private void startService() {
        Log.d(LOG_TAG, "[SpotifyStreamer]Starting service");
        startService(new Intent(getBaseContext(), MediaPlayerService.class));
    }

    // Method to stop the service
    private void stopService() {
        Log.d(LOG_TAG, "[SpotifyStreamer]Stopping service");
        stopService(new Intent(getBaseContext(), MediaPlayerService.class));
    }

    private void doBindService() {
        Log.d(LOG_TAG, "[SpotifyStreamer]Binding service");
        bindService(new Intent(this, MediaPlayerService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    private void doUnbindService() {
        Log.d(LOG_TAG, "[SpotifyStreamer]Unbinding service");
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
            mService = null;
        }
    }

    public MediaPlayerService getMediaService(){
        return mService;
    }

    /**
     * Developer docs Android Bound service
     */
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.ServiceBinder binder = (MediaPlayerService.ServiceBinder) service;
            mService = binder.getService();
            if(mOnServiceBindListener != null){
                mOnServiceBindListener.onServiceBound(mService);
            }
            mIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mIsBound = false;
            mService = null;
        }
    };

    private OnServiceBindListener mOnServiceBindListener;

    public void setOnServiceBindListener(OnServiceBindListener listener){
        mOnServiceBindListener = listener;
    }

    public interface OnServiceBindListener{
        void onServiceBound(MediaPlayerService mp);
        void onServiceUnbound();
    }

    @Override
    public void onApplicationForeground() {
        if(mService != null && mService.isPlaying()){
            mService.hideMediaNotification();
        }
    }

    @Override
    public void onApplicationBackground() {
        if(mService != null && mService.isPlaying()){
            mService.showMediaNotificationAsync();
        }
    }
}
