package nl.idesign.spotifystreamer;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by huib on 9/1/2015.
 */
public class Lifecycle implements Application.ActivityLifecycleCallbacks {

    private static final String LOG_TAG = Lifecycle.class.getSimpleName();
    private static Lifecycle sAppCycle;
    private int mActiveActivities = 0;

    public static void init(Application app){
        if(sAppCycle == null){
            sAppCycle = new Lifecycle();
            app.registerActivityLifecycleCallbacks(sAppCycle);
        }


    }

    public static Lifecycle getInstance(){
        return sAppCycle;
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.d(LOG_TAG, "[" + activity.getClass().getSimpleName() +"]onActivityStarted called");

        if(mActiveActivities == 0 && mListener != null){
            mListener.onApplicationForeground();
        }

        mActiveActivities++;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.d(LOG_TAG, "[" + activity.getClass().getSimpleName() +"]onActivityStopped called");
        if(mActiveActivities == 1 && mListener != null){
            mListener.onApplicationBackground();
        }
        mActiveActivities--;
    }

    private OnApplicationForegroundListener mListener;

    public void setOnApplicationForegroundListener(OnApplicationForegroundListener listener){
        mListener = listener;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public interface OnApplicationForegroundListener{
        void onApplicationForeground();
        void onApplicationBackground();
    }
}
