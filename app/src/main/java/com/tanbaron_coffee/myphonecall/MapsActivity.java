package com.tanbaron_coffee.myphonecall;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.tanbaron_coffee.myphonecall.MainActivity.fireStationLocation;
import static com.tanbaron_coffee.myphonecall.MainActivity.hospitalLocation;
import static com.tanbaron_coffee.myphonecall.MainActivity.policeLocation;
import static com.tanbaron_coffee.myphonecall.MainActivity.user;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
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
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        double longitude;
        double latitude;
        CameraPosition cameraPosition;
        LatLng location;
        Intent receiveIntent;

        receiveIntent = this.getIntent();
        longitude = receiveIntent.getDoubleExtra("longitude",0);
        latitude = receiveIntent.getDoubleExtra("latitude",0);
        location = new LatLng(latitude,longitude);

        //user location init
        mMap.addMarker(new MarkerOptions().position(new LatLng(user.getCurrentLocation().getLatitude(),user.getCurrentLocation().getLongitude())).title("Your Location"));

        //hospital location init
        for(int i=0;i<hospitalLocation.size();i++)
            mMap.addMarker(new MarkerOptions().position(new LatLng(hospitalLocation.get(i).getLatitude(),
                    hospitalLocation.get(i).getLongitude())).title(hospitalLocation.get(i).getName()));

        //police station location init
        for(int i=0;i<policeLocation.size();i++)
            mMap.addMarker(new MarkerOptions().position(new LatLng(policeLocation.get(i).getLatitude(),
                    policeLocation.get(i).getLongitude())).title(policeLocation.get(i).getName()));

        //fire station location init
        for(int i=0;i<fireStationLocation.size();i++)
            mMap.addMarker(new MarkerOptions().position(new LatLng(fireStationLocation.get(i).getLatitude(),
                    fireStationLocation.get(i).getLongitude())).title(fireStationLocation.get(i).getName()));

        cameraPosition = new CameraPosition.Builder()
                .target(location)      // Sets the center of the map
                .zoom(13)                   // Sets the zoom
                .build();                   // Creates a CameraPosition from the builder
        //                .bearing(90)                // Sets the orientation of the camera to east
        //                .tilt(30)                   // Sets the tilt of the camera to 30 degrees

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

}