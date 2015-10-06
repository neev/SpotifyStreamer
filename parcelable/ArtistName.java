package com.example.android.spotifystreamer3.parcelable;


import android.os.Parcel;
import android.os.Parcelable;

public class ArtistName implements Parcelable {

    String artistName;
    String artistId;

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }


    public ArtistName(String artistName,String artistId) {
        this.artistName = artistName;
        this.artistId = artistId;
    }



    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(artistName);
        dest.writeString(artistId);
    }

    private void readFromParcel(Parcel in) {
        artistName = in.readString();
        artistId=in.readString();
    }
    public ArtistName(Parcel in){
        readFromParcel(in);
    }
    public static final Creator<ArtistName> CREATOR = new Creator<ArtistName>() {

        @Override
        public ArtistName createFromParcel(Parcel source) {
            return new ArtistName(source);
        }
        @Override
        public ArtistName[] newArray(int size) {
            return new ArtistName[size];
        }

    };

}

