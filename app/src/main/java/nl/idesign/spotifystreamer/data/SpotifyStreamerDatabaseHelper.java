package nl.idesign.spotifystreamer.data;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

/**
 * Created by huib on 8-6-2015.
 */
public class SpotifyStreamerDatabaseHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = SpotifyStreamerDatabaseHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 7;

    private static final String DATABASE_NAME = "spotifystreamer.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String REAL_TYPE = " REAL";
    private static final String INT_TYPE = " INTEGER";
    private static final String BLOB_TYPE = " BLOB";
    private static final String COMMA_SEP = " , ";

    private static final String NOT_NULL = "  NOT NULL";

    private static SpotifyStreamerDatabaseHelper mInstance;

    private SpotifyStreamerDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static SpotifyStreamerDatabaseHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SpotifyStreamerDatabaseHelper(context.getApplicationContext());
        }
        return mInstance;
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        //Enable foreign keys for older Android versions. Foreign key enabling for Jelly Bean will occur in onConfigure
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            if (! db.isReadOnly()) {
                db.execSQL("PRAGMA foreign_keys = ON;");
            }
        }
        db.execSQL("PRAGMA recursive_triggers = true");
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        //Foreign keys need to be enabled manually, else they will not work.
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v(LOG_TAG, "Creating searched artist table");
        db.execSQL(createArtistTable());

        Log.v(LOG_TAG, "Creating top tracks table");
        db.execSQL(createTopTracksTable());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Just drop the tables for now...
        db.execSQL("DROP TABLE IF EXISTS " + SpotifyStreamerDataContract.TopTracksEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SpotifyStreamerDataContract.ArtistEntry.TABLE_NAME);
        this.onCreate(db);
    }

    private static String createArtistTable(){
        return  "CREATE TABLE " + SpotifyStreamerDataContract.ArtistEntry.TABLE_NAME + " (" +
                SpotifyStreamerDataContract.ArtistEntry._ID + " INTEGER PRIMARY KEY " + COMMA_SEP +
                SpotifyStreamerDataContract.ArtistEntry.COLUMN_NAME_ARTIST_ID + TEXT_TYPE + " UNIQUE " + NOT_NULL +  " )";
    }

    private static String createTopTracksTable(){
        return  "CREATE TABLE " + SpotifyStreamerDataContract.TopTracksEntry.TABLE_NAME + " (" +
                SpotifyStreamerDataContract.TopTracksEntry._ID + " INTEGER PRIMARY KEY " + COMMA_SEP +
                SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_ARTIST_ID + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_ARTIST_NAME + TEXT_TYPE + NOT_NULL +  COMMA_SEP +
                SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_TRACK_ID + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_ALBUM_NAME + TEXT_TYPE + COMMA_SEP +
                SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_TRACK_NAME + TEXT_TYPE + COMMA_SEP +
                SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_TRACK_POPULARITY + INT_TYPE + COMMA_SEP +
                SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_ALBUM_THUMBNAIL + TEXT_TYPE  + COMMA_SEP +
                SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_TRACK_PREVIEW_URL + TEXT_TYPE  + COMMA_SEP +
                " FOREIGN KEY(" + SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_ARTIST_ID + ") REFERENCES " +
                SpotifyStreamerDataContract.ArtistEntry.TABLE_NAME + " (" + SpotifyStreamerDataContract.ArtistEntry.COLUMN_NAME_ARTIST_ID + ") ON DELETE CASCADE ON UPDATE CASCADE" +" )";

    }
}
