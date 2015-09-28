package com.example.aya.myapplication;

import android.graphics.Bitmap;
import android.widget.ImageView;
import android.graphics.drawable.*;
import android.os.Parcelable;
import android.os.Parcel;

/**
 * Created by aya on 15-8-6.
 */
public class Movie{
    private String id;
    private String title;
    private int year;
    private String shortPlot;
    private String fullPlot;
    private String genre;
    private Bitmap poster;
    private float rating;
    private String actors;
    private String writer;
    private String director;

    public Movie(String id, String title, String genre, Bitmap pic, int year,float rating){
        this.title = title;
        this.genre = genre;
        this.id = id;
        this.poster = pic;
        this.year = year;
        this.rating = rating;
    }

    public Movie(String id, String title, String genre, Bitmap pic,int year,
                 float rating, String sPlot, String fPlot,
                 String actors, String writer,String direcor){
        this.genre = genre;
        this.year = year;
        this.id = id;
        this.title = title;
        this.poster = pic;
        this.shortPlot = sPlot;
        this.fullPlot = fPlot;
        this.rating = rating;
        this.director = direcor;
        this.actors = actors;
        this.writer = writer;
    }

    public String getId() {
        return id;
    }

    public  Bitmap getPicture(){
        return poster;
    }

    public float getRating(){
        return this.rating;
    }

    public int getYear(){return this.year;}

    public String getTitle(){
        return title;
    }

    public String getGenre(){return genre;}
    public String getActors(){return actors;}
    public String getDirector(){return director;}
    public String getWriter(){return writer;}

    public String getShortPlot(){return shortPlot;}
    public String getFullPlot(){return fullPlot;}
    public void setRating(float r){
        this.rating = r;
    }
}
