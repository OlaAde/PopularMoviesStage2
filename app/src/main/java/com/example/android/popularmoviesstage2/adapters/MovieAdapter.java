package com.example.android.popularmoviesstage2.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmoviesstage2.R;
import com.example.android.popularmoviesstage2.data.FavoritesContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

/**
 * Created by Adeogo on 4/25/2017.
 */

public class MovieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_CURSOR = 100;
    private static final int TYPE_JSONARRAY = 101;
    Context context;
    private Cursor mCursor = null;

    private JSONArray movieArray;
    private final MovieAdapterOnclickHandler mClickHandler;
    private Random mRandom = new Random();


    public interface MovieAdapterOnclickHandler{
        void voidMethod(JSONObject param);
         void cursorClick(Cursor mCursor, int adapterPosition);
    }

    public MovieAdapter( MovieAdapterOnclickHandler movieAdapterOnclickHandler){
        mClickHandler = movieAdapterOnclickHandler;
    }


    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public final TextView mMovieTextView;
        public final ImageView mMovieImageView;

        public MovieAdapterViewHolder(View itemView) {
            super(itemView);
            mMovieTextView = (TextView) itemView.findViewById(R.id.tv_item);
            mMovieImageView = (ImageView) itemView.findViewById(R.id.iv_item);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            JSONObject jsonObject = null;
            int adapterPosition = getAdapterPosition();
            try {
                jsonObject = movieArray.getJSONObject(adapterPosition);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mClickHandler.voidMethod(jsonObject);
        }
    }

    public class FavoritesAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public final TextView mMovieTextView;
        public final ImageView mMovieImageView;

        public FavoritesAdapterViewHolder(View itemView) {
            super(itemView);
            mMovieTextView = (TextView) itemView.findViewById(R.id.tv_item);
            mMovieImageView = (ImageView) itemView.findViewById(R.id.iv_item);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            mClickHandler.cursorClick(mCursor, adapterPosition);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_JSONARRAY) {
            View view  = layoutInflater.inflate(R.layout.list_item_layout, parent, false);

            return new MovieAdapterViewHolder(view);
        } else {
            View view =  layoutInflater.inflate(R.layout.list_item_layout, parent, false);

            return new FavoritesAdapterViewHolder(view);
        }
    }




    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        String movieTitle = null;
        String moviePosterStringUrl = null;
        String basePicasso = "http://image.tmdb.org/t/p/w185/";







        if (holder instanceof MovieAdapterViewHolder) {
            try {
                JSONObject newJSObject = movieArray.getJSONObject(position);
                movieTitle = newJSObject.getString("original_title");
                moviePosterStringUrl = newJSObject.getString("poster_path");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if(mCursor != null){
            // Indices for the _id, description, and priority columns
            int movieTitleIndex = mCursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_TITLE);
            int posterPathIndex = mCursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_POSTER_PATH);

            mCursor.moveToPosition(position); // get to the right location in the cursor

            // Determine the values of the wanted data
             movieTitle = mCursor.getString(movieTitleIndex);
             moviePosterStringUrl = mCursor.getString(posterPathIndex);
        }


        Uri uri = Uri.parse(basePicasso + moviePosterStringUrl);
        ((MovieAdapterViewHolder) holder).mMovieTextView.setText(movieTitle);
        Picasso.with(context).load(uri).into(((MovieAdapterViewHolder) holder).mMovieImageView);
        ((MovieAdapterViewHolder) holder).mMovieImageView.getLayoutParams().height = getRandomIntInRange(400, 250);
    }


    @Override
    public int getItemCount() {
        if(null == movieArray){
            return 0;
        }
        return movieArray.length();
    }

    // Custom method to get a random number between a range
    protected int getRandomIntInRange(int max, int min){
        return mRandom.nextInt((max-min)+min)+min;
    }


    public void setMovieData(JSONArray mMovieArray){
        movieArray = mMovieArray;
        notifyDataSetChanged();
    }

    public Cursor swapCursor(Cursor c) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == c) {
            return null; // bc nothing has changed
        }
        Cursor temp = mCursor;
        this.mCursor = c; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (c != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }

}
