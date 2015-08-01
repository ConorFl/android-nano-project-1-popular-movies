package com.example.android.popularmovies;

/**
 * Created by conorflanagan on 7/28/15.
 */
public class Movie {
    String title;
    String overview;
    String imageUrl;

    public Movie(String title, String overview, String imageUrl) {
        this.title = title;
        this.overview = overview;
//        leave out image for now, gotta look into drawables, use lib mentioned in instructions
        this.imageUrl = imageUrl;
    }
}
