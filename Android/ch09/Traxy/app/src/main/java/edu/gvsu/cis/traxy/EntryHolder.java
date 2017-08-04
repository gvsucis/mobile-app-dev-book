package edu.gvsu.cis.traxy;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by dulimarh on 8/2/17.
 */

public class EntryHolder extends RecyclerView.ViewHolder {
    private final TextView caption;
    public final ImageView topImage;
    public final ImageView playIcon;
    public final ImageButton editBtn;

    public EntryHolder(View itemView) {
        super(itemView);
        caption = (TextView) itemView.findViewById(R.id.caption);
        topImage = (ImageView) itemView.findViewById(R.id.topImage);
        playIcon = (ImageView) itemView.findViewById(R.id.playback);
        editBtn = (ImageButton) itemView.findViewById(R.id.je_edit);
    }

    public void setCaption(String txt) {
        caption.setText(txt);
    }
}
