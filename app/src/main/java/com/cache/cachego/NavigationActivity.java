package com.cache.cachego;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

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

public class NavigationActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    LocationManager locationManager;
    private static final int GPS_TIME_INTERVAL = 50; // get GPS location every 1 min
    private static final int GPS_DISTANCE = 1000; // set distance value in meter
    private static final int HANDLER_DELAY = 50;
    private static final int START_HANDLER_DELAY = 0;
    final static String[] PERMISSION = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    final static int PERMISSION_ALL = 1;

    private GoogleMap mMap;
    private String TAG = "CACHEGO:";
    StaticFields sf = new StaticFields();
    Cache SelectedCache;
    LatLng source;
    LatLng destination;
    private Marker myLocation;
    private LatLng loc;
    private boolean locFound = false;
    Polyline pl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
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

        source = new LatLng(sf.getLat(), sf.getLon());

        GetSelectedCache();
    }

    private void GetSelectedCache() {
        SelectedCache = sf.getCacheArrayList().stream()
                .filter(cache -> sf.getSelectedId().equals(cache.getId()))
                .findAny()
                .orElse(null);

        LoadPolylines();
    }

    private boolean ReachedCache(LatLng myLocation, LatLng cache) {
        double maxX = cache.longitude + 0.0001250;
        double minX = cache.longitude - 0.0001250;

        double maxY = cache.latitude + 0.0001250;
        double minY = cache.latitude - 0.0001250;

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

        AddCacheToMap(SelectedCache);

        new GetPathFromLocation(source, destination, new DirectionPointListener() {
            @Override
            public void onPath(PolylineOptions polyLine) {
                pl = mMap.addPolyline(polyLine);
            }
        }, getString(R.string.MAPS_API_KEY)).execute();

        MoveMap();
    }

    private void MoveMap() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(source, 15.0f));
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

        Marker cache_marker = mMap.addMarker(marker);
        cache_marker.setTag(c.getId());
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {        
        try {
            myLocation.remove();
        } catch (Exception e) {

        }
        Log.d("mylog", "Got Location: " + location.getLatitude() + ", " + location.getLongitude());
        locationManager.removeUpdates(this);

        loc = new LatLng(location.getLatitude(), location.getLongitude());
        source = loc;
        MarkerOptions marker = new MarkerOptions().position(loc);

        if (ReachedCache(loc, destination)) {
            Toast.makeText(this, "You are very close to the cache!", Toast.LENGTH_SHORT).show();
        }

        sf.setLat(location.getLatitude());
        sf.setLon(location.getLongitude());

        //Icon Creation
        int height = 50;
        int width = 50;
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.map_user_icon);
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);

        //Set Marker Icon
        marker.icon(smallMarkerIcon);
        myLocation = mMap.addMarker(marker);
        String tag = "MyLocation";
        myLocation.setTag(tag);

        //Update Polyline
        new GetPathFromLocation(source, destination, new DirectionPointListener() {
            @Override
            public void onPath(PolylineOptions polyLine) {
                pl.remove();
                pl = mMap.addPolyline(polyLine);
            }
        }, getString(R.string.MAPS_API_KEY)).execute();

        if (locFound == false) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
            locFound = true;
        }
        else {
            GetDeclination(location);
        }
    }

    float mDeclination;
    private void GetDeclination(Location location) {
        GeomagneticField field = new GeomagneticField(
                (float)location.getLatitude(),
                (float)location.getLongitude(),
                (float)location.getAltitude(),
                System.currentTimeMillis()
        );

        // getDeclination returns degrees
        mDeclination = field.getDeclination();
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
}