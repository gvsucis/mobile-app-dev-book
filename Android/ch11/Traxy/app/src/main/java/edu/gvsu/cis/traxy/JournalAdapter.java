package edu.gvsu.cis.traxy;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.truizlop.sectionedrecyclerview.SectionedRecyclerViewAdapter;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import edu.gvsu.cis.traxy.JournalFragment.OnListFragmentInteractionListener;
import edu.gvsu.cis.traxy.model.Trip;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Trip} and makes a
 * call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class JournalAdapter extends SectionedRecyclerViewAdapter<JournalAdapter.HeaderViewHolder,
            JournalAdapter.ViewHolder, JournalAdapter.FooterViewHolder> {

    private final OnListFragmentInteractionListener mListener;

    private final List<Trip> current, future, past;

    public JournalAdapter(List<Trip> items, OnListFragmentInteractionListener listener) {
        mListener = listener;
        this.current = new ArrayList<Trip>();
        this.future = new ArrayList<Trip>();
        this.past = new ArrayList<Trip>();
        reloadFrom(items);
    }

    public void reloadFrom(final List<Trip> data) {
        current.clear();
        past.clear();
        future.clear();
        for (Trip t : data) {
            DateTime begDate = DateTime.parse(t.getStartDate());
            DateTime endDate = DateTime.parse(t.getEndDate());
            if (begDate.isAfterNow()) {
                future.add(t);
            } else if (endDate.isBeforeNow()) {
                past.add(t);
            } else {
                current.add(t);
            }
        }
        notifyDataSetChanged();
    }

//    @Override
//    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.fragment_journal, parent, false);
//        return new ViewHolder(view);
//    }

    @Override
    protected int getSectionCount() {
        return 3;
    }

    @Override
    protected int getItemCountForSection(int section) {
        switch(section) {
            case 0:
                return current.size();
            case 1:
                return future.size();
            default:
                return past.size();
        }
    }

    @Override
    protected boolean hasFooterInSection(int section) {
        return false;
    }

    @Override
    protected HeaderViewHolder onCreateSectionHeaderViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_section_header, parent, false);
        return new HeaderViewHolder(view);
    }

    @Override
    protected FooterViewHolder onCreateSectionFooterViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    protected ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_journal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    protected void onBindSectionHeaderViewHolder(HeaderViewHolder holder, int section) {
        switch(section) {
            case 0:
                holder.header.setText("Current");
                break;
            case 1:
                holder.header.setText("Future");
                break;
            default:
                holder.header.setText("Past");
        }
    }

    @Override
    protected void onBindSectionFooterViewHolder(FooterViewHolder holder, int section) {

    }

    @Override
    protected void onBindItemViewHolder(ViewHolder holder, int section, int position) {
        Trip item;
        switch(section) {
            case 0:
                item = this.current.get(position);
                break;
            case 1:
                item = this.future.get(position);
                break;
            default:
                item = this.past.get(position);
        }
        holder.mItem = item;
        holder.mIdView.setText(item.getName());
        holder.mContentView.setText(item.getLocation());

        String photoUrl = item.getCoverPhotoUrl();
        if (photoUrl != null && photoUrl.startsWith("http")) {
            Glide.with(holder.mBackImage.getContext())
                    .load(photoUrl)
                    .placeholder(R.drawable.traxy_landscape)
                    .centerCrop()
                    .into(holder.mBackImage);
        }
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

//    @Override
//    public void onBindViewHolder(final ViewHolder holder, int position) {
//        holder.mItem = mValues.get(position);
//        holder.mIdView.setText(mValues.get(position).name);
//        holder.mContentView.setText(mValues.get(position).location);
//
//        holder.mView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (null != mListener) {
//                    // Notify the active callbacks interface (the activity, if the
//                    // fragment is attached to one) that an item has been selected.
//                    mListener.onListFragmentInteraction(holder.mItem);
//                }
//            }
//        });
//    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public final ImageView mBackImage;
        public Trip mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mBackImage = (ImageView) view.findViewById(R.id.imageView2);
            mIdView = (TextView) view.findViewById(R.id.name);
            mContentView = (TextView) view.findViewById(R.id.location);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView header;
        public HeaderViewHolder(View view) {
            super(view);
            header = (TextView) view.findViewById(R.id.header);
        }
    }


    public class FooterViewHolder extends RecyclerView.ViewHolder {
        public FooterViewHolder(View view) {
            super(view);
        }
    }

}
