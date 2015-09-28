/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import java.util.ArrayList;

public class MoviesUtility {
    static Gson gson = new Gson();

    public static ArrayList<Movie> getFavoriteMovies(Context context) {
        String arrayName = "favorites";
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int favoritesCount = prefs.getInt(context.getString(R.string.pref_favorites_count), 0);
        ArrayList<Movie> favoritesArray = new ArrayList<>();
        for(int i = 0; i < favoritesCount; i++) {
            favoritesArray
                    .add(gson.fromJson(prefs.getString(arrayName + "_" + i, null), Movie.class));
        }
        return favoritesArray;
    }

    public static boolean setFavoriteMovies(Context context, ArrayList<Movie> favoriteMovies) {
        String arrayName = "favorites";
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        int oldFavoritesCount = prefs.getInt(context.getString(R.string.pref_favorites_count), 0);
        // remove old ones
        for(int i = 0; i < oldFavoritesCount; i++) {
            editor.remove(arrayName + "_" + i);
        }
        // add new ones
        editor.putInt(context.getString(R.string.pref_favorites_count), favoriteMovies.size());
        for(int i = 0; i < favoriteMovies.size(); i++) {
            editor.putString(arrayName + "_" + i, gson.toJson(favoriteMovies.get(i), Movie.class));
        }
        return editor.commit();

    }
}
