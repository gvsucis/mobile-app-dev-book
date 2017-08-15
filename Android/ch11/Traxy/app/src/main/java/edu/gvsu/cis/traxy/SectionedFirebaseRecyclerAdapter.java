package edu.gvsu.cis.traxy;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.lang.reflect.Constructor;

/**
 * Created by dulimarh on 8/13/17.
 */

public abstract class SectionedFirebaseRecyclerAdapter<T,VH extends
        RecyclerView.ViewHolder, SVH extends RecyclerView.ViewHolder>
        extends FirebaseRecyclerAdapter<T,RecyclerView.ViewHolder> {
    private final Class<VH> itemClass;
    private final Class<SVH> headerClass;
    private int[] rowCount;
    private int totalRows;
    private int itemLayout;
    private int headerLayout;

    /**
     *
     * @param modelClass  the class of the list items
     * @param itemLayout  XML layout of each list item
     * @param itemHolderClass  ViewHolder subclass of the list item
     * @param headerLayout  XML layout of the section headers
     * @param headerHolderClass  ViewHolder subclass of the section headers
     * @param q reference to the Firebase datasource
     */
    SectionedFirebaseRecyclerAdapter(Class<T> modelClass,
                                     @LayoutRes int itemLayout,
                                     Class<VH> itemHolderClass,
                                     @LayoutRes int headerLayout,
                                     Class<SVH> headerHolderClass,
                                     Query q) {
        super(modelClass, itemLayout,
                (Class<RecyclerView.ViewHolder>) itemHolderClass, q);
        this.itemLayout = itemLayout;
        this.headerLayout = headerLayout;
        this.itemClass = itemHolderClass;
        this.headerClass = headerHolderClass;
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

    @Override
    public T getItem(int position) {
        if (isHeader(position))
            return null;
        else {
            int section = sectionOfPosition(position);
            return super.getItem(position - (section + 1));
        }
    }

    @Override
    public DatabaseReference getRef(int position) {
        if (isHeader(position))
            return null;
        int section = sectionOfPosition(position);
        return super.getRef(position - (section + 1));
    }

    /**
     * Determine the section number of a given list position
     * @param pos the list position of an item
     * @return
     */
    public int sectionOfPosition(int pos) {
        int section = 0;
        while (pos >= rowCount[section]) {
            pos -= rowCount[section];
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
     * @param pos the list position of an item
     * @return
     */
    private int positionInSection (int pos) {
        int section = 0;
        while (pos >= rowCount[section]) {
            pos -= rowCount[section];
            section++;
        }
        return pos - 1; /* the first item in each section is a header */
    }

    private boolean isHeader (int pos) {
        return positionInSection(pos) == -1;
    }

    @Override
    public @LayoutRes int getItemViewType(int position) {
        return positionInSection(position) == -1 ? headerLayout :
                itemLayout;
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
                Constructor<SVH> ctor = headerClass.getConstructor(View
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
    protected void populateViewHolder(RecyclerView.ViewHolder viewHolder, T model, int
            position) {
        int section = sectionOfPosition(position);
        if (!isHeader(position)) {
            int secPos = positionInSection(position);
            populateItemViewHolder((VH)viewHolder, model, section, secPos,
                    position);
        } else {
            populateHeaderViewHolder((SVH)viewHolder, section);
        }
    }

    abstract int getSectionCount();
    abstract int getItemCountForSection(int section);
    abstract void populateItemViewHolder(VH ViewHolder, T model,
                                         int section, int posInSection,
                                         int posInDB);
    abstract void populateHeaderViewHolder(SVH ViewHolder, int section);
}
