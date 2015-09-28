package com.example.aya.myapplication;

/**
 * Created by aya on 15-8-15.
 */
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;
public class Party {
    //private Date partyDate;

    private Calendar partyDatetime;
    private String   partyVenue;
    private ArrayList<Contact> contacts;
    private double    longitude;
    private double    latitude;

    public Party(){}
    public Party(Calendar partyDatetime, String venue, double longitude, double latitude){
        this.partyDatetime = partyDatetime;
        this.partyVenue = venue;
        this.contacts = new ArrayList<Contact>();
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getLongitude(){
        return longitude;
    }

    public double getLatitude(){
        return  latitude;
    }
    public Calendar getPartyDateTime(){
        return partyDatetime;
    }

    public String getVenue(){
        return partyVenue;
    }
    public ArrayList getContacts(){
        return this.contacts;
    }

    public void addContact(Contact contact){
        this.contacts.add(contact);
    }

    public String getPrintableDate(){
        SimpleDateFormat month_date = new SimpleDateFormat("MMM",Locale.ENGLISH);
        String month_name = month_date.format(partyDatetime.getTime());
        String datetime = month_name + " "+partyDatetime.get(Calendar.DAY_OF_MONTH)+" " + partyDatetime.get(Calendar.YEAR)+ ","+
                partyDatetime.get(Calendar.HOUR_OF_DAY) + ":" + partyDatetime.get(Calendar.MINUTE);
        return datetime;
    }

    public String getPrintableContancts(){
        String contactList = "";
        for(Contact c :contacts){
            if(!contactList.equals("")){
                contactList += " ,"+c.getName();
            }
            else{
                contactList+= c.getName();
            }
        }
        return contactList;
    }

    public  String getMonthForInt(int num) {
        String month = "wrong";
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        if (num >= 0 && num <= 11) {
            month = months[num];
        }
        return month;
    }
}
