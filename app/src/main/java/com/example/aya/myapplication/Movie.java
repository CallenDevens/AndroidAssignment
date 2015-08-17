package com.example.aya.myapplication;

import android.widget.ImageView;
import android.graphics.drawable.*;
import android.os.Parcelable;
import android.os.Parcel;

/**
 * Created by aya on 15-8-6.
 */
public class Movie{
    private int id;
    private String title;
    private int year;
    private String shortPlot;
    private String fullPlot;
    private String genre;
    private Drawable poster;
    private double rating;
    private String actors;
    private String writer;
    private String director;

    private Party party = null;

    public Movie(int id, String title, Drawable pic, int year,double rating){
        this.title = title;
        this.id = id;
        this.poster = pic;
        this.year = year;
        this.rating = rating;
        this.party = null;
    }

    public Movie(int id, String title, String genre, Drawable pic,int year,
                 double rating, String sPlot, String fPlot,
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
        this.party = null;
    }

    public int getId() {
        return id;
    }

    public  Drawable getPicture(){
        return poster;
    }

    public double getRating(){
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

    public void setParty(Party p){
        this.party = p;
    }

    public Party getParty(){
        return party;
    }
}
