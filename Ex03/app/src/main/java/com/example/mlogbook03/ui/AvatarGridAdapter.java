package com.example.mlogbook03.ui;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.mlogbook03.R;

public class AvatarGridAdapter extends BaseAdapter {
    private Context context;
    private int[] avatars;

    public AvatarGridAdapter(Context ctx, int[] avatars) {
        this.context = ctx;
        this.avatars = avatars;
    }

    @Override public int getCount() { return avatars.length; }
    @Override public Object getItem(int position) { return avatars[position]; }
    @Override public long getItemId(int position) { return position; }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        ImageView iv;
        if (convertView == null) {
            iv = (ImageView) LayoutInflater.from(context).inflate(R.layout.item_avatar, parent, false);
        } else {
            iv = (ImageView) convertView;
        }
        iv.setImageResource(avatars[position]);
        return iv;
    }
}

