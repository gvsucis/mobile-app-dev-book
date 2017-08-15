package edu.gvsu.cis.traxy;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by dulimarh on 8/14/17.
 */

public class SectionHolder extends RecyclerView.ViewHolder {
    public final TextView textView;
    public SectionHolder(View itemView) {
        super(itemView);
        textView = (TextView) itemView.findViewById(R.id.text1);
    }
}
