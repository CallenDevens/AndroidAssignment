package com.example.aya.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by aya on 15-8-15.
 */
public class MovieInfoAdapter extends BaseAdapter {
    private List<lstItem> items;
    private int size;
    private int position = 0;

    public MovieInfoAdapter(List<lstItem> items){
        this.items = items;
        this.size = items.size();
    }
    public long getItemId(int position){
        return position;
    }

    public int getCount(){
        return size;
    }

    public lstItem getItem(int position){
        return items.get(position);
    }

    public View getView(int position, View contentView, ViewGroup parent){
        lstItem item = items.get(position);

        LinearLayout infoLayout = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.info_item, parent, false);

        TextView txtViewKey = (TextView) infoLayout.findViewById(R.id.txtKey);
        txtViewKey.setText(item.getTitle());

        TextView txtViewValue = (TextView) infoLayout.findViewById(R.id.txtValue);
        txtViewValue.setText(item.getText());

        return infoLayout;
    }
}
