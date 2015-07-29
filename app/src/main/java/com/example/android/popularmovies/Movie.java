package com.example.android.popularmovies;

/**
 * Created by conorflanagan on 7/28/15.
 */
public class Movie {
    String title;
    String overview;
    int image; // drawable ref id

    public Movie(String title, String overview) {
        this.title = title;
        this.overview = overview;
//        leave out image for now, gotta look into drawables, use lib mentioned in instructions
//        this.image = image;
    }
}
