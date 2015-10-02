package com.example.android.spotifystreamer3.parcelable;

import android.os.Parcel;
import android.os.Parcelable;

public class SongParcelable implements Parcelable {

    private String artistName;
    private String albumName;
    private String imageUrl;
    private String trackName;
    private String trackPreviewUrl;

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getTrackPreviewUrl() {
        return trackPreviewUrl;
    }

    public void setTrackPreviewUrl(String trackPreviewUrl) {
        this.trackPreviewUrl = trackPreviewUrl;
    }
//constructor


    public SongParcelable() {
    }

    public SongParcelable(String artistName, String albumName, String imageUrl, String trackName, String trackPreviewUrl) {
        this.artistName = artistName;
        this.albumName = albumName;
        this.imageUrl = imageUrl;
        this.trackName = trackName;
        this.trackPreviewUrl = trackPreviewUrl;
    }
    @SuppressWarnings("unused")
    public SongParcelable(Parcel in) {
        this();
        readFromParcel(in);
    }
    private void readFromParcel(Parcel in) {
        this.artistName = in.readString();
        this.albumName = in.readString();
        this.imageUrl = in.readString();
        this.trackName = in.readString();
        this.trackPreviewUrl = in.readString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(artistName);
        dest.writeString(albumName);
        dest.writeString(imageUrl);
        dest.writeString(trackName);
        dest.writeString(trackPreviewUrl);
    }

    public final Creator<SongParcelable> CREATOR = new Creator<SongParcelable>() {
        public SongParcelable createFromParcel(Parcel in) {
            return new SongParcelable(in);
        }

        public SongParcelable[] newArray(int size) {
            return new SongParcelable[size];
        }
    };
}
