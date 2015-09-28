package com.example.aya.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by aya on 15-9-22.
 */
public class PartyAdapter extends BaseAdapter{
    private List<Party> parties;
    private Context context;
    private int size;
    private int position = 0;

    public PartyAdapter(List<Party> items, Context c){
        this.parties = items;
        this.context = c;
        this.size = items.size();
    }

    public int getCount(){
        return size;
    }

    public Party getItem(int position){
        return parties.get(position);
    }

    public long getItemId(int position){
        return position;
    }

    public View getView(int position, View contentView, ViewGroup parent){
        Party party = parties.get(position);
        LinearLayout partyLayout = (LinearLayout) LayoutInflater.from(context)
                .inflate(R.layout.party_list_item, parent, false);

        TextView venueTextView = (TextView)partyLayout.findViewById(R.id.party_list_venue);
        venueTextView.setText(party.getVenue());

        TextView dateTextView = (TextView)partyLayout.findViewById(R.id.party_list_datetime);
        dateTextView.setText(party.getPrintableDate());

        TextView contactsTextView=(TextView)partyLayout.findViewById(R.id.party_list_contacts);
        contactsTextView.setText(party.getPrintableContancts());
        return partyLayout;
    }
}
