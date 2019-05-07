package edu.gvsu.cis.traxy;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;

import butterknife.BindView;
import butterknife.ButterKnife;
import edu.gvsu.cis.traxy.model.JournalEntry;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnPhotoInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PhotoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PhotoFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private static final String ARG_PARAM1 = "FIREBASE_URL";

    @BindView(R.id.photoList)
    RecyclerView photoList;

    // TODO: Rename and change types of parameters
    private DatabaseReference dbRef;
    private Query photoQuery;

    private OnPhotoInteractionListener mListener;
    private PhotoAdapter adapter;

    public PhotoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param entriesUrl Firebase Database URL to the journal entries of
     *                   the current trip
     * @return A new instance of fragment PhotoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PhotoFragment newInstance(String entriesUrl) {
        PhotoFragment fragment = new PhotoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, entriesUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String url = getArguments().getString(ARG_PARAM1);
            dbRef = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(url);
            photoQuery = dbRef.orderByChild("type").equalTo(2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_photo, container,
                false);
        ButterKnife.bind(this, view);
        photoList.setLayoutManager(
                new GridLayoutManager(view.getContext(), 3));
        FirebaseRecyclerOptions<JournalEntry> options;
        options = new FirebaseRecyclerOptions.Builder<JournalEntry>()
                .setQuery(photoQuery, JournalEntry.class).build();
        adapter = new PhotoAdapter(options);
        photoList.setAdapter(adapter);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
//            mListener.onPhotoSelected(uri.getPath());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPhotoInteractionListener) {
            mListener = (OnPhotoInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPhotoInteractionListener");
        }
    }

    private class PhotoAdapter extends
            FirebaseRecyclerAdapter<JournalEntry,PhotoHolder> {

        private FirebaseImageLoader imgLoader;
        private FirebaseStorage storage;
        private int selectedPosition;

        public PhotoAdapter(FirebaseRecyclerOptions<JournalEntry> options) {
            super(options);
//            super(JournalEntry.class, R.layout.photo_item,
//                    PhotoHolder.class, photoQuery);
            imgLoader = new FirebaseImageLoader();
            storage = FirebaseStorage.getInstance();
            selectedPosition = -1;
        }

        @NonNull
        @Override
        public PhotoHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return null;
        }

        @Override
        protected void onBindViewHolder(@NonNull PhotoHolder viewHolder, int position, @NonNull JournalEntry model) {
            String url = model.getUrl();
            if (url != null && url.startsWith("http")) {
                Glide.with(PhotoFragment.this)
//                        .using(imgLoader)
                        .load(storage.getReferenceFromUrl(url))
//                        .centerCrop()
                        .into(viewHolder.photo);
            }
            viewHolder.selected.setVisibility(position ==
                    selectedPosition ? View.VISIBLE : View.GONE);
            viewHolder.photo.setOnClickListener( v -> {
                if (selectedPosition != -1)
                    notifyItemChanged(selectedPosition);
                notifyItemChanged(position);
                this.selectedPosition = position;
                mListener.onPhotoSelected(model.getUrl());
            });
        }

    };
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnPhotoInteractionListener {
        void onPhotoSelected(String url);
    }
}
