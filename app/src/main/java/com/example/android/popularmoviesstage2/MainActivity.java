package com.example.android.popularmoviesstage2;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmoviesstage2.adapters.MovieAdapter;
import com.example.android.popularmoviesstage2.data.FavoritesContract;
import com.example.android.popularmoviesstage2.utilities.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnclickHandler{

    private RecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;
    private ImageButton mRightButton;
    private ImageButton mLeftButton;
    private TextView mPageTextview;
    private LinearLayout mLayout;

    private static final int MOVIE_LOADER_ID = 0;
    private static final int FAVORITES_LOADER_ID = 1;


    private String sort_by_popularity = "popular";
    private String sort_by_highest_rated = "top_rated";
    private String sort_by_favorites = "favorites";
    private String mSortPref = null;
    int mCurrentPageNo = 1, mTotalPageNo = 0;
    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;
    private String keyPref = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mSortPref = sort_by_favorites;
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_main);
        mRightButton = (ImageButton) findViewById(R.id.right_arrow);
        mLeftButton = (ImageButton) findViewById(R.id.left_arrow);
        mPageTextview = (TextView) findViewById(R.id.page_num_tv);
        mLayout = (LinearLayout) findViewById(R.id.linear_layout);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, 1));
        } else {
            mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(5, 1));
        }

        mRecyclerView.setHasFixedSize(true);
        mMovieAdapter = new MovieAdapter(this);
        keyPref = getString((R.string.tmdb_api_key));



            mRecyclerView.setAdapter(mMovieAdapter);
            updateLayout();


        setmLeftButton();
        setmRightButton();
        mPageTextview.setText("Page " + mCurrentPageNo + " of " + mTotalPageNo);

        if (mCurrentPageNo == 1)
            mLeftButton.setVisibility(View.INVISIBLE);
        else
            mLeftButton.setVisibility(View.VISIBLE);


    }

    public void updateLayoutSwitches() {
        if (mCurrentPageNo == 1)
            mLeftButton.setVisibility(View.INVISIBLE);
        else
            mLeftButton.setVisibility(View.VISIBLE);

        Bundle queryBundle = new Bundle();
        queryBundle.putString("storeSortPref", mSortPref);
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String[]> loader = loaderManager.getLoader(MOVIE_LOADER_ID);
        Loader<Cursor> loader1 = loaderManager.getLoader(FAVORITES_LOADER_ID);





            if (isNetworkAvailable() == true) {
                showMovieDataView();
                mMovieAdapter.setMovieData(null);
                if(loader == null)
                    loaderManager.initLoader(MOVIE_LOADER_ID,queryBundle,new ArrayCallback());
                else
                    loaderManager.restartLoader(MOVIE_LOADER_ID,queryBundle,new ArrayCallback());
                mPageTextview.setText("Page " + mCurrentPageNo + " of " + mTotalPageNo);
            } else {
                showErrorMessage();
            }




        if (mCurrentPageNo == mTotalPageNo)
            mRightButton.setVisibility(View.INVISIBLE);
    }

    public void updateLayout() {
        if (mCurrentPageNo == 1)
            mLeftButton.setVisibility(View.INVISIBLE);
        else
            mLeftButton.setVisibility(View.VISIBLE);

        Bundle queryBundle = new Bundle();
        queryBundle.putString("storeSortPref", mSortPref);
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String[]> loader = loaderManager.getLoader(MOVIE_LOADER_ID);


            if (isNetworkAvailable() == true) {
                showMovieDataView();
                mMovieAdapter.setMovieData(null);
                if(loader == null)
                    loaderManager.initLoader(MOVIE_LOADER_ID,queryBundle,new ArrayCallback());
                else
                    loaderManager.restartLoader(MOVIE_LOADER_ID,queryBundle,new ArrayCallback());
                mPageTextview.setText("Page " + mCurrentPageNo + " of " + mTotalPageNo);
            } else {
                if(loader == null)
                {
                    showErrorMessage();
                    return;
                }
                else
                    loaderManager.initLoader(MOVIE_LOADER_ID,queryBundle,new ArrayCallback());

            }




        if (mCurrentPageNo == mTotalPageNo)
            mRightButton.setVisibility(View.INVISIBLE);
    }

    /**
     * This method will make the View for the weather data visible and
     * hide the error message.
     */
    private void showMovieDataView() {
        /* First, to make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, to make sure the weather data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
        mLayout.setVisibility(View.VISIBLE);
    }




    public class ArrayCallback implements LoaderManager.LoaderCallbacks<JSONArray>{
        @Override
        public Loader<JSONArray> onCreateLoader(int id, final Bundle args) {


            return new AsyncTaskLoader<JSONArray>(MainActivity.this
            ) {
                JSONArray mData = null;

                @Override
                protected void onStartLoading() {
                    if (args == null)
                        return;
                    if (mData != null) {
                        deliverResult(mData);
                    } else {
                        mLoadingIndicator.setVisibility(View.VISIBLE);
                        forceLoad();
                    }
                }

                @Override
                public JSONArray loadInBackground() {

                    String sort = args.getString("storeSortPref");
                    URL url = NetworkUtils.buildUrl(sort_by_popularity, Integer.toString(mCurrentPageNo), keyPref);
                    Log.v("url", url.toString());
                    String reply = null;
                    try {
                        reply = NetworkUtils.getResponseFromHttpUrl(url);
                        mTotalPageNo = NetworkUtils.getTotalNumberOfPages(reply);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    JSONArray returnArray = null;
                    try {
                        returnArray = NetworkUtils.resultArray(reply);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return returnArray;

                }

                public void deliverResult(JSONArray data) {
                    mData = data;
                    super.deliverResult(data);
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<JSONArray> loader, JSONArray data) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            mMovieAdapter.setMovieData(data);
            if (null == data) {
                showErrorMessage();

            } else {
                showMovieDataView();
            }
        }

        @Override
        public void onLoaderReset(Loader<JSONArray> loader) {

        }

    }

    public class CursorCallback implements LoaderManager.LoaderCallbacks<Cursor>{

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new AsyncTaskLoader<Cursor>(MainActivity.this) {
                Cursor mFavoritesData = null;

                @Override
                protected void onStartLoading() {
                    if (mFavoritesData != null) {
                        // Delivers any previously loaded data immediately
                        deliverResult(mFavoritesData);
                    } else {
                        // Force a new load
                        forceLoad();
                    }
                }

                @Override
                public Cursor loadInBackground() {
                    try {
                        return getContentResolver().query(FavoritesContract.FavoritesEntry.CONTENT_URI,
                                null,
                                null,
                                null,
                                FavoritesContract.FavoritesEntry._ID);

                    } catch (Exception e) {
                        Log.e("TAG", "Failed to asynchronously load data.");
                        e.printStackTrace();
                        return null;
                    }
                }

                @Override
                public void deliverResult(Cursor data) {
                    mFavoritesData = data;
                    super.deliverResult(data);
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mMovieAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mMovieAdapter.swapCursor(null);
        }
    }

    /**
     * This method will make the error message visible and hide the weather
     * View.
     */
    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
        mLayout.setVisibility(View.INVISIBLE);
    }


    @Override
    public void voidMethod(JSONObject param) {
        Intent intent = new Intent(MainActivity.this, MovieDetailActivity.class);
        String movieTitle = null;
        String description = null;
        String usersRating = null;
        String releaseDate = null;
        String thumbnail = null;
        String poster_path = null;
        String id_movie = null;
        try {
            movieTitle = param.getString("original_title");
            description = param.getString("overview");
            usersRating = param.getString("vote_average");
            releaseDate = param.getString("release_date");
            thumbnail = param.getString("backdrop_path");
            id_movie = param.getString("id");
            poster_path = param.getString("poster_path");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        intent.putExtra("movieTitle", movieTitle);
        intent.putExtra("description", description);
        intent.putExtra("usersRating", usersRating);
        intent.putExtra("releaseDate", releaseDate);
        intent.putExtra("thumbnail", thumbnail);
        intent.putExtra("id_movie",id_movie);
        intent.putExtra("poster_path", poster_path);
        startActivity(intent);
    }

    @Override
    public void cursorClick(Cursor mCursor, int adapterPosition) {

    }

    public void setmLeftButton() {
        mLeftButton = (ImageButton) findViewById(R.id.left_arrow);
        mLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentPageNo = mCurrentPageNo - 1;
                mPageTextview.setText(Integer.toString(mCurrentPageNo));
                updateLayout();
            }
        });
    }

    public void setmRightButton() {
        mRightButton = (ImageButton) findViewById(R.id.right_arrow);
        mRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentPageNo = mCurrentPageNo + 1;
                mPageTextview.setText(Integer.toString(mCurrentPageNo));
                updateLayout();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem action_sort_by_popularity = menu.findItem(R.id.action_sort_by_popularity);
        MenuItem action_sort_by_rating = menu.findItem(R.id.action_sort_by_rating);
        MenuItem action_sort_by_favorites = menu.findItem(R.id.action_sort_by_favorites);
        if (mSortPref.contentEquals(sort_by_popularity)) {
            if (!action_sort_by_popularity.isChecked())
                action_sort_by_popularity.setChecked(true);
        } else if(mSortPref.contentEquals(sort_by_highest_rated)) {
            if (!action_sort_by_rating.isChecked())
                action_sort_by_rating.setChecked(true);
        }

        else {
            if (!action_sort_by_favorites.isChecked())
                action_sort_by_favorites.setChecked(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int idMenuSelected = item.getItemId();
        switch (idMenuSelected) {
            case R.id.action_sort_by_popularity:
                if (item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
                mSortPref = sort_by_popularity;
                updateLayoutSwitches();
                return true;
            case R.id.action_sort_by_rating:
                if (item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
                mSortPref = sort_by_highest_rated;
                updateLayoutSwitches();
                return true;
            case R.id.action_sort_by_favorites:
                if(item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
                mSortPref = sort_by_favorites;
                updateLayoutSwitches();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean isNetworkAvailable() {
        boolean status = false;
        try {
            ConnectivityManager cm =
                    (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            status = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return status;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("storeSortPref", mSortPref);
    }
}
