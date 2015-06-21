package nl.idesign.spotifystreamer.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
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

    public static final String[] mProjection = new String[]{
            SpotifyStreamerDataContract.TopTracksEntry._ID,
            SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_TRACK_ID,
            SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_TRACK_NAME,
            SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_ALBUM_NAME,
            SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_ALBUM_THUMBNAIL
    };

    public final static int COL_ID = 0;
    public final static int COL_TRACK_ID = 1;
    public final static int COL_TRACK_NAME = 2;
    public final static int COL_ALBUM_NAME = 3;
    public final static int COL_THUMBNAIL = 4;

    private final LayoutInflater mInflater;

    public SpotifyTopTracksAdapter(Context context, boolean autoRequery) {
        super(context, null, autoRequery);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return mInflater.inflate(R.layout.spotify_top_tracks_list_item, parent, false);
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
        holder.mAlbumImageView.setContentDescription(String.format(context.getString(R.string.spotify_top_tracks_content_description), cursor.getString(COL_ALBUM_NAME)));
        if(imageUrl.isEmpty()){
            //Set a default image
        }else {
            Picasso.with(context).load(imageUrl).into(holder.mAlbumImageView);
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
