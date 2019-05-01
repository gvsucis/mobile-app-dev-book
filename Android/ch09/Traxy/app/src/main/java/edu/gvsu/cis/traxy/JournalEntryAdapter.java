package edu.gvsu.cis.traxy;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import edu.gvsu.cis.traxy.model.JournalEntry;

public class JournalEntryAdapter extends FirebaseRecyclerAdapter<JournalEntry, EntryHolder> {

    private JournalMediaActions actions;

    public JournalEntryAdapter(FirebaseRecyclerOptions<JournalEntry> options, JournalMediaActions actions) {
        super(options);
        this.actions = actions;
    }

    @NonNull
    @Override
    public EntryHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.journal_entry_item, viewGroup, false);
        return new EntryHolder(v);
    }

    @Override
    protected void onBindViewHolder(@NonNull EntryHolder viewHolder, int position, @NonNull JournalEntry model) {
        viewHolder.setCaption(model.getCaption());
        viewHolder.setDate(model.getDate());

        switch (model.getType()) {
            case 2: // photo
                viewHolder.topImage.setVisibility(View.VISIBLE);
                viewHolder.playIcon.setVisibility(View.GONE);
                Glide.with(viewHolder.topImage.getContext())
                        .load(Uri.parse(model.getUrl()))
                        .into(viewHolder.topImage);
                break;
            case 4: // video
                viewHolder.topImage.setVisibility(View.VISIBLE);
                viewHolder.playIcon.setVisibility(View.VISIBLE);
                Glide.with(viewHolder.topImage.getContext())
                        .load(Uri.parse(model.getThumbnailUrl()))
                        .into(viewHolder.topImage);
                break;
            default:
                viewHolder.topImage.setVisibility(View.GONE);
                break;
        }
        viewHolder.editBtn.setOnClickListener( view -> {
            String key = getRef(position).getKey();
            this.actions.editAction(model, key);
        });
        viewHolder.topImage.setOnClickListener( view -> {
            this.actions.viewAction(model);
        });
    }

}
