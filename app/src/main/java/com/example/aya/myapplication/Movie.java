package com.example.aya.myapplication;

import android.widget.ImageView;
import android.graphics.drawable.*;
/**
 * Created by aya on 15-8-6.
 */
public class Movie {
    private int id;
    private String title;
    private int year;
    private String shortPlot;
    private String fullPlot;
    private Drawable poster;
    private double rating;

    public Movie(int id, String title, Drawable pic, int year,double rating){
        this.title = title;
        this.id = id;
        this.poster = pic;
        this.year = year;
        this.rating = rating;
    }

    public Movie(int id, String title, Drawable pic, double rating,
                 String sPlot, String fPlot){
        this.id = id;
        this.title = title;
        this.poster = pic;
        this.shortPlot = sPlot;
        this.fullPlot = fPlot;
        this.rating = rating;
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
}
