package com.example.aya.myapplication;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Rating;
import android.net.Uri;
import android.widget.BaseAdapter;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.view.LayoutInflater;


/**
 * Created by aya on 15-8-6.
 */

public class MovieAdapter extends BaseAdapter {
    private List<Movie> items;
    private Context context;
    private int size;
    private int position = 0;

    public MovieAdapter(List<Movie> items, Context c){
        this.items = items;
        this.context = c;
        this.size = items.size();
    }

    public int getCount(){
        return size;
    }

    public Movie getItem(int position){
        return items.get(position);
    }

    public long getItemId(int position){
        return position;
    }

    public View getView(int position, View contentView, ViewGroup parent){
        Movie movie = items.get(position);
        RelativeLayout movieLayout = (RelativeLayout) LayoutInflater.from(context)
                .inflate(R.layout.list_item, parent, false);

        ImageView imgPoster = (ImageView) movieLayout.findViewById(R.id.imgPoster);
        imgPoster.setImageBitmap(movie.getPicture());

        TextView txtLabel = (TextView) movieLayout.findViewById(R.id.txtTitle);
        txtLabel.setText(movie.getTitle() + "(" + movie.getYear() + ")");

        RatingBar ratingBar = (RatingBar)movieLayout.findViewById(R.id.listRating);
        ratingBar.setRating((float)items.get(position).getRating());

        TextView txtGenre = (TextView)movieLayout.findViewById(R.id.txtViewListGenre);
        txtGenre.setText(movie.getGenre());

        return movieLayout;
    }

}