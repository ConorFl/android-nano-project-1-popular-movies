package com.example.android.popularmovies;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailsFragment extends Fragment {

    static final String MOVIE_KEY = "movie";
    Movie movie;

    public DetailsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        TODO: REMOVE THIS, CAN'T BE READING FROM INTENTS IN TABLETS, GOTTA USE BUNDLES
        Intent intent = getActivity().getIntent();
        Bundle args = getArguments();
        if (savedInstanceState != null) {
            // movie saved on screen rotation
            movie = savedInstanceState.getParcelable(MOVIE_KEY);
        } else {
            // movie saved in intent
            // right here is broken... extras is empty
            if (args != null) {
                movie = args.getParcelable(MOVIE_KEY);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);
        if (movie != null) {
            populateView(rootView, movie);
        } else {
//            what to do if no movie because just opened app?
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(MOVIE_KEY, movie);
        super.onSaveInstanceState(outState);
    }

    private void populateView(View rootView, Movie movie) {
        String title = movie.title;
        String posterUrl = movie.imageUrl;
        String synopsis = movie.overview;
        String releaseDate = movie.releaseDate;
        Double rating = movie.rating;

        // Set the title on menu
        getActivity().setTitle(title);

        ImageView posterView = (ImageView) rootView.findViewById(R.id.movie_poster);
        Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w185" + posterUrl).into(posterView);

        TextView synopsisView = (TextView) rootView.findViewById(R.id.movie_synopsis);
        synopsisView.setText(synopsis);

        TextView ratingView = (TextView) rootView.findViewById(R.id.movie_rating);
        ratingView.setText(Double.toString(rating));

        TextView releaseDateView = (TextView) rootView.findViewById(R.id.movie_release_date);
        releaseDateView.setText(releaseDate);
    }
}
