package com.cache.cachego;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.cache.cachego.databinding.FragmentMyCachesBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyCachesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyCachesFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    //Our own Variables
    private FragmentMyCachesBinding binding;
    Fragment OverlayFragment;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MyCachesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_mycaches.
     */
    // TODO: Rename and change types and number of parameters
    public static MyCachesFragment newInstance(String param1, String param2) {
        MyCachesFragment fragment = new MyCachesFragment();
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
        //Our own code
        binding = FragmentMyCachesBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        listView = view.findViewById(R.id.lvMyCaches);
        cardView = view.findViewById(R.id.cvMyCaches);
        GetMyCaches();

        binding.btnAddCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getParentFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                OverlayFragment = new CacheAddFragment();
                transaction.replace(R.id.overlay_fragment, OverlayFragment);
                transaction.commit();
            }
        });

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
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

    private void GetMyCaches() {
        StaticFields sf = new StaticFields();
        DatabaseReference refCaches = FirebaseDatabase.getInstance().getReference("caches");

        refCaches.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Cache> alCaches = sf.getCacheArrayList();
                ArrayList<Cache> alMyCaches = new ArrayList<Cache>();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    String UserID = sf.getAuthor();
                    String CacheAuthor = snap.child("author").getValue(String.class);

                    if (UserID.equals(CacheAuthor)) {
                        Cache c = snap.getValue(Cache.class);
                        alMyCaches.add(c);
                    }

                    try {
                        MyCachesAdapter mcAdapter = new MyCachesAdapter(getContext(), alMyCaches);
                        listView.setAdapter(mcAdapter);
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