package com.example.android.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailsActivityFragment extends Fragment {

    View rootView;
    // ^ ugly?

    public DetailsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_details, container, false);

        Intent intent = getActivity().getIntent();
//        Default to 'Fight Club'.  Better options?
        int id = intent.getIntExtra(Intent.EXTRA_TEXT, 550);

        FetchMovieDetailsDataTask fetchDetailsTask = new FetchMovieDetailsDataTask();
        fetchDetailsTask.execute(id);

        return rootView;
    }

    public class FetchMovieDetailsDataTask extends AsyncTask<Integer, Void, String> {

        private final String LOG_TAG = FetchMovieDetailsDataTask.class.getSimpleName();

        private Void setMovieDetails() {
            return null;
        }

        @Override
        protected String doInBackground(Integer... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;

            try {
                final String FORECAST_BASE_URL = "https://api.themoviedb.org/3/movie";
                final String API_PARAM = "api_key";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendPath(Integer.toString(params[0]))
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
                Log.e(LOG_TAG, "Error ", e);
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
                Log.v("CFMOVIEDETAILS", moviesJsonStr);
                return moviesJsonStr;
            }
            catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

//            This return is only here in case the return above gets caught in an exception
            return null;
        }

        @Override
        protected void onPostExecute(String movieJsonStr) {
            String originalTitle;
            String posterUrl;
            String synopsis;
            Double rating;
            String releaseDate;
            try {
                JSONObject movieJson = new JSONObject(movieJsonStr);
                originalTitle = movieJson.getString("original_title");
                posterUrl = movieJson.getString("poster_path");
                synopsis = movieJson.getString("overview");
                rating = movieJson.getDouble("vote_average");
                releaseDate = movieJson.getString("release_date");

                ImageView posterView = (ImageView) rootView.findViewById(R.id.details_poster);
                Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w185" + posterUrl).into(posterView);
//                Picasso.with(getContext()).load(BASE_POSTER_URL + movie.imageUrl).into(movieImage);


                TextView titleView = (TextView) rootView.findViewById(R.id.movie_title);
                titleView.setText(originalTitle);

                TextView synopsisView = (TextView) rootView.findViewById(R.id.movie_synopsis);
                synopsisView.setText(synopsis);

                TextView ratingView = (TextView) rootView.findViewById(R.id.movie_rating);
                ratingView.setText(Double.toString(rating));

                TextView releaseDateView = (TextView) rootView.findViewById(R.id.movie_release_date);
                releaseDateView.setText(releaseDate);

                //            original title
                //            movie poster image thumbnail
                //            A plot synopsis (called overview in the api)
                //            user rating (called vote_average in the api)
                //            release date
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
        }
    }
}
