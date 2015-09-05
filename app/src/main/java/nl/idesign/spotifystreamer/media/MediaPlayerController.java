package nl.idesign.spotifystreamer.media;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.MediaController;

/**
 * Created by huib on 8/26/2015.
 */
public class MediaPlayerController extends MediaController {
    public static final String LOG_TAG = MediaPlayerController.class.getSimpleName();

    public MediaPlayerController(Context context) {
        super(context);
    }

    public MediaPlayerController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Override to stop the player from hiding
     */
    @Override
    public void hide() {
        super.hide();
        //Log.d(LOG_TAG, "Should hide mediacontroller");
    }


}
