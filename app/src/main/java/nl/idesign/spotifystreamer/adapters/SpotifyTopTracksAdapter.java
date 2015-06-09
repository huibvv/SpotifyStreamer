package nl.idesign.spotifystreamer.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import nl.idesign.spotifystreamer.R;
import nl.idesign.spotifystreamer.data.SpotifyStreamerDataContract;

/**
 * Created by huib on 9-6-2015.
 */
public class SpotifyTopTracksAdapter extends CursorAdapter {

    public static String[] mProjection = new String[]{
            SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_TRACK_ID,
            SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_TRACK_NAME,
            SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_ALBUM_NAME,
            SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_ALBUM_THUMBNAIL
    };

    public static int COL_TRACK_ID = 0;
    public static int COL_TRACK_NAME = 1;
    public static int COL_ALBUM_NAME = 2;
    public static int COL_THUMBNAIL = 3;

private Context mContext;

    public SpotifyTopTracksAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder holder =  (ViewHolder)view.getTag();
        if(holder == null){
            holder = new ViewHolder(view);
        }

        holder.mAlbumNameTextView.setText(cursor.getString(COL_ALBUM_NAME));
        holder.mTrackNameTextView.setText(cursor.getString(COL_TRACK_NAME));

        String imageUrl = cursor.getString(COL_THUMBNAIL);
        if(imageUrl.isEmpty()){
            //Set a default image
        }else {
            Picasso.with(mContext).load(imageUrl).into(holder.mAlbumImageView);
        }
    }

    private class ViewHolder{
        public ImageView mAlbumImageView;
        public TextView mTrackNameTextView;
        public TextView mAlbumNameTextView;

        public ViewHolder(View rootView){
            mAlbumImageView = (ImageView)rootView.findViewById(R.id.spotify_top_track_album_imageview);
            mTrackNameTextView = (TextView)rootView.findViewById(R.id.spotify_top_track_name_textview);
            mAlbumNameTextView = (TextView)rootView.findViewById(R.id.spotify_top_track_album_name_textview);
        }
    }
}
