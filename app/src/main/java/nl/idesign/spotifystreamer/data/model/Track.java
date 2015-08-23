package nl.idesign.spotifystreamer.data.model;

/**
 * Created by huib on 7-7-2015.
 */
public class Track {
    private String mAlbumName;
    private String mTrackname;
    private String mPreviewUrl;
    private String mThumbnail;
    private String mArtistName;
    private String mArtistId;
    private int mPopularity;
    private long mId;

    public String getAlbumName() {
        return mAlbumName;
    }

    public void setAlbumName(String mAlbumName) {
        this.mAlbumName = mAlbumName;
    }

    public String getTrackname() {
        return mTrackname;
    }

    public void setTrackname(String mTrackname) {
        this.mTrackname = mTrackname;
    }

    public String getPreviewUrl() {
        return mPreviewUrl;
    }

    public void setPreviewUrl(String mPreviewUrl) {
        this.mPreviewUrl = mPreviewUrl;
    }

    public String getThumbnail() {
        return mThumbnail;
    }

    public void setThumbnail(String mThumbnail) {
        this.mThumbnail = mThumbnail;
    }

    public String getArtistName() {
        return mArtistName;
    }

    public void setArtistName(String mArtistName) {
        this.mArtistName = mArtistName;
    }

    public String getArtistId() {
        return mArtistId;
    }

    public void setArtistId(String mArtistId) {
        this.mArtistId = mArtistId;
    }

    public int getPopularity() {
        return mPopularity;
    }

    public void setPopularity(int mPopularity) {
        this.mPopularity = mPopularity;
    }

    public long getId() {
        return mId;
    }

    public void setId(long mId) {
        this.mId = mId;
    }
}
