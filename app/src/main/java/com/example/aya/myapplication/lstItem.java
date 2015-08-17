package com.example.aya.myapplication;

/**
 * Created by aya on 15-8-14.
 */
public class lstItem {
    private String title;
    private String text;

    public lstItem(String title, String text){
        this.title = title;
        this.text = text;
    }

    public String getTitle(){
        return title;
    }

    public String getText(){
        return text;
    }

    public void setTitle(String t){
        this.title = t;
    }

    public void setText(String text){
        this.text = text;
    }
}