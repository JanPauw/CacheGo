package com.cachego.cachego;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cachego.cachego.databinding.FragmentAccountBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    //Our own Variables
    private FragmentAccountBinding binding;
    Fragment OverlayFragment;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AccountFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_account.
     */
    // TODO: Rename and change types and number of parameters
    public static AccountFragment newInstance(String param1, String param2) {
        AccountFragment fragment = new AccountFragment();
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
        //Our own code
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        StaticFields sf = new StaticFields();

        GetFavouritesID();
        GetCurrentMeasurement();

        //Set Name TextView
        binding.tvName.setText(sf.getUserName());

        binding.rlMyCaches.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getParentFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                OverlayFragment = new MyCachesFragment();
                transaction.replace(R.id.overlay_fragment, OverlayFragment);
                transaction.commit();
            }
        });

        binding.rlFavourites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getParentFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                OverlayFragment = new FavouriteCachesFragment();
                transaction.replace(R.id.overlay_fragment, OverlayFragment);
                transaction.commit();
            }
        });

        binding.rlCacheHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getParentFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                OverlayFragment = new CacheHisoryFragment();
                transaction.replace(R.id.overlay_fragment, OverlayFragment);
                transaction.commit();
            }
        });

        return view;
    }

    //Get all Favourite IDs into ArrayList: alFavourites
    private void GetFavouritesID() {
        StaticFields sf = new StaticFields();
        ArrayList<String> alFavourites = new ArrayList<String>();

        DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference refUser = refUsers.child("" + sf.getAuthor());
        DatabaseReference refFavourites = refUser.child("favourites");

        refFavourites.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                alFavourites.clear();
                int counter = 0;

                for (DataSnapshot snap : snapshot.getChildren()) {
                    String CacheID = snap.child("id").getValue(String.class);
                    alFavourites.add(CacheID);
                    counter++;
                }

                sf.setAlFavorites(alFavourites);
                Log.d(TAG, "FAVOURITES LOADED FOR USER " + sf.getAuthor() + ": COUNT " + counter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void GetCurrentMeasurement() {
        StaticFields sf = new StaticFields();

        DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference refUser = refUsers.child("" + sf.getAuthor());
        DatabaseReference refSettings = refUser.child("settings");
        DatabaseReference refMeasurement = refSettings.child("measurement");
        refMeasurement.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sf.setMetricSystem(snapshot.getValue(String.class));
                Log.d(TAG, "Set Measurment Setting: " + sf.getMetricSystem());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}