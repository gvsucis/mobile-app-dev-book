package edu.gvsu.cis.traxy;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created by dulimarh on 8/2/17.
 */

public class EntryHolder extends RecyclerView.ViewHolder {
    private final TextView caption, date;
    public final ImageView topImage;
    public final ImageView playIcon;
    public final ImageButton editBtn;

    private final static DateTimeFormatter fmt;

    static {
        fmt = DateTimeFormat.forPattern("yyyy-MM-dd, HH:mm");
    }

    public EntryHolder(View itemView) {
        super(itemView);
        caption = (TextView) itemView.findViewById(R.id.caption);
        date = (TextView) itemView.findViewById(R.id.date_time);
        topImage = (ImageView) itemView.findViewById(R.id.topImage);
        playIcon = (ImageView) itemView.findViewById(R.id.playback);
        editBtn = (ImageButton) itemView.findViewById(R.id.je_edit);
    }

    public void setCaption(String txt) {
        caption.setText(txt);
    }

    public void setDate(String isoDate) {
        date.setText(fmt.print(DateTime.parse(isoDate)));
    }
}
