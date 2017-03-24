package cis.gvsu.edu.traxy;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.truizlop.sectionedrecyclerview.HeaderViewHolder;
import com.truizlop.sectionedrecyclerview.SectionedRecyclerViewAdapter;

import cis.gvsu.edu.traxy.JournalFragment.OnListFragmentInteractionListener;
import cis.gvsu.edu.traxy.dummy.DummyContent.DummyJournal;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyJournal} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class JournalAdapter extends SectionedRecyclerViewAdapter<JournalAdapter.HeaderViewHolder,
            JournalAdapter.ViewHolder, JournalAdapter.FooterViewHolder> {

    private final OnListFragmentInteractionListener mListener;

    private final List<DummyJournal> current, future, past;

    public JournalAdapter(List<DummyJournal> items, OnListFragmentInteractionListener listener) {
        mListener = listener;
        this.current = new ArrayList<DummyJournal>();
        this.future = new ArrayList<DummyJournal>();
        this.past = new ArrayList<DummyJournal>();

        for (DummyJournal jrn : items) {
            if (jrn.startDate.isAfterNow())
                future.add(jrn);
            else if (jrn.endDate.isBeforeNow())
                past.add(jrn);
            else
                current.add(jrn);
        }
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
        DummyJournal item;
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
        holder.mIdView.setText(item.name);
        holder.mContentView.setText(item.location);

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
        public DummyJournal mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
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
