package com.example.android.popularmovies;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FetchMovieDataTask movieDataTask = new FetchMovieDataTask();
        movieDataTask.execute();
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    public class FetchMovieDataTask extends AsyncTask<Void, Void, String[]> {

        private final String LOG_TAG = FetchMovieDataTask.class.getSimpleName();


        /* The date/time conversion code is going to be moved outside the asynctask later,
 * so for convenience we're breaking it out into its own method now.
 */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
//        private String formatHighLows(double high, double low) {
//            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//            String preferredUnits = prefs.getString(getString(R.string.pref_units_key),
//                    getString(R.string.pref_units_metric));
//            if (preferredUnits.equals(getString(R.string.pref_units_imperial))) {
//                high = convertToImperial(high);
//                low = convertToImperial(low);
//            }
//            // For presentation, assume the user doesn't care about tenths of a degree.
//            long roundedHigh = Math.round(high);
//            long roundedLow = Math.round(low);
//
//            String highLowStr = roundedHigh + "/" + roundedLow;
//            return highLowStr;
//        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getMovieDataFromJson(String movieJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String MOVIES_LIST = "results";
            final String TITLE = "title";
            final String DESCRIPTION = "overview";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray moviesArray = movieJson.getJSONArray(MOVIES_LIST);
            int moviesCount = moviesArray.length();

            String[] resultStrs = new String[moviesCount];
            for(int i = 0; i < moviesCount; i++) {
                String title;
                String description;

                // Get the JSON object representing the movie
                JSONObject movie = moviesArray.getJSONObject(i);

                // description is in a child array called "weather", which is 1 element long.
                title = movie.getString(TITLE);
                description = movie.getString(DESCRIPTION);
                resultStrs[i] = title + " - " + description;
            }

            return resultStrs;

        }

        @Override
        protected String[] doInBackground(Void... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;

            String sort = "popularity.desc";

            try {
                final String FORECAST_BASE_URL = "http://api.themoviedb.org/3/discover/movie";
                final String SORT_BY_PARAM = "sort_by";
                final String API_PARAM = "api_key";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_BY_PARAM, sort)
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
                // If the code didn't successfully get the movie data, there's no point in attemping
                // to parse it.
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
                Log.v(LOG_TAG, moviesJsonStr);
                return getMovieDataFromJson(moviesJsonStr);
            }
            catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

//            This return is only here in case the return above gets caught in an exception
            return null;
        }

        @Override
        protected void onPostExecute(String[] parsedMovies) {
            List<String> weekForecast = new ArrayList<String>(Arrays.asList(parsedMovies));

            if (weekForecast != null) {
                for(String movie : parsedMovies) {
                    Log.v(LOG_TAG, movie);
                }
//                mForecastAdapter.clear();
//                for (String dayForecast : weekForecast) {
//                    mForecastAdapter.add(dayForecast);
//                }
            }
        }
    }
}
