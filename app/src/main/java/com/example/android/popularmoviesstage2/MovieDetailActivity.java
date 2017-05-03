package com.example.android.popularmoviesstage2;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmoviesstage2.adapters.ReviewAdapter;
import com.example.android.popularmoviesstage2.adapters.TrailerAdapter;
import com.example.android.popularmoviesstage2.data.FavoritesContract;
import com.example.android.popularmoviesstage2.models.Review;
import com.example.android.popularmoviesstage2.models.Trailer;
import com.example.android.popularmoviesstage2.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

public class MovieDetailActivity extends AppCompatActivity implements TrailerAdapter.TrailerAdapterOnclickHandler, ReviewAdapter.ReviewAdapterOnclickHandler{
    private TextView mTitleTextView;
    private TextView mDescriptionTextView;
    private TextView mUsersRatingTextView;
    private TextView mRealeaseDateTextView;
    private ImageView mThumbnailImageView;
    private String basePicasso = "http://image.tmdb.org/t/p/w185/";
    private String id_movie;
    private RecyclerView mRecyclerViewTrailer;
    private RecyclerView mRecyclerViewReview;
    private TrailerAdapter mTrailerAdapter;
    private ReviewAdapter mReviewAdapter;
    private String keyPref = null;
    private Trailer mFirstTrailer = null;
    private String movieTitle;
    private ContentValues contentValues = new ContentValues();
    private boolean checkMarked =  false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTitleTextView = (TextView) findViewById(R.id.detail_tv);
        mDescriptionTextView = (TextView) findViewById(R.id.description_tv);
        mUsersRatingTextView = (TextView) findViewById(R.id.users_rating_tv);
        mRealeaseDateTextView = (TextView) findViewById(R.id.release_date_tv);
        mThumbnailImageView = (ImageView) findViewById(R.id.detail_iv);
        mRecyclerViewTrailer = (RecyclerView) findViewById(R.id.rv_layout_trailers);
        mRecyclerViewReview = (RecyclerView) findViewById(R.id.rv_reviews);
        mRecyclerViewTrailer.setLayoutManager(new LinearLayoutManager(this,1,false));
        mRecyclerViewReview.setLayoutManager(new LinearLayoutManager(this,1,false));
        mTrailerAdapter = new TrailerAdapter(this);
        mReviewAdapter = new ReviewAdapter(this);
        keyPref = getString((R.string.tmdb_api_key));
        mRecyclerViewTrailer.setAdapter(mTrailerAdapter);
        mRecyclerViewReview.setAdapter(mReviewAdapter);


        Intent intent = getIntent();
        movieTitle = intent.getStringExtra("movieTitle");
        String movieDescription = intent.getStringExtra("description");
        String usersRating = intent.getStringExtra("usersRating");
        String releaseDate = intent.getStringExtra("releaseDate");
        String thumbnail = intent.getStringExtra("thumbnail");
        String posterPath = intent.getStringExtra("poster_path");
        id_movie = intent.getStringExtra("id_movie");
        setTitle(movieTitle);

        contentValues.put(FavoritesContract.FavoritesEntry.COLUMN_TITLE, movieTitle);
        contentValues.put(FavoritesContract.FavoritesEntry.COLUMN_ID_MOVIE, id_movie);
        contentValues.put(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH, posterPath);
        contentValues.put(FavoritesContract.FavoritesEntry.COLUMN_DESCRIPTION, movieDescription);
        contentValues.put(FavoritesContract.FavoritesEntry.COLUMN_RATING, usersRating);
        contentValues.put(FavoritesContract.FavoritesEntry.COLUMN_RELEASE_DATE,releaseDate);
        contentValues.put(FavoritesContract.FavoritesEntry.COLUMN_THUMBNAIL, thumbnail);


        if(savedInstanceState != null ) {
            if(savedInstanceState.containsKey("checkClicked"))
            checkMarked = savedInstanceState.getBoolean("checkClicked");
        }
        else
            checkMarked = false;
        Uri uri = Uri.parse(basePicasso + thumbnail);
        Picasso.with(this).load(uri).into(mThumbnailImageView);

        mTitleTextView.setText(movieTitle);
        mDescriptionTextView.setText(movieDescription);
        mUsersRatingTextView.setText(usersRating);
        mRealeaseDateTextView.setText(releaseDate );

         TrailerTask trailerTask = new TrailerTask();
         trailerTask.execute(id_movie);

        ReviewTask reviewTask = new ReviewTask();
        reviewTask.execute(id_movie);
    }

    @Override
    public void clickTrailer(Uri uri) {
        Intent intent1 = new Intent(Intent.ACTION_VIEW,uri );
        startActivity(intent1);
    }

    @Override
    public void clickReview(String uriString, String string) {
        Intent intent  = new Intent(this, FullReview.class);
        intent.putExtra("link_review", uriString);
        intent.putExtra("text_review", string);
        startActivity(intent);
    }


    class TrailerTask extends AsyncTask<String, Void, List<Trailer>>{

        @Override
        protected List<Trailer> doInBackground(String... strings) {

            if (strings.length <  1 || strings[0]== null) {
                return null;
            }


            List<Trailer> trailers = null;
            try {
              trailers   =  NetworkUtils.formatTrailerJson(NetworkUtils.getResponseFromHttpUrl(NetworkUtils.buildTrailerUrl(strings[0],keyPref)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (trailers== null)
                return null;
            else
                return trailers;
        }

        @Override
        protected void onPostExecute(List<Trailer> trailers) {
            if (trailers != null && !trailers.isEmpty())
            {
                mTrailerAdapter.setMovieData(trailers);
                mFirstTrailer = trailers.get(0);
            }
        }
    }
    class ReviewTask extends AsyncTask<String, Void, List<Review>>{

        @Override
        protected List<Review> doInBackground(String... strings) {

            if (strings.length <  1 || strings[0]== null) {
                return null;
            }


            List<Review> reviews = null;
            try {
                reviews   =  NetworkUtils.formatReviewJson(NetworkUtils.getResponseFromHttpUrl(NetworkUtils.buildReviewsUrl(strings[0],"0",keyPref)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (reviews== null)
                return null;
            else
                return reviews;
        }

        @Override
        protected void onPostExecute(List<Review> reviews) {
            if (reviews != null && !reviews.isEmpty())
            {
                mReviewAdapter.setMovieData(reviews);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        if(checkMarked = true){
            menu.findItem(R.id.action_favorites).setIcon(R.drawable.ic_star_rate_black_18dp);
        }
        else
            menu.findItem(R.id.action_favorites).setIcon(R.drawable.ic_star_rate_white_18dp);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int idSelected = item.getItemId();
        switch (idSelected){
            case R.id.action_share:
                String firstTrailerUrlPart = mFirstTrailer.getmVideoUrlPart();
                String fullStringUrl = NetworkUtils.buildYoutubeUrl(firstTrailerUrlPart).toString();
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT,"Check out this awesome movie "+ movieTitle + ", and view the trailer " + fullStringUrl);
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent,getResources().getText(R.string.share_to)));
                break;
            case R.id.action_favorites:
                if(checkMarked == true){
                    item.setIcon(R.drawable.ic_star_rate_white_18dp);
                    checkMarked = false;
                }

                else{
                    item.setIcon(R.drawable.ic_star_rate_black_18dp);
                    checkMarked = true;
                    Uri uri =  getContentResolver().insert(FavoritesContract.FavoritesEntry.CONTENT_URI,contentValues);
                    if(uri != null)
                        Toast.makeText(this, uri.toString(),Toast.LENGTH_SHORT).show();
                }

                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("checkClicked", checkMarked);
        super.onSaveInstanceState(outState);
    }
}
