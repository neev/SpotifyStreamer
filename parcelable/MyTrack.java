package com.example.android.spotifystreamer3.parcelable;

import android.os.Parcel;
import android.os.Parcelable;

public class MyTrack implements Parcelable {



    String artist_names;
    String imageurl;
    String SpotifyArtistID;

    public MyTrack(){}

    public MyTrack(String artist_names, String spotifyArtistID) {
        this.artist_names = artist_names;
        SpotifyArtistID = spotifyArtistID;
    }

    public MyTrack(String artist_names, String imageurl, String spotifyArtistID) {
        this.artist_names = artist_names;
        this.imageurl = imageurl;
        SpotifyArtistID = spotifyArtistID;
    }

    public MyTrack(String artist_names) {
        this.artist_names = artist_names;
    }

    public String getSpotifyArtistID() {
        return SpotifyArtistID;
    }


    public String getArtist_names() {
        return artist_names;
    }



    public String getImageurl() {
        return this.imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }
    public void setArtist_names(String artist_names) {
        this.artist_names = artist_names;
    }

    public void setSpotifyArtistID(String spotifyArtistID) {
        SpotifyArtistID = spotifyArtistID;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(artist_names);
        dest.writeString(imageurl);
        dest.writeString(SpotifyArtistID);
    }
    public final Creator<MyTrack> CREATOR = new Creator<MyTrack>() {
        public MyTrack createFromParcel(Parcel in) {
            return new MyTrack(in);
        }

        public MyTrack[] newArray(int size) {
            return new MyTrack[size];
        }
    };
    public MyTrack(Parcel in) {
        this();
        readFromParcel(in);
    }
    private void readFromParcel(Parcel in) {
        this.artist_names = in.readString();
        this.imageurl = in.readString();
        this.SpotifyArtistID = in.readString();

    }
}



