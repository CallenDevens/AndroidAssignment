package com.example.aya.myapplication;

/**
 * Created by aya on 15-8-15.
 */
import java.util.*;
public class Party {
    private Date partyDate;
    private String partyVenue;
    private float longitude;
    private float latitude;

    public Party(){}
    public Party(Date pd, String pv, float lo, float la){
        this.partyDate = pd;
        this.partyVenue = pv;
        this.longitude = lo;
        this.latitude = la;
    }

    public Date getDate(){
        return partyDate;
    }

    public String getVenue(){
        return partyVenue;
    }
    public float getLongitude(){
        return longitude;
    }

    public float getLatitude(){
        return  latitude;
    }
}
