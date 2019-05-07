package edu.gvsu.cis.traxy;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
        StorageReference mediaRef;
        viewHolder.setCaption(model.getCaption());
        viewHolder.setDate(model.getDate());

        switch (model.getType()) {
            case 1:
                viewHolder.mediaContainer.setVisibility(View.GONE);
                break;
            case 2: // photo
                viewHolder.topImage.setVisibility(View.VISIBLE);
                viewHolder.playIcon.setVisibility(View.GONE);
                mediaRef = FirebaseStorage.getInstance().getReferenceFromUrl(model.getUrl());
                GlideApp.with(viewHolder.topImage.getContext())
                        .load(mediaRef)
                        .into(viewHolder.topImage);
                break;
            case 3: // audio
                viewHolder.topImage.setVisibility(View.VISIBLE);
                viewHolder.playIcon.setVisibility(View.VISIBLE);
                viewHolder.topImage.setImageResource(0);
                break;
            case 4: // video
                viewHolder.topImage.setVisibility(View.VISIBLE);
                viewHolder.playIcon.setVisibility(View.VISIBLE);
                mediaRef = FirebaseStorage.getInstance().getReferenceFromUrl(model.getThumbnailUrl());
                GlideApp.with(viewHolder.topImage.getContext())
                        .load(mediaRef)
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
        viewHolder.playIcon.setOnClickListener(view -> {
            this.actions.viewAction(model);
        });
    }

}
