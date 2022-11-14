package com.cache.cachego;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.cache.cachego.databinding.FragmentNavigationBinding;
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
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.util.List;

public class NavigationFragment extends Fragment implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener {

    LocationManager locationManager;
    private static final int GPS_TIME_INTERVAL = 50; // get GPS location every 1 min
    private static final int GPS_DISTANCE = 1000; // set distance value in meter
    private static final int HANDLER_DELAY = 50;
    private static final int START_HANDLER_DELAY = 0;
    final static String[] PERMISSION = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    final static int PERMISSION_ALL = 1;

    private GoogleMap NavMap;
    private String TAG = "CACHEGO:";
    StaticFields sf = new StaticFields();
    Cache SelectedCache;
    LatLng source;
    LatLng destination;
    private Marker myLocation;
    private LatLng loc;
    private boolean locFound = false;
    Polyline pl;
    private boolean CacheFound = false;
    private boolean CacheAdded = false;
    private int MapCounter = 0;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            NavMap = googleMap;

            NavMap.getUiSettings().setMapToolbarEnabled(false);
            NavMap.getUiSettings().setZoomControlsEnabled(false);

            source = new LatLng(sf.getLat(), sf.getLon());

            GetSelectedCache();
        }
    };

    private FragmentNavigationBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentNavigationBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getParentFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Location Updating
        locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                requestLocation();
                handler.postDelayed(this, HANDLER_DELAY);
            }
        }, START_HANDLER_DELAY);

        binding.btnRelocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    NavMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
                } catch (Exception e) {

                }
            }
        });

        binding.btnExitNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getParentFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();

                transaction.remove(NavigationFragment.this);
                transaction.commit();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    private void GetSelectedCache() {
        SelectedCache = sf.getCacheArrayList().stream()
                .filter(cache -> sf.getSelectedId().equals(cache.getId()))
                .findAny()
                .orElse(null);

        AddCacheToMap(SelectedCache);
        LoadPolylines();
    }

    private boolean ReachedCache(LatLng myLocation, LatLng cache) {
        double maxX = cache.longitude + 0.0002000;
        double minX = cache.longitude - 0.0002000;

        double maxY = cache.latitude + 0.0002000;
        double minY = cache.latitude - 0.0002000;

        double myX = myLocation.longitude;
        double myY = myLocation.latitude;

        boolean inX = false;
        boolean inY = false;

        if (myX >= minX && myX <= maxX) {
            inX = true;
        }

        if (myY >= minY && myY <= maxY) {
            inY = true;
        }

        return (inX && inY);
    }

    private void LoadPolylines() {
        destination = new LatLng(SelectedCache.getLat(), SelectedCache.getLon());

        new GetPathFromLocation(source, destination, new DirectionPointListener() {
            @Override
            public void onPath(PolylineOptions polyLine) {
                try {
                    pl.remove();
                    Log.d(TAG, "POLYLINE: REMOVED");
                } catch (Exception e) {
                    Log.d(TAG, "POLYLINE: NONE");
                }

                pl = NavMap.addPolyline(polyLine);
                Log.d(TAG, "POLYLINE: ADDED");
            }
        }, getString(R.string.MAPS_API_KEY)).execute();

        MoveMap();
    }

    private void MoveMap() {
        NavMap.moveCamera(CameraUpdateFactory.newLatLngZoom(source, 15.0f));
    }

    private void AddCacheToMap(Cache c) {
        //Icon size
        int height = 100;
        int width = 100;

        //Easy Default Icon
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.map_cache_easy);

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

        if (MapCounter == 1) {
            Marker cache_marker = NavMap.addMarker(marker);
            Log.d(TAG, "CACHE ADDED: From NavigationActivity");
            cache_marker.setTag(c.getId());
        }

        MapCounter++;
    }

    private boolean LocationReachedNotify = false;
    @Override
    public void onLocationChanged(@NonNull Location location) {
        try {
            myLocation.remove();
        } catch (Exception e) {

        }

        try {
            Log.d("mylog", "Got Location: " + location.getLatitude() + ", " + location.getLongitude());
            locationManager.removeUpdates(this);

            loc = new LatLng(location.getLatitude(), location.getLongitude());
            source = loc;
            MarkerOptions marker = new MarkerOptions().position(loc);

            //When user is close to cache
            if (ReachedCache(loc, destination)) {
                //Show Popup of Cache Details
                if (LocationReachedNotify == true) {
                    Log.d(TAG, "REACHED: NOTIFICATION HAS BEEN DISPLAYED");
                } else {
                    //Display navCache Details
                    FragmentManager manager = getParentFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();

                    Fragment OverlayFragment = new CacheReachedFragment();
                    transaction.replace(R.id.overlay_fragment, OverlayFragment);
                    transaction.commit();
                    LocationReachedNotify = true;

                    Log.d(TAG, "REACHED: DISPLAYING NOTIFICATION");
                }
                Log.d(TAG, "REACHED: HAVE REACHED DESTINATION");
            } else {
                Log.d(TAG, "REACHED: HAVE NOT REACHED DESTINATION");
            }

            sf.setLat(location.getLatitude());
            sf.setLon(location.getLongitude());

            //Icon Creation
            int height = 50;
            int width = 50;
            Bitmap b = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.map_user_icon);
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
            BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);

            //Set Marker Icon
            marker.icon(smallMarkerIcon);
            myLocation = NavMap.addMarker(marker);
            String tag = "MyLocation";
            myLocation.setTag(tag);

            //Update Polyline
            new GetPathFromLocation(source, destination, new DirectionPointListener() {
                @Override
                public void onPath(PolylineOptions polyLine) {
                    pl.remove();
                    Log.d(TAG, "POLYLINE: REMOVED");
                    pl = NavMap.addPolyline(polyLine);
                    Log.d(TAG, "POLYLINE: ADDED");
                }
            }, getString(R.string.MAPS_API_KEY)).execute();

            if (locFound == false) {
                NavMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
                locFound = true;
            } else {
                GetDeclination(location);
            }

            DecimalFormat df = new DecimalFormat("0.00");
            NavDetails nd = new NavDetails();
            double distance = nd.getDistanceMeters();

            String ms = sf.getMetricSystem();
            if (ms.equals("metric")) {
                //If Metric System
                if (nd.getDistanceMeters() >= 1000) {
                    distance = distance / 1000;
                    binding.tvDistance.setText("Distance: " + df.format(distance) + " kilometres");
                } else {
                    binding.tvDistance.setText("Distance: " + df.format(distance) + " meters");
                    Log.d(TAG, "Distance & Time: " + nd.getDistanceMeters());
                }
            } else {
                //If Imperial System
                if (nd.getDistanceMeters() >= 1000) {
                    distance = distance / 1000;
                    distance = distance / 1.6;
                    binding.tvDistance.setText("Distance: " + df.format(distance) + " miles");
                } else {
                    distance = distance * 1.09361;
                    binding.tvDistance.setText("Distance: " + df.format(distance) + " yards");
                    Log.d(TAG, "Distance & Time: " + nd.getDistanceMeters());
                }
            }


            binding.tvCacheName.setText(SelectedCache.getName());
            binding.tvDuration.setText("Duration: " + nd.getDuration());
        } catch (Exception e) {

        }
    }

    //Marker on Click to load Details
    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        if (marker.getTag().toString().equals("MyLocation")) {

        } else {
            StaticFields placeholder = new StaticFields();
            placeholder.setSelectedId("" + marker.getTag());

            FragmentManager manager = getParentFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            Fragment OverlayFragment = new CacheReachedFragment();
            transaction.replace(R.id.overlay_fragment, OverlayFragment);
            transaction.show(OverlayFragment);
            transaction.commit();
        }

        return false;
    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);
    }

    float mDeclination;

    private void GetDeclination(Location location) {
        GeomagneticField field = new GeomagneticField(
                (float) location.getLatitude(),
                (float) location.getLongitude(),
                (float) location.getAltitude(),
                System.currentTimeMillis()
        );

        // getDeclination returns degrees
        mDeclination = field.getDeclination();
    }

    private boolean HasPermission = false;

    @SuppressLint("MissingPermission")
    private void requestLocation() {
        if (locationManager == null)
            locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (HasPermission) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_TIME_INTERVAL, GPS_DISTANCE, NavigationFragment.this);
            } else {
                if (ActivityCompat.checkSelfPermission(getContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(getContext(),
                                Manifest.permission.ACCESS_COARSE_LOCATION) ==
                                PackageManager.PERMISSION_GRANTED) {
                    HasPermission = true;
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_TIME_INTERVAL, GPS_DISTANCE, NavigationFragment.this);
                }
            }

        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        NavMap = googleMap;

        source = new LatLng(sf.getLat(), sf.getLon());

        GetSelectedCache();
    }
}