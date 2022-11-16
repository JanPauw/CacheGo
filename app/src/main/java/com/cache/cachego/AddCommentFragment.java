package com.cache.cachego;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cache.cachego.databinding.FragmentAddCommentBinding;
import com.cache.cachego.databinding.FragmentCacheDetailsBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddCommentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddCommentFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AddCommentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddCommentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddCommentFragment newInstance(String param1, String param2) {
        AddCommentFragment fragment = new AddCommentFragment();
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

    FragmentAddCommentBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddCommentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = ((MapsActivity) view.getContext()).getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                Fragment OverlayFragment = new CacheDetailsFragment();
                transaction.replace(R.id.overlay_fragment, OverlayFragment);
                transaction.commit();
            }
        });

        binding.btnAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                LocalDateTime now = LocalDateTime.now();

                StaticFields sf = new StaticFields();

                String date = dtf.format(now);

                String FullName = sf.getUserName();
                int iPos = FullName.indexOf(' ');

                String author = FullName;
                if (iPos == -1) {
                } else {
                    author = FullName.substring(0, iPos);
                }


                String comment = binding.edtComment.getText().toString().trim();

                String commentId = "" + System.currentTimeMillis();

                DatabaseReference refCache = FirebaseDatabase.getInstance().getReference("comments").child("" + sf.getSelectedId());
                DatabaseReference refComments = refCache.child("" + commentId);

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("date", date);
                hashMap.put("author", author);
                hashMap.put("comment", comment);

                refComments.setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        FragmentManager manager = ((MapsActivity) view.getContext()).getSupportFragmentManager();
                        FragmentTransaction transaction = manager.beginTransaction();
                        Fragment OverlayFragment = new CacheDetailsFragment();
                        transaction.replace(R.id.overlay_fragment, OverlayFragment);
                        transaction.commit();
                    }
                });
            }
        });

        return view;
    }
}