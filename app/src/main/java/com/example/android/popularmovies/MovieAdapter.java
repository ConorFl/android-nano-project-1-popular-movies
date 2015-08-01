package com.example.android.popularmovies;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by conorflanagan on 7/28/15.
 */
public class MovieAdapter extends ArrayAdapter<Movie> {
    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();
    private static final String BASE_POSTER_URL = "http://image.tmdb.org/t/p/w185/";

//    constructor
    public MovieAdapter(Activity context, List<Movie> movies) {
        super(context, 0, movies);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Movie movie = getItem(position);

        // Adapters recycle views to AdapterViews.
        // If this is a new View object we're getting, then inflate the layout.
        // If not, this view already has the layout inflated from a previous call to getView,
        // and we modify the View widgets as usual.
        // What is this now?
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.movie_item, parent, false);
        }
        ImageView movieImage = (ImageView) convertView.findViewById(R.id.movie_image);

        TextView titleView = (TextView) convertView.findViewById(R.id.movie_title);
        titleView.setText(movie.title);

        Picasso.with(getContext()).load(BASE_POSTER_URL + movie.imageUrl).into(movieImage);

        return convertView;
    }
}
