package com.example.android.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by conorflanagan on 7/28/15.
 */
public class Movie implements Parcelable {
    int id;
    String title;
    String overview;
    String imageUrl;

    public Movie(int id, String title, String overview, String imageUrl) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.imageUrl = imageUrl;
    }

    private Movie(Parcel in) {
        id = in.readInt();
        title = in.readString();
        overview = in.readString();
        imageUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(overview);
        dest.writeString(imageUrl);
    }

    public final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {

        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
