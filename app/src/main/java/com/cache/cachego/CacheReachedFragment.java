package com.cache.cachego;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.cache.cachego.databinding.FragmentCacheReachedBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CacheReachedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CacheReachedFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private StaticFields placeholder = new StaticFields();
    private FragmentCacheReachedBinding binding;
    private Cache navCache = new Cache();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CacheReachedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CacheReachedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CacheReachedFragment newInstance(String param1, String param2) {
        CacheReachedFragment fragment = new CacheReachedFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCacheReachedBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        GetCacheDetails();

        return view;
    }

    private void GetCacheDetails() {
        DatabaseReference refCache = FirebaseDatabase.getInstance().getReference("caches").child("" + placeholder.getSelectedId());

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
    }

    private void LoadCacheDetails(Cache c) {
        navCache = c;

        binding.tvCacheName.setText(c.getName());
        binding.tvCacheDescription.setText(c.getDescription());
        binding.tvCacheArea.setText(c.getArea_name());

        switch(c.getDifficulty()) {
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

        binding.btnCacheFound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StaticFields sf = new StaticFields();

                //Get to User directory in Firebase DB
                String userId = sf.getAuthor();
                DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("users");
                DatabaseReference refUser = refUsers.child("" + userId);
                DatabaseReference CacheHistory = refUser.child("cache-history");
                DatabaseReference cache = CacheHistory.child(navCache.getId());
                cache.child("id").setValue(navCache.getId());


                FragmentManager manager = getParentFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();

                Fragment NavFragment = new NavigationFragment();
                Fragment OverlayFragment = new AccountFragment();

                transaction.replace(R.id.navigation_fragment, NavFragment);
                transaction.replace(R.id.overlay_fragment, OverlayFragment);

                transaction.remove(OverlayFragment);
                transaction.remove(NavFragment);
                transaction.commit();
                sf.setPreferenceUpdate(true);
            }
        });
    }
}