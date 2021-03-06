package com.example.android.popularmovies;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainFragment extends Fragment {

    private final String MOVIES = "movies";

    private MovieAdapter mMovieAdapter;
    private ArrayList<Movie> movieList;
    private String sortPreferences;

    public MainFragment() {
    }


    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(Movie movie);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sortPreferences = getSortPreference();
        if (savedInstanceState == null || !savedInstanceState.containsKey(MOVIES)) {
            movieList = new ArrayList<>();
            getMovies();
        } else {
            movieList = savedInstanceState.getParcelableArrayList(MOVIES);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(MOVIES, movieList);
        outState.putString("sortPreference", getSortPreference());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!sortPreferences.equals(getSortPreference())) {
            sortPreferences = getSortPreference();
            getMovies();
        }
    }

    private void getMovies() {
        if (getSortPreference().equals(getString(R.string.pref_sort_favorites))) {
            movieList.clear();
            movieList.addAll(MoviesUtility.getFavoriteMovies(getActivity()));
            if(mMovieAdapter != null) {
                mMovieAdapter.notifyDataSetChanged();
            }
        } else {
            FetchMovieDataTask movieDataTask = new FetchMovieDataTask();
            movieDataTask.execute();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mMovieAdapter = new MovieAdapter(getActivity(), movieList);

        GridView gridView = (GridView) rootView.findViewById(R.id.movies_grid);
        gridView.setAdapter(mMovieAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie selectedMovie = mMovieAdapter.getItem(position);
                ((Callback) getActivity()).onItemSelected(selectedMovie);
            }
        });
        return rootView;
    }

    public class FetchMovieDataTask extends AsyncTask<Void, Void, Movie[]> {

        private final String LOG_TAG = FetchMovieDataTask.class.getSimpleName();


        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private Movie[] getMoviesFromJson(String movieJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String MOVIES_LIST = "results";

            final String ID = "id";
            final String TITLE = "title";
            final String DESCRIPTION = "overview";
            final String IMAGE_URL = "poster_path";
            final String RATING = "vote_average";
            final String RELEASE_DATE = "release_date";

            JSONObject moviesJson = new JSONObject(movieJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(MOVIES_LIST);
            int moviesCount = moviesArray.length();

            Movie[] results = new Movie[moviesCount];
            for(int i = 0; i < moviesCount; i++) {
                int id;
                String title;
                String description;
                String imageUrl;
                String releaseDate;
                Double rating;
                // Get the JSON object representing the movie
                JSONObject movie = moviesArray.getJSONObject(i);

                id = movie.getInt(ID);
                title = movie.getString(TITLE);
                description = movie.getString(DESCRIPTION);
                imageUrl = movie.getString(IMAGE_URL);
                releaseDate = movie.getString(RELEASE_DATE);
                rating = movie.getDouble(RATING);

                results[i] = new Movie(id, title, description, imageUrl, releaseDate, rating);
            }

            return results;

        }

        @Override
        protected Movie[] doInBackground(Void... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;

            try {
                final String FORECAST_BASE_URL = "http://api.themoviedb.org/3/discover/movie";
                final String SORT_BY_PARAM = "sort_by";
                final String API_PARAM = "api_key";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_BY_PARAM, getSortPreference())
                        .appendQueryParameter(API_PARAM, getString(R.string.the_movie_db_api_key))
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to TheMovieDB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviesJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "IO Error ", e);
                // If the code didn't successfully get the movie data, there's no point in
                // attempting to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMoviesFromJson(moviesJsonStr);
            }
            catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

//            This return is only here in case the return above gets caught in an exception
            return null;
        }

        @Override
        protected void onPostExecute(Movie[] parsedMovies) {
            if (parsedMovies != null) {
                movieList.clear();
                for(Movie movie : parsedMovies) {
                    movieList.add(movie);
                }
                mMovieAdapter.notifyDataSetChanged();
            }
        }
    }

    private String getSortPreference() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return prefs.getString(getString(R.string.pref_sort_by_key),
                getString(R.string.pref_sort_by_default_value));
    }
}
