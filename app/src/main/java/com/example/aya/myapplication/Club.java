package com.example.aya.myapplication;

/**
 * Created by aya on 15-10-13.
 */
public class Club {
    private String club_address;
    private String club_name;
    private double club_latitude;
    private double club_longitude;
    private int club_range = Integer.MAX_VALUE;
    private boolean club_joined;

    public String getClubAddress(){
        return club_address;
    }
    public String getClubName(){
        return club_name;
    }
    public double getClubLatitude(){
        return club_latitude;
    }
    public double getClubLongitude(){
        return club_longitude;
    }
    public  int getClubRange(){
        return club_range;
    }

    public boolean isClubJoined(){
        return club_joined;
    }

    public Club(String address, String name, double club_latitude, double club_longitude, int club_range,boolean club_joined)
    {
        this.club_address = address;
        this.club_name = name;
        this.club_latitude = club_latitude;
        this.club_longitude = club_longitude;
        this.club_range = club_range;
        this.club_joined = club_joined;
    }
}
