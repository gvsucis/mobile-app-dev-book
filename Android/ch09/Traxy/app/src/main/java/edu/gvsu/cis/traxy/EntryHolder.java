package edu.gvsu.cis.traxy;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by dulimarh on 8/2/17.
 */

public class EntryHolder extends RecyclerView.ViewHolder {
    private final TextView caption;
    public final ImageView topImage;

    public EntryHolder(View itemView) {
        super(itemView);
        caption = (TextView) itemView.findViewById(R.id.caption);
        topImage = (ImageView) itemView.findViewById(R.id.topImage);
    }

    public void setCaption(String txt) {
        caption.setText(txt);
    }
}
