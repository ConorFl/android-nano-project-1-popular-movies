package com.example.android.popularmovies;

import android.app.ActionBar;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DetailsFragment extends Fragment {

    static final String MOVIE_KEY = "movie";
    Movie movie;
    LinearLayout trailersLayout;

    public DetailsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (savedInstanceState != null) {
            // movie saved on screen rotation
            movie = savedInstanceState.getParcelable(MOVIE_KEY);
        } else if(args != null) {
        // movie saved in intent
        // right here is broken... extras is empty
            movie = args.getParcelable(MOVIE_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);
        trailersLayout = (LinearLayout) rootView.findViewById(R.id.trailer_buttons);
        if (movie != null) {
            populateView(rootView, movie);
            FetchMovieTrailersTask movieTrailersTask = new FetchMovieTrailersTask();
            movieTrailersTask.execute(movie.id);
        } else {
//            what to do if no movie because just opened ??
//            SHOW FIRST MOVIE
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
        Picasso.with(getActivity())
                .load("http://image.tmdb.org/t/p/w185" + posterUrl)
                .into(posterView);

        TextView synopsisView = (TextView) rootView.findViewById(R.id.movie_synopsis);
        synopsisView.setText(synopsis);

        TextView ratingView = (TextView) rootView.findViewById(R.id.movie_rating);
        ratingView.setText(Double.toString(rating));

        TextView releaseDateView = (TextView) rootView.findViewById(R.id.movie_release_date);
        releaseDateView.setText(releaseDate);
    }

    private void populateTrailerLinks(final String[] parsedTrailerUrls) {
        for (int i = 0; i < parsedTrailerUrls.length; i++) {
            Button button = new Button(getActivity());
            button.setText("Play Trailer ".concat(Integer.toString(i + 1)));
//            the drawable icons are still here but not used, consider removing
            final String url = parsedTrailerUrls[i];
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent youtubeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(youtubeIntent);
                }
            });
            trailersLayout.addView(button);
        }
    }

    public class FetchMovieTrailersTask extends AsyncTask<Integer, Void, String[]> {

        private final String LOG_TAG = FetchMovieTrailersTask.class.getSimpleName();

        // Get movie trailer URLs from JSON object of movie trailers for a given movie
        private String[] getTrailerUrlsFromJson(String trailersJsonStr) throws JSONException {
            final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch?v=";
            // These are the names of the JSON objects that need to be extracted.
            final String KEY = "key";
            final String TRAILERS_LIST = "results";

            JSONObject trailersJson = new JSONObject(trailersJsonStr);
            JSONArray trailersArray = trailersJson.getJSONArray(TRAILERS_LIST);
            int trailersCount = trailersArray.length();

            String[] trailerUrls = new String[trailersCount];
            for(int i = 0; i < trailersCount; i++) {
                String youtubeId;
                JSONObject trailer = trailersArray.getJSONObject(i);
                youtubeId = trailer.getString(KEY);
                trailerUrls[i] = YOUTUBE_BASE_URL.concat(youtubeId);
            }
            return trailerUrls;
        }

        @Override
        protected String[] doInBackground(Integer... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String trailersJsonStr = null;

            try {
                final String MOVIE_DB_BASE_URL = "http://api.themoviedb.org/3/movie";
                final String VIDEOS = "videos";
                final String API_PARAM = "api_key";
                final String movieIdStr = Integer.toString(params[0]);

                Uri builtUri = Uri.parse(MOVIE_DB_BASE_URL).buildUpon()
                        .appendPath(movieIdStr)
                        .appendPath(VIDEOS)
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
                trailersJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "IO Error ", e);
                // If the code didn't successfully get the trailer data, there's no point in
                // attempting to parse it.
                return null;
            } finally {
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
                return getTrailerUrlsFromJson(trailersJsonStr);
            }
            catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

//            This return is only here in case the return above gets caught in an exception
            return null;
        }

        private Integer[] getFavoriteMovieIds() {
            String arrayName = "favorites";
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            int favoritesCount = prefs.getInt(getString(R.string.pref_favorites_count), 0);
            Integer[] favoritesArray = new Integer[favoritesCount];
            for(int i = 0; i < favoritesCount; i++) {
                favoritesArray[i] = prefs.getInt(arrayName + "_" + i, 0);
            }
            return favoritesArray;
        }

        private boolean setFavoriteMovieIds(Integer[] movieIds) {
            String arrayName = "favorites";
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(getString(R.string.pref_favorites_count), movieIds.length);
            for(int i = 0; i < movieIds.length; i++) {
                editor.putInt(arrayName + "_" + i, movieIds[i]);
            }
            return editor.commit();

        }

//        public boolean saveArray(String[] array, String arrayName, Context mContext) {
//            SharedPreferences prefs = mContext.getSharedPreferences("preferencename", 0);
//            SharedPreferences.Editor editor = prefs.edit();
//            editor.putInt(arrayName +"_size", array.length);
//            for(int i=0;i<array.length;i++)
//                editor.putString(arrayName + "_" + i, array[i]);
//            return editor.commit();
//        }

        @Override
        protected void onPostExecute(String[] parsedMovies) {
            populateTrailerLinks(parsedMovies);
        }
    }
}
