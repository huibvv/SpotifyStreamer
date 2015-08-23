package nl.idesign.spotifystreamer.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by huib on 8-6-2015.
 */
public class SpotifyStreamerDataContract {

    public static final String CONTENT_AUTHORITY = "nl.idesign.spotifystreamer";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_ARTISTS = "artists";
    public static final String PATH_TOP_TRACKS = "top_tracks";
    public static final String PATH_INCLUDE_ARTIST = "include";

    public static final class ArtistEntry implements BaseColumns{
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ARTISTS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ARTISTS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ARTISTS;

        public static final String TABLE_NAME = "artists";

        public static final String COLUMN_NAME_ARTIST_ID = "artist_id";

        public static Uri buildArtistUri(String artistID){
            return CONTENT_URI.buildUpon().appendPath(artistID).build();
        }

        public static String getArtistID(Uri artistUri){
            return artistUri.getPathSegments().get(1);
        }

    }

    public static final class TopTracksEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TOP_TRACKS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TOP_TRACKS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TOP_TRACKS;

        public static final String TABLE_NAME = "top_tracks";

        public static final String COLUMN_NAME_TRACK_ID = "track_id";
        public static final String COLUMN_NAME_ARTIST_ID = "artist_id";
        public static final String COLUMN_NAME_ARTIST_NAME = "artist_name";
        public static final String COLUMN_NAME_TRACK_NAME = "track_name";
        public static final String COLUMN_NAME_ALBUM_NAME = "album_name";
        public static final String COLUMN_NAME_ALBUM_THUMBNAIL = "album_thumbnail";
        public static final String COLUMN_NAME_TRACK_POPULARITY = "track_popularity";
        public static final String COLUMN_NAME_TRACK_PREVIEW_URL = "track_preview_url";

        public static Uri buildTopTrackUri(long trackID){
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(trackID)).build();
        }

        public static String getTopTrackID(Uri topTrackUri){
            return topTrackUri.getPathSegments().get(1);
        }
    }
}
