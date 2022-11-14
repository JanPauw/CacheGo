package com.cache.cachego;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.cache.cachego.databinding.FragmentCacheDetailsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Tag;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CacheDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CacheDetailsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private StaticFields placeholder = new StaticFields();
    private FragmentCacheDetailsBinding binding;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CacheDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_cache_details.
     */
    // TODO: Rename and change types and number of parameters
    public static CacheDetailsFragment newInstance(String param1, String param2) {
        CacheDetailsFragment fragment = new CacheDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    ListView listView;
    CardView cardView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCacheDetailsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        listView = view.findViewById(R.id.lvCacheComments);
        cardView = view.findViewById(R.id.cvComment);

        GetCacheDetails();

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StaticFields sf = new StaticFields();

                FragmentManager manager = ((MapsActivity) view.getContext()).getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                Fragment OverlayFragment;

                if (sf.getPreviousFragment() == null) {
                    OverlayFragment = new AccountFragment();
                    transaction.replace(R.id.overlay_fragment, OverlayFragment);
                    transaction.hide(OverlayFragment);
                } else {
                    OverlayFragment = sf.getPreviousFragment();
                    transaction.replace(R.id.overlay_fragment, OverlayFragment);
                }

                transaction.commit();
            }
        });

        binding.btnAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getParentFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                Fragment OverlayFragment = new AddCommentFragment();
                transaction.replace(R.id.overlay_fragment, OverlayFragment);
                transaction.commit();
            }
        });

        binding.btnNavigateToCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent i = new Intent(getContext(), NavigationActivity.class);
                startActivity(i);*/
                FragmentManager manager = getParentFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                Fragment OverlayFragment;

                Fragment NavFragment = new NavigationFragment();
                transaction.replace(R.id.navigation_fragment, NavFragment);
                transaction.remove(CacheDetailsFragment.this);
                transaction.commit();
            }
        });

        return view;
    }

    private void GetCacheDetails() {
        DatabaseReference refCache = FirebaseDatabase.getInstance().getReference("caches").child("" + placeholder.getSelectedId());
        DatabaseReference refComments = FirebaseDatabase.getInstance().getReference("comments").child("" + placeholder.getSelectedId());

        //Get Cache Details
        refCache.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Cache c = new Cache();
                DataSnapshot snap = snapshot;

                c.setArea_name(snap.child("area_name").getValue().toString());
                c.setAuthor(snap.child("author").getValue().toString());
                c.setDescription(snap.child("description").getValue().toString());
                c.setDifficulty(snap.child("difficulty").getValue().toString());
                c.setId(snap.child("id").getValue().toString());
                c.setLat(Double.parseDouble(snap.child("lat").getValue().toString()));
                c.setLon(Double.parseDouble(snap.child("lon").getValue().toString()));
                c.setName(snap.child("name").getValue().toString());

                LoadCacheDetails(c);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        refComments.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Comment> alComments = new ArrayList<Comment>();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    if (snap.getChildrenCount() != 3) {

                    } else {
                        Comment comment = new Comment();

                        String strDate = snap.child("date").getValue().toString();
                        String strAuthor = snap.child("author").getValue().toString();
                        String strComment = snap.child("comment").getValue().toString();

                        if (strDate != null && strAuthor != null && strComment != null) {
                            comment.setDate(strDate);
                            comment.setAuthor(strAuthor);
                            comment.setComment(strComment);

                            alComments.add(comment);
                            Log.d(TAG, "COMMENTS: comment read");
                        }
                    }
                }

                try {
                    CommentsAdapter cAdapter = new CommentsAdapter(getActivity(), alComments);
                    listView.setAdapter(cAdapter);
                }
                catch (Exception e) {

                }

                setListViewHeightBasedOnItems(listView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //Get Cache Comments

    }

    private void LoadCacheDetails(Cache c) {
        binding.tvCacheName.setText(c.getName());
        binding.tvCacheDescription.setText(c.getDescription());
        binding.tvCacheArea.setText(c.getArea_name());

        switch (c.getDifficulty()) {
            case "easy":
                //Change Background Colors
                binding.tvEasyTitle.setBackgroundColor(getResources().getColor(R.color.blue));
                binding.ivEasy.setBackgroundColor(getResources().getColor(R.color.blue));
                break;
            case "normal":
                //Change Background Colors
                binding.tvNormalTitle.setBackgroundColor(getResources().getColor(R.color.blue));
                binding.ivNormal.setBackgroundColor(getResources().getColor(R.color.blue));
                break;
            case "hard":
                //Change Background Colors
                binding.tvHardTitle.setBackgroundColor(getResources().getColor(R.color.blue));
                binding.ivHard.setBackgroundColor(getResources().getColor(R.color.blue));
                break;
        }
    }

//    public ArrayList<Comment> GenerateTestComments() {
//        Comment comment1 = new Comment();
//        Comment comment2 = new Comment();
//        Comment comment3 = new Comment();
//        Comment comment4 = new Comment();
//
//        comment1.setDate("2022/11/14");
//        comment1.setComment("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus sed consectetur velit. Vivamus accumsan diam quis semper vulputate. Sed sagittis dapibus pharetra.");
//        comment1.setAuthor("Jan");
//
//        comment2.setDate("2022/11/13");
//        comment2.setComment("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus sed consectetur velit. Vivamus accumsan diam quis semper vulputate. Sed sagittis dapibus pharetra.");
//        comment2.setAuthor("Henco");
//
//        comment3.setDate("2022/11/12");
//        comment3.setComment("Vivamus accumsan diam quis semper vulputate. Sed sagittis dapibus pharetra.");
//        comment3.setAuthor("David");
//
//        comment4.setDate("2022/11/12");
//        comment4.setComment("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus sed consectetur velit. Vivamus accumsan diam quis semper vulputate. Sed sagittis dapibus pharetra.");
//        comment4.setAuthor("Oliver");
//
//        ArrayList<Comment> alComments = new ArrayList<>();
//        alComments.add(comment1);
//        alComments.add(comment2);
//        alComments.add(comment3);
//        alComments.add(comment4);
//
//        return alComments;
//    }


//    private void GetCacheComments(Cache c) {
//
//        CommentsAdapter cAdapter = new CommentsAdapter(getContext(), GenerateTestComments());
//        listView.setAdapter(cAdapter);
//
//        boolean setListViewSize = setListViewHeightBasedOnItems(listView);
//    }

    public static boolean setListViewHeightBasedOnItems(ListView listView) {

        CommentsAdapter listAdapter = (CommentsAdapter) listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                float px = 300 * (listView.getResources().getDisplayMetrics().density);
                item.measure(View.MeasureSpec.makeMeasureSpec((int) px, View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);
            // Get padding
            int totalPadding = listView.getPaddingTop() + listView.getPaddingBottom();

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight + totalPadding;
            listView.setLayoutParams(params);
            listView.requestLayout();
            //setDynamicHeight(listView);
            return true;

        } else {
            return false;
        }

    }
}