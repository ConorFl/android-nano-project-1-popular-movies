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
    String releaseDate;
    Double rating;

    public Movie(int id, String title, String overview, String imageUrl, String releaseDate, Double rating) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.imageUrl = imageUrl;
        this.releaseDate = releaseDate;
        this.rating = rating;
    }

    private Movie(Parcel in) {
        id = in.readInt();
        title = in.readString();
        overview = in.readString();
        imageUrl = in.readString();
        releaseDate = in.readString();
        rating = in.readDouble();
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
        dest.writeString(releaseDate);
        dest.writeDouble(rating);
    }

    public static Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {

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
