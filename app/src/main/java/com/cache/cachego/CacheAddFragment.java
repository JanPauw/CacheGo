package com.cache.cachego;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.cache.cachego.databinding.FragmentCacheAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CacheAddFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CacheAddFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    //ViewBinding
    private FragmentCacheAddBinding binding;
    //Progress Dialog
    ProgressDialog progressDialog;
    //StaticFields object to access static fields
    private StaticFields placeholder = new StaticFields();
    private Cache CacheToAdd = new Cache();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CacheAddFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_addcache.
     */
    // TODO: Rename and change types and number of parameters
    public static CacheAddFragment newInstance(String param1, String param2) {
        CacheAddFragment fragment = new CacheAddFragment();
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

    //Cache Form Variables
    private String CacheDifficulty = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Our own code
        binding = FragmentCacheAddBinding.inflate(inflater,container,false);
        View view = binding.getRoot();

        //Instantiate ProgressDialog
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        //Difficulty Selection OnClick
        binding.rlDiffEasy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectDifficulty(0);
            }
        });

        binding.rlDiffNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectDifficulty(1);
            }
        });

        binding.rlDiffHard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectDifficulty(2);
            }
        });

        //Add Cache Button
        binding.btnAddCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ValidateData();
            }
        });

        return view;
    }

    //Write to Firebase
    private void CacheToFirebase() {
        //Show Progress Dialog
        progressDialog.setMessage("Adding Cache...");
        progressDialog.show();

        //Get ID for Collection
        String id = "" + System.currentTimeMillis();
        CacheToAdd.setId(id);

        //Create HashMap to push to FireBase
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("area_name", "" + CacheToAdd.getArea_name());
        hashMap.put("author", "" + CacheToAdd.getAuthor());
        hashMap.put("description", "" + CacheToAdd.getDescription());
        hashMap.put("difficulty", "" + CacheToAdd.getDifficulty());
        hashMap.put("id", "" + CacheToAdd.getId());
        hashMap.put("lat", CacheToAdd.getLat());
        hashMap.put("lon", CacheToAdd.getLon());
        hashMap.put("name", "" + CacheToAdd.getName());

        //Get Reference
        DatabaseReference refCaches = FirebaseDatabase.getInstance().getReference("caches");
        //Set firebase id and add hashmap
        refCaches.child("" + CacheToAdd.getId())
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //Load Variables with Form Input
    private void SetData() {
        CacheToAdd.setName(binding.edtCacheName.getText().toString().trim());
        CacheToAdd.setDescription(binding.edtCacheDescription.getText().toString().trim());
        CacheToAdd.setArea_name(binding.edtCacheArea.getText().toString().trim());
        CacheToAdd.setDifficulty(CacheDifficulty);

        CacheToAdd.setAuthor(placeholder.getAuthor());
        CacheToAdd.setLon(placeholder.getLon());
        CacheToAdd.setLat(placeholder.getLat());

        CacheToFirebase();
    }

    //Check Valid Inputs
    private void ValidateData() {
        //Cache Name
        if (binding.edtCacheName.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), "Please enter Cache Name.", Toast.LENGTH_SHORT).show();
            return;
        }

        //Cache Description
        if (binding.edtCacheDescription.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), "Please enter Cache Description.", Toast.LENGTH_SHORT).show();
            return;
        }

        //Cache Area Name
        if (binding.edtCacheArea.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), "Please enter Cache Area Name.", Toast.LENGTH_SHORT).show();
            return;
        }

        //Cache Difficulty
        if (!(CacheDifficulty.equals("easy") || CacheDifficulty.equals("normal") || CacheDifficulty.equals("hard"))) {
            Toast.makeText(getContext(), "Please select a Difficulty.", Toast.LENGTH_SHORT).show();
            return;
        }

        SetData();
    }

    private void ClearDiffColors() {
        binding.tvEasyTitle.setBackgroundColor(getResources().getColor(R.color.yellow));
        binding.ivEasy.setBackgroundColor(getResources().getColor(R.color.yellow));
        binding.tvNormalTitle.setBackgroundColor(getResources().getColor(R.color.yellow));
        binding.ivNormal.setBackgroundColor(getResources().getColor(R.color.yellow));
        binding.tvHardTitle.setBackgroundColor(getResources().getColor(R.color.yellow));
        binding.ivHard.setBackgroundColor(getResources().getColor(R.color.yellow));
    }

    private void SelectDifficulty(int index) {
        ClearDiffColors();

        switch(index) {
            case 0:
                CacheDifficulty = "easy";
                //Change Background Colors
                binding.tvEasyTitle.setBackgroundColor(getResources().getColor(R.color.blue));
                binding.ivEasy.setBackgroundColor(getResources().getColor(R.color.blue));
                break;
            case 1:
                CacheDifficulty = "normal";
                //Change Background Colors
                binding.tvNormalTitle.setBackgroundColor(getResources().getColor(R.color.blue));
                binding.ivNormal.setBackgroundColor(getResources().getColor(R.color.blue));
                break;
            case 2:
                CacheDifficulty = "hard";
                //Change Background Colors
                binding.tvHardTitle.setBackgroundColor(getResources().getColor(R.color.blue));
                binding.ivHard.setBackgroundColor(getResources().getColor(R.color.blue));
                break;
        }
    }
}