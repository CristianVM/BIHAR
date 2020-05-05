package com.example.bihar.view.activities;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.example.bihar.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;

public class MapsUniversidad extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String universidad;
    private double latitud;
    private double longitud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_universidad);

        Bundle bundle = getIntent().getExtras();
        if(bundle !=null){
            universidad = bundle.getString("nombreUniversidad");
            latitud = Double.valueOf(bundle.getString("latitud"));
            longitud = Double.valueOf(bundle.getString("longitud"));
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //LOCALIZACION DE LA UNIVERSIDAD
        LatLng coordenadasRestaurante = new LatLng(latitud, longitud);
        mMap.addMarker(new MarkerOptions()
                        .position(coordenadasRestaurante)
                        .title(universidad));

        // Controles UI
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(false);

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordenadasRestaurante,15));
    }
}
