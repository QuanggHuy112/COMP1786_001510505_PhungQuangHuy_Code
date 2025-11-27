package com.example.mhike.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.mhike.R;
import com.example.mhike.models.Observation;

import java.util.List;

public class ObservationAdapter extends BaseAdapter {

    private Context ctx;
    private List<Observation> obsList;
    private LayoutInflater layoutInflater;

    public ObservationAdapter(Context ctx, List<Observation> obsList) {
        this.ctx = ctx;
        this.obsList = obsList;
        this.layoutInflater = LayoutInflater.from(ctx);
    }

    @Override
    public int getCount() {
        return obsList.size();
    }

    @Override
    public Object getItem(int position) {
        return obsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return obsList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewData viewData;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_observation, parent, false);
            viewData = new ViewData();
            viewData.obsText = convertView.findViewById(R.id.obsText);
            viewData.timeText = convertView.findViewById(R.id.obsTimeText);
            convertView.setTag(viewData);
        } else {
            viewData = (ViewData) convertView.getTag();
        }

        Observation obs = obsList.get(position);
        viewData.obsText.setText(obs.getObservationText());
        viewData.timeText.setText(obs.getTime());

        return convertView;
    }

    static class ViewData {
        TextView obsText;
        TextView timeText;
    }
}
