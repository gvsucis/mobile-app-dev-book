package edu.gvsu.cis.traxy;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by dulimarh on 8/17/17.
 */

public class PhotoHolder extends RecyclerView.ViewHolder {
    public final ImageView photo;
    public final ImageView selected;

    public PhotoHolder(View itemView) {
        super(itemView);
        photo = (ImageView) itemView.findViewById(R.id.photo);
        selected = (ImageView) itemView.findViewById(R.id.selected);
    }
}
