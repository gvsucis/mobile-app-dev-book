package edu.gvsu.cis.traxy;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.lang.reflect.Constructor;

/**
 * This subclass of FirebaseRecyclerAdapter adds a header view to a
 * group of like items.
 *
 * @param <T> the Java class that maps to the type of objects/items in the
 *           Firebase database
 * @param <VH> the {@link RecyclerView.ViewHolder} class that contains
 *            the views in the layout of each object
 * @param <HVH> the (@link RecyclerView.ViewHolder} class that contains
 *             the views in the layout of each header
 *
 * @author Hans Dulimarta
 * @version 2017-08-13
 */

public abstract class SectionedFirebaseRecyclerAdapter<T,VH extends
        RecyclerView.ViewHolder, HVH extends RecyclerView.ViewHolder>
        extends FirebaseRecyclerAdapter<T,RecyclerView.ViewHolder> {
    private final Class<VH> itemClass;
    private final Class<HVH> headerClass;
    private int[] rowCount;
    private int totalRows;
    private int itemLayout;
    private int headerLayout;

    /**
     *
     * @param itemLayout  XML layout of each list item
     * @param itemHolderClass  ViewHolder subclass of the list item
     * @param headerLayout  XML layout of the section headers
     * @param headerHolderClass  ViewHolder subclass of the section headers
     * @param options reference to the FirebaseRecyclerOptions
     */
    SectionedFirebaseRecyclerAdapter(@LayoutRes int itemLayout,
                                     Class<VH> itemHolderClass,
                                     @LayoutRes int headerLayout,
                                     Class<HVH> headerHolderClass,
                                     FirebaseRecyclerOptions<T> options) {
        super(options);
        this.itemLayout = itemLayout;
        this.headerLayout = headerLayout;
        this.itemClass = itemHolderClass;
        this.headerClass = headerHolderClass;
    }

    @Override
    public void stopListening() {
        super.stopListening();
        totalRows = 0;
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        int numSections = getSectionCount();
        rowCount = new int[numSections];
        totalRows = numSections;
        for (int k = 0; k < numSections; k++) {
            /* number of rows including the header */
            int thisRow = getItemCountForSection(k);
            rowCount[k] = thisRow + 1;
            totalRows += thisRow;
        }
    }

    /* Since getItemCount() now includes the header views, we must
     * change the behavior of getItem() to exclude these header views */
    @Override
    public int getItemCount() {
        return totalRows;
    }

    /**
     *
     * @param listPos the list position (which is different from the data
     *            position)
     * @return
     */
    @Override
    public T getItem(int listPos) {
        if (isHeader(listPos))
            return null;
        else {
            int section = sectionOfPosition(listPos);
            return super.getItem(listPos - (section + 1));
        }
    }

    @Override
    public DatabaseReference getRef(int listPos) {
        if (isHeader(listPos))
            return null;
        int section = sectionOfPosition(listPos);
        return super.getRef(listPos - (section + 1));
    }

    /**
     * Determine the section number of a given list position
     * @param listPos the list position of an item
     * @return
     */
    public int sectionOfPosition(int listPos) {
        int section = 0;
        while (listPos >= rowCount[section]) {
            listPos -= rowCount[section];
            section++;
        }
        return section;
    }

    /**
     * Determine the first list position of a given section
     * @param sec
     * @return
     */
    public int positionOfSection(int sec) {
        int pos = 0;
        for (int k = 0; k < sec; k++)
            pos += rowCount[k];
        return pos;
    }

    /**
     * Calculate the section position of a given list position
     * @param listPos the list position of an item
     * @return
     */
    private int positionInSection (int listPos) {
        int section = 0;
        while (listPos >= rowCount[section]) {
            listPos -= rowCount[section];
            section++;
        }
        return listPos - 1; /* the first item in each section is a
        header */
    }

    private boolean isHeader (int pos) {
        return positionInSection(pos) == -1;
    }

    @Override
    public @LayoutRes int getItemViewType(int position) {
        return isHeader(position) ? headerLayout : itemLayout;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      @LayoutRes int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate
                (viewType, parent, false);
        RecyclerView.ViewHolder holder;
        try {
            if (viewType == itemLayout) {
                Constructor<VH> ctor = itemClass.getConstructor(View.class);
                holder = ctor.newInstance(view);
            } else {
                Constructor<HVH> ctor = headerClass.getConstructor(View
                        .class);
                holder = ctor.newInstance(view);
            }
            return holder;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int listPos, T model) {
        int section = sectionOfPosition(listPos);
        if (!isHeader(listPos)) {
            int secPos = positionInSection(listPos);
            populateItemViewHolder((VH)viewHolder, model, section, secPos,
                    listPos);
        } else {
            populateHeaderViewHolder((HVH)viewHolder, section);
        }
    }

    abstract int getSectionCount();
    abstract int getItemCountForSection(int section);
    abstract void populateItemViewHolder(VH ViewHolder, T model,
                                         int section, int posInSection,
                                         int listPos);
    abstract void populateHeaderViewHolder(HVH ViewHolder, int section);
}
