package com.cache.cachego;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FavouriteCachesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FavouriteCachesFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FavouriteCachesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FavouriteCachesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FavouriteCachesFragment newInstance(String param1, String param2) {
        FavouriteCachesFragment fragment = new FavouriteCachesFragment();
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
        View view = inflater.inflate((R.layout.fragment_favourite_caches), container, false);

        listView = view.findViewById(R.id.lvFaveCaches);
        cardView = view.findViewById(R.id.cvMyCaches);

        //Load Cache History
        GetFavouriteCaches();

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = ((MapsActivity)view.getContext()).getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                Fragment OverlayFragment = new AccountFragment();
                transaction.replace(R.id.overlay_fragment, OverlayFragment);
                transaction.commit();
            }
        });

        return view;
    }

    private void GetFavouriteCaches() {
        StaticFields sf = new StaticFields();
        DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference refUser = refUsers.child("" + sf.getAuthor());
        DatabaseReference refCacheHistory = refUser.child("favourites");

        refCacheHistory.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Cache> alCaches = sf.getCacheArrayList();
                ArrayList<Cache> alFaveCaches = new ArrayList<Cache>();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    String CacheID = snap.child("id").getValue(String.class);

                    for (int i = 0; i < alCaches.size(); i++) {
                        if (alCaches.get(i).getId().equals(CacheID)) {
                            alFaveCaches.add(alCaches.get(i));
                        }
                    }

                    try {
                        FavouriteCachesAdapter fcAdapter = new FavouriteCachesAdapter(getContext(), alFaveCaches);
                        listView.setAdapter(fcAdapter);
                    } catch (Exception e) {

                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}