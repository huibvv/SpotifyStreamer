package nl.idesign.spotifystreamer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import kaaes.spotify.webapi.android.models.Artist;
import nl.idesign.spotifystreamer.R;

/**
 * Created by huib on 7-6-2015.
 */
public class SpotifySearchAdapter extends ArrayAdapter<Artist> {
    private final Context  mContext;

    public SpotifySearchAdapter(Context context) {
        super(context, R.layout.spotify_search_result_list_item);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.spotify_search_result_list_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder)convertView.getTag();
        }

        Artist artist = getItem(position);
        holder.mArtistName.setText(artist.name);

        holder.mArtistImage.setImageBitmap(null);
        holder.mArtistImage.setContentDescription(String.format(mContext.getString(R.string.spotify_search_content_description), artist.name));

        if(artist.images == null || artist.images.size() == 0){
            holder.mArtistImage.setImageResource(R.drawable.no_image);
        }else {
            Picasso.with(mContext).load(artist.images.get(0).url).into(holder.mArtistImage);
        }

        return convertView;
    }


    private class ViewHolder{
        public final TextView  mArtistName;
        public final ImageView  mArtistImage;

        public ViewHolder(View rootView){
            mArtistName = (TextView)rootView.findViewById(R.id.spotify_search_result_textview);
            mArtistImage = (ImageView)rootView.findViewById(R.id.spotify_search_result_imageview);
        }
    }
}
