package com.cachego.cachego;

import static android.content.ContentValues.TAG;
import static com.cachego.cachego.R.color.blue;
import static com.cachego.cachego.R.color.green;
import static com.cachego.cachego.R.color.yellow;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.cachego.cachego.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener {

    //Location Tracking
    LocationManager locationManager;
    private static final int GPS_TIME_INTERVAL = 50; // get GPS location every 1 min
    private static final int GPS_DISTANCE = 1000; // set distance value in meter
    private static final int HANDLER_DELAY = 50;
    private static final int START_HANDLER_DELAY = 0;
    final static String[] PERMISSION = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    final static int PERMISSION_ALL = 1;
    private Marker myLocation;
    private LatLng loc;
    float mDeclination;
    private boolean CachesLoaded = false;

    private StaticFields placeholder = new StaticFields();

    //Location found
    private boolean locFound = false;

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FirebaseAuth FireAuth;

    private ArrayList<Cache> cacheArrayList;
    private ArrayList<Marker> markerArrayList;

    Fragment OverlayFragment;
    private ProgressDialog progressDialog;


    //calculate route
    //to get location permissions.
    private final static int LOCATION_REQUEST_CODE = 23;
    boolean locationPermission = false;

    //polyline object
    private List<Polyline> polylines = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FireAuth = FirebaseAuth.getInstance();
        CheckUser();
        GetDifficulties();

        //Progress Dialog for Stalling user...
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //Display Progress Dialog until locFound == true;
        if (!locFound) {
            progressDialog.setMessage("Finding your location.");
            progressDialog.show();
        } else {
            progressDialog.dismiss();
        }

        placeholder.setAuthor(FireAuth.getUid());
        SetUserName();

        SetOverlayFragment(1);

        //Permissions for newer SDK
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(PERMISSION, PERMISSION_ALL);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Location Updating
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                requestLocation();
                handler.postDelayed(this, HANDLER_DELAY);
            }
        }, START_HANDLER_DELAY);

        //Nav Bar Button CLicks
        //ImageButton Index 0
        binding.navAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = 0;
                SetActiveNavBarColor(index);
                SetOverlayFragment(index);
            }
        });

        //ImageButton Index 1
        binding.navMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = 1;
                SetActiveNavBarColor(index);
                SetOverlayFragment(index);
            }
        });

        //ImageButton Index 2
        binding.navSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = 2;
                SetActiveNavBarColor(index);
                SetOverlayFragment(index);
            }
        });

        binding.btnRelocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
                } catch (Exception e) {

                }
            }
        });
    }

    private void SetUserName() {
        DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference refUser = refUsers.child("" + placeholder.getAuthor());

        refUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Set Name
                StaticFields sf = new StaticFields();
                String name = snapshot.child("name").getValue(String.class);
                sf.setUserName(name);
                Log.d(TAG, "USER DETAILS: Name: " + name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void CheckUser() {
        // get current user
        FirebaseUser firebaseUser = FireAuth.getCurrentUser();
        if (firebaseUser == null) {
            // not logged in,goto main screen
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
        } else {

        }
    }

    //Set Active NavBar Color
    private void SetActiveNavBarColor(int index) {
        binding.navAccount.setBackgroundColor(getResources().getColor(yellow));
        binding.navMap.setBackgroundColor(getResources().getColor(yellow));
        binding.navSettings.setBackgroundColor(getResources().getColor(yellow));

        switch (index) {
            case 0:
                binding.navAccount.setBackgroundColor(getResources().getColor(green));
                break;
            case 1:
                binding.navMap.setBackgroundColor(getResources().getColor(blue));
                break;
            case 2:
                binding.navSettings.setBackgroundColor(getResources().getColor(green));
                break;
        }
    }

    //Set OverlayFragment
    private void SetOverlayFragment(int index) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        switch (index) {
            case 0:
                OverlayFragment = new AccountFragment();
                transaction.replace(R.id.overlay_fragment, OverlayFragment);
                transaction.commit();
                break;
            case 1:
                OverlayFragment = new AccountFragment();
                transaction.replace(R.id.overlay_fragment, OverlayFragment);
                transaction.hide(OverlayFragment);
                transaction.commit();
                break;
            case 2:
                OverlayFragment = new SettingsFragment();
                transaction.replace(R.id.overlay_fragment, OverlayFragment);
                transaction.commit();
                break;
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);

        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
    }

    private void GetDifficulties() {
        DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference refUser = refUsers.child("" + placeholder.getAuthor());
        DatabaseReference refSettings = refUser.child("settings");
        DatabaseReference refCachePref = refSettings.child("cache-preferences");

        refCachePref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                placeholder.setEasy(snapshot.child("easy").getValue(String.class));
                placeholder.setNormal(snapshot.child("normal").getValue(String.class));
                placeholder.setHard(snapshot.child("hard").getValue(String.class));

                Log.d(TAG, "Selected Difficulties: Easy: " + placeholder.getEasy() + " | Normal: " + placeholder.getNormal() + " | Hard: " + placeholder.getHard());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        GetDifficulties();

        Log.d("mylog", "Got Location: " + location.getLatitude() + ", " + location.getLongitude());
        locationManager.removeUpdates(this);

        loc = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions marker = new MarkerOptions().position(loc);

        placeholder.setLat(location.getLatitude());
        placeholder.setLon(location.getLongitude());

        //Icon Creation
        int height = 50;
        int width = 50;
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.map_user_icon);
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);

        //Set Marker Icon
        marker.icon(smallMarkerIcon);
        //Remove Marker before adding updated version...
        try {
            myLocation.remove();
        } catch (Exception e) {

        }
        //updating current location marker
        myLocation = mMap.addMarker(marker);
        String tag = "MyLocation";
        myLocation.setTag(tag);

        if (locFound == false) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
            progressDialog.dismiss();
            locFound = true;
        }

        LoadCacheHistory(marker);
    }

    @SuppressLint("MissingPermission")
    private void requestLocation() {
        if (locationManager == null)
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_TIME_INTERVAL, GPS_DISTANCE, this);
            }
        }
    }

    private ArrayList<String> listCacheHistory = new ArrayList<String>();

    private void LoadCacheHistory(MarkerOptions marker) {
        markerArrayList = new ArrayList<>();
        //Check if Cache has been found before
        DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference refCacheHistory = refUsers.child("" + placeholder.getAuthor()).child("cache-history");

        refCacheHistory.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listCacheHistory.clear();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    String cacheId = snap.child("id").getValue(String.class);

                    listCacheHistory.add(cacheId);
                }

                if (placeholder.getEasy() == null && placeholder.getNormal() == null && placeholder.getHard() == null) {

                } else {
                    if (CachesLoaded == false || placeholder.isPreferenceUpdate() == true) {
                        //Clear map of all markers
                        mMap.clear();
                        //Add Location back to map
                        myLocation = mMap.addMarker(marker);
                        String tag = "MyLocation";
                        myLocation.setTag(tag);

                        //Load outstanding markers
                        LoadCaches();

                        Log.d(TAG, "Caches Loaded");

                        //Update Complete No need to reload markers
                        placeholder.setPreferenceUpdate(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void RemoveAllCacheMarkers() {
        for (int i = 0; i < markerArrayList.size(); i++) {
            try {
                markerArrayList.get(i).remove();
            } catch (Exception e) {
                Log.d(TAG, "RemoveAllCacheMarkers: " + e.toString());
            }
        }
    }


    private void SetFoundIcon() {
        for (int i = 0; i < listCacheHistory.size(); i++) {
            String CacheID = listCacheHistory.get(i);
            for (int j = 0; j < markerArrayList.size(); j++) {
                if (markerArrayList.get(j).getTag().equals(CacheID)) {
                    Marker m = markerArrayList.get(j);
                    //Icon Settings
                    int height = 100;
                    int width = 100;
                    Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.map_cache_found);
                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                    BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);

                    m.setIcon(smallMarkerIcon);
                }
            }
        }
    }

    private void LoadCaches() {
        cacheArrayList = new ArrayList<>();
        markerArrayList = new ArrayList<>();
        DatabaseReference refCaches = FirebaseDatabase.getInstance().getReference("caches");

        refCaches.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cacheArrayList.clear();


                for (DataSnapshot snap : snapshot.getChildren()) {
                    Cache c = snap.getValue(Cache.class);

                    cacheArrayList.add(c);

                    if (c.getDifficulty().equals("easy")) {
                        if (placeholder.getEasy().equals("true")) {
                            AddCacheToMap(c);
                        }
                    }

                    if (c.getDifficulty().equals("normal")) {
                        if (placeholder.getNormal().equals("true")) {
                            AddCacheToMap(c);
                        }
                    }

                    if (c.getDifficulty().equals("hard")) {
                        if (placeholder.getHard().equals("true")) {
                            AddCacheToMap(c);
                        }
                    }
                }

                CachesLoaded = true;
                placeholder.setPreferenceUpdate(false);
                SetFoundIcon();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        placeholder.setCacheArrayList(cacheArrayList);
    }

    private void AddCacheToMap(Cache c) {
        //Icon size
        int height = 100;
        int width = 100;

        //Easy Default Icon
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.map_cache_normal);

        //Marker Object Creation
        LatLng cache_location = new LatLng(c.getLat(), c.getLon());
        MarkerOptions marker = new MarkerOptions().position(cache_location);


        //Check Difficulty
        switch (c.getDifficulty()) {
            case "easy":
                b = BitmapFactory.decodeResource(getResources(), R.drawable.map_cache_easy);
                break;
            case "normal":
                b = BitmapFactory.decodeResource(getResources(), R.drawable.map_cache_normal);
                break;
            case "hard":
                b = BitmapFactory.decodeResource(getResources(), R.drawable.map_cache_hard);
                break;
        }

        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);

        marker.icon(smallMarkerIcon);

        Marker cache_marker = mMap.addMarker(marker);
        Log.d(TAG, "CACHE ADDED: From MapsActivity");
        cache_marker.setTag(c.getId());

        markerArrayList.add(cache_marker);
    }


    //Marker on Click to load Details
    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        if (marker.getTag().toString().equals("MyLocation")) {

        } else {
            placeholder.setSelectedId("" + marker.getTag());
            placeholder.setPreviousFragment(null);

            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            OverlayFragment = new CacheDetailsFragment();
            transaction.replace(R.id.overlay_fragment, OverlayFragment);
            transaction.commit();
        }

        return false;
    }


}