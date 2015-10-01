package com.example.android.popularmovies;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity implements MainFragment.Callback {
    private boolean mTwoPanes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.details_view_container) != null) {
            mTwoPanes = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.details_view_container, new DetailsFragment())
                        .commit();
                getSupportFragmentManager().findFragmentById(R.id.main_fragment).getActivity();
            }
        } else {
            mTwoPanes = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Movie movie) {
        if (mTwoPanes) {
            // launch details frag
            Bundle args = new Bundle();
            args.putParcelable(DetailsFragment.MOVIE_KEY, movie);

            DetailsFragment fragment = new DetailsFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.details_view_container, fragment)
                    .commit();
        } else {
            Intent detailsIntent = new Intent(this, DetailsActivity.class);
            detailsIntent.putExtra(DetailsFragment.MOVIE_KEY, movie);
            startActivity(detailsIntent);
        }
    }
}
