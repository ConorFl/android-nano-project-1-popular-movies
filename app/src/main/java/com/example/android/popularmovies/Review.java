package com.example.android.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by conorflanagan on 9/29/15.
 */

public class Review implements Parcelable {
    int id;
    String author;
    String content;

    public Review(int id, String author, String content) {
        this.id = id;
        this.author = author;
        this.content = content;
    }

    private Review(Parcel in) {
        id = in.readInt();
        author = in.readString();
        content = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(author);
        dest.writeString(content);
    }

    public static Parcelable.Creator<Review> CREATOR = new Parcelable.Creator<Review>() {

        @Override
        public Review createFromParcel(Parcel source) {
            return new Review(source);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };
}
