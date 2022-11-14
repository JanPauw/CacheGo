package com.cache.cachego;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.fragment.app.Fragment;

import com.cache.cachego.databinding.FragmentCachePreferencesBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CachePreferencesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CachePreferencesFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FragmentCachePreferencesBinding binding;
    Fragment OverlayFragment;

    public CachePreferencesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_cachePreferences.
     */
    // TODO: Rename and change types and number of parameters
    public static CachePreferencesFragment newInstance(String param1, String param2) {
        CachePreferencesFragment fragment = new CachePreferencesFragment();
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

    //Difficulties
    String easy;
    String hard;
    String normal;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCachePreferencesBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        StaticFields sf = new StaticFields();

        DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference refUser = refUsers.child("" + sf.getAuthor());
        DatabaseReference refSettings = refUser.child("settings");
        DatabaseReference refCachePref = refSettings.child("cache-preferences");

        if (sf.getEasy().equals("true")) {
            binding.cbEasy.setChecked(true);
        } else {
            binding.cbEasy.setChecked(false);
        }

        if (sf.getNormal().equals("true")) {
            binding.cbNormal.setChecked(true);
        } else {
            binding.cbNormal.setChecked(false);
        }

        if (sf.getHard().equals("true")) {
            binding.cbHard.setChecked(true);
        } else {
            binding.cbHard.setChecked(false);
        }

        binding.cbEasy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    refCachePref.child("easy").setValue("true");
                } else {
                    refCachePref.child("easy").setValue("false");
                }

                sf.setPreferenceUpdate(true);
            }
        });

        binding.cbNormal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    refCachePref.child("normal").setValue("true");
                } else {
                    refCachePref.child("normal").setValue("false");
                }

                sf.setPreferenceUpdate(true);
            }
        });

        binding.cbHard.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    refCachePref.child("hard").setValue("true");
                } else {
                    refCachePref.child("hard").setValue("false");
                }

                sf.setPreferenceUpdate(true);
            }
        });

        return view;
    }
}