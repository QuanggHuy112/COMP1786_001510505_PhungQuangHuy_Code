package com.example.mhike.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.mhike.R;
import com.example.mhike.models.Hike;

import java.util.List;

public class HikeAdapter extends BaseAdapter {

    private Context ctx;
    private List<Hike> hikesList;
    private LayoutInflater layoutInflater;

    public HikeAdapter(Context ctx, List<Hike> hikesList) {
        this.ctx = ctx;
        this.hikesList = hikesList;
        this.layoutInflater = LayoutInflater.from(ctx);
    }

    @Override
    public int getCount() {
        return hikesList.size();
    }

    @Override
    public Object getItem(int position) {
        return hikesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return hikesList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewData viewData;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_hike, parent, false);
            viewData = new ViewData();
            viewData.nameText = convertView.findViewById(R.id.hikeNameText);
            viewData.locationText = convertView.findViewById(R.id.hikeLocationText);
            viewData.dateText = convertView.findViewById(R.id.hikeDateText);
            viewData.difficultyText = convertView.findViewById(R.id.hikeDifficultyText);
            convertView.setTag(viewData);
        } else {
            viewData = (ViewData) convertView.getTag();
        }

        Hike hike = hikesList.get(position);
        viewData.nameText.setText(hike.getName());
        viewData.locationText.setText(hike.getLocation());
        viewData.dateText.setText(hike.getDate());
        viewData.difficultyText.setText(hike.getDifficulty());

        return convertView;
    }

    static class ViewData {
        TextView nameText;
        TextView locationText;
        TextView dateText;
        TextView difficultyText;
    }
}
