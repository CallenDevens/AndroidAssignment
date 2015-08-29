package com.example.aya.myapplication;

/**
 * Created by aya on 15-8-28.
 */
public class Contact {
    private String name;
    private String phoneNumber;

    public Contact(String n, String p){
        this.name = n;
        this.phoneNumber = p;
    }

    public String getName(){
        return name;
    }

    public String getPhoneNumber(){
        return this.phoneNumber;
    }
}
