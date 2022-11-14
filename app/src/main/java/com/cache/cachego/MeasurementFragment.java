package com.cache.cachego;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.cache.cachego.databinding.FragmentMeasurementBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MeasurementFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MeasurementFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FragmentMeasurementBinding binding;
    Fragment OverlayFragment;

    public MeasurementFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_measurements.
     */
    // TODO: Rename and change types and number of parameters
    public static MeasurementFragment newInstance(String param1, String param2) {
        MeasurementFragment fragment = new MeasurementFragment();
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
        binding = FragmentMeasurementBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        StaticFields sf = new StaticFields();

        binding.cbMetric.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    binding.cbImperial.setChecked(false);
                    SetMeasurementSetting("metric");
                } else {
                    binding.cbImperial.setChecked(true);
                }
            }
        });

        binding.cbImperial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    binding.cbMetric.setChecked(false);
                    SetMeasurementSetting("imperial");
                } else {
                    binding.cbMetric.setChecked(true);
                }
            }
        });

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getParentFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                OverlayFragment = new SettingsFragment();
                transaction.replace(R.id.overlay_fragment, OverlayFragment);
                transaction.commit();
            }
        });

        switch (sf.getMetricSystem()) {
            case "metric":
                binding.cbMetric.setChecked(true);
                binding.cbImperial.setChecked(false);
                break;
            case "imperial":
                binding.cbMetric.setChecked(false);
                binding.cbImperial.setChecked(true);
                break;
        }

        return view;
    }

    private void SetMeasurementSetting(String m) {
        StaticFields sf = new StaticFields();

        DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference refUser = refUsers.child("" + sf.getAuthor());
        DatabaseReference refSettings = refUser.child("settings");
        DatabaseReference refMeasurement = refSettings.child("measurement");

        refMeasurement.setValue("" + m);
    }
}