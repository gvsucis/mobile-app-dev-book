package edu.gvsu.cis.traxy;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by dulimarh on 8/14/17.
 */

public class SectionHolder extends RecyclerView.ViewHolder {
    public final TextView headerText, temperature;
    public final ImageView icon;
    public SectionHolder(View itemView) {
        super(itemView);
        headerText = (TextView) itemView.findViewById(R.id.header);
        temperature = (TextView) itemView.findViewById(R.id.temperature);
        icon = (ImageView) itemView.findViewById(R.id.weather_icon);
    }
}
