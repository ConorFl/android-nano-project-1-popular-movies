package com.example.android.popularmovies;

/**
 * Created by conorflanagan on 7/28/15.
 */
public class Movie {
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
}
