package nl.idesign.spotifystreamer.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by huib on 8-6-2015.
 */
public class SpotifyStreamerProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private SpotifyStreamerDatabaseHelper mDbHelper;

    private static final int ARTISTS = 100;
    private static final int ARTIST = 101;

    private static final int TOP_TRACKS = 200;
    private static final int TOP_TRACK = 201;

    public SpotifyStreamerProvider() {
        super();
    }

    static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = SpotifyStreamerDataContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, SpotifyStreamerDataContract.PATH_ARTISTS, ARTISTS);
        uriMatcher.addURI(authority, SpotifyStreamerDataContract.PATH_ARTISTS + "/#", ARTIST);

        uriMatcher.addURI(authority, SpotifyStreamerDataContract.PATH_TOP_TRACKS, TOP_TRACKS);
        uriMatcher.addURI(authority, SpotifyStreamerDataContract.PATH_TOP_TRACKS + "/#", TOP_TRACK);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor retCursor;

        switch (sUriMatcher.match(uri)) {
            case ARTISTS: {
                retCursor = db.query(SpotifyStreamerDataContract.ArtistEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case ARTIST: {
                //First, get the id from the URI
                String artistId = SpotifyStreamerDataContract.ArtistEntry.getArtistID(uri);
                retCursor = db.query(SpotifyStreamerDataContract.ArtistEntry.TABLE_NAME,
                        projection,
                        SpotifyStreamerDataContract.ArtistEntry.COLUMN_NAME_ARTIST_ID + " = ? ",
                        new String[]{artistId},
                        null,
                        null,
                        sortOrder);
                break;
            }
            case TOP_TRACKS: {
                retCursor = db.query(SpotifyStreamerDataContract.TopTracksEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case TOP_TRACK: {
                //First, get the id from the URI
                String trackId = SpotifyStreamerDataContract.TopTracksEntry.getTopTrackID(uri);
                retCursor = db.query(SpotifyStreamerDataContract.TopTracksEntry.TABLE_NAME,
                        projection,
                        SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_TRACK_ID + " = ? ",
                        new String[]{trackId},
                        null,
                        null,
                        sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case ARTISTS:
                return SpotifyStreamerDataContract.ArtistEntry.CONTENT_TYPE;
            case ARTIST:
                return SpotifyStreamerDataContract.ArtistEntry.CONTENT_ITEM_TYPE;
            case TOP_TRACKS:
                return SpotifyStreamerDataContract.TopTracksEntry.CONTENT_TYPE;
            case TOP_TRACK:
                return SpotifyStreamerDataContract.TopTracksEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Uri returnUri;
        switch (sUriMatcher.match(uri)) {
            case ARTISTS: {
                long itemId = db.replace(SpotifyStreamerDataContract.ArtistEntry.TABLE_NAME, null,  values);
                if(itemId == -1){
                    //Failed to add a record
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                returnUri = SpotifyStreamerDataContract.ArtistEntry.buildArtistUri(values.getAsString(SpotifyStreamerDataContract.ArtistEntry.COLUMN_NAME_ARTIST_ID));

                break;
            }
            case TOP_TRACKS: {
                long itemId = db.replace(SpotifyStreamerDataContract.ArtistEntry.TABLE_NAME, null,  values);
                if(itemId == -1){
                    //Failed to add a record
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                returnUri = SpotifyStreamerDataContract.TopTracksEntry.buildTopTrackUri(values.getAsString(SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_TRACK_ID));

                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        switch (match) {
            case ARTISTS: {
                rowsDeleted = db.delete(SpotifyStreamerDataContract.ArtistEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case ARTIST: {
                String artistId = SpotifyStreamerDataContract.ArtistEntry.getArtistID(uri);
                rowsDeleted = db.delete(SpotifyStreamerDataContract.ArtistEntry.TABLE_NAME,
                        SpotifyStreamerDataContract.ArtistEntry.COLUMN_NAME_ARTIST_ID + " = ?",
                        new String[]{artistId});
                break;
            }
            case TOP_TRACKS: {
                rowsDeleted = db.delete(SpotifyStreamerDataContract.TopTracksEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case TOP_TRACK: {
                String artistId = SpotifyStreamerDataContract.TopTracksEntry.getTopTrackID(uri);
                rowsDeleted = db.delete(SpotifyStreamerDataContract.TopTracksEntry.TABLE_NAME,
                        SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_TRACK_ID + " = ?",
                        new String[]{artistId});
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if(rowsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case ARTISTS: {
                rowsUpdated = db.update(SpotifyStreamerDataContract.ArtistEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case ARTIST: {
                String artistId = SpotifyStreamerDataContract.ArtistEntry.getArtistID(uri);
                rowsUpdated = db.update(SpotifyStreamerDataContract.ArtistEntry.TABLE_NAME,
                        values,
                        SpotifyStreamerDataContract.ArtistEntry.COLUMN_NAME_ARTIST_ID + " = ?",
                        new String[]{artistId});
                break;
            }
            case TOP_TRACKS: {
                rowsUpdated = db.update(SpotifyStreamerDataContract.TopTracksEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case TOP_TRACK: {
                String artistId = SpotifyStreamerDataContract.TopTracksEntry.getTopTrackID(uri);
                rowsUpdated = db.update(SpotifyStreamerDataContract.TopTracksEntry.TABLE_NAME,
                        values,
                        SpotifyStreamerDataContract.TopTracksEntry.COLUMN_NAME_TRACK_ID + " = ?",
                        new String[]{artistId});
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if(rowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsInserted = 0;

        switch (match) {
            case ARTISTS: {
                db.beginTransaction();
                rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(SpotifyStreamerDataContract.ArtistEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                    break;
                } finally {
                    db.endTransaction();
                }
            }
            case TOP_TRACKS:{
                db.beginTransaction();
                rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(SpotifyStreamerDataContract.TopTracksEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                    break;
                } finally {
                    db.endTransaction();
                }
            } default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if(rowsInserted > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsInserted;
    }
}
