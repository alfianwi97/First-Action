package com.tanbaron_coffee.myphonecall;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.tanbaron_coffee.myphonecall.MainActivity.fireStationLocation;
import static com.tanbaron_coffee.myphonecall.MainActivity.hospitalLocation;
import static com.tanbaron_coffee.myphonecall.MainActivity.policeLocation;
import static com.tanbaron_coffee.myphonecall.MainActivity.user;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener {
    private GoogleMap mMap;
    private Button buttonLogin;
    private Button buttonSchedule;
    private Button buttonCall;
    private Button buttonCallMenu;

    private String currentPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        buttonLogin = (Button) findViewById(R.id.btnLogin);
        buttonSchedule = (Button) findViewById(R.id.btnSchedule);
        buttonCall = (Button) findViewById(R.id.btnCallNumber);
        buttonCallMenu = (Button) findViewById(R.id.btnCall);
        currentPhoneNumber = null;
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
        mMap.setOnMarkerClickListener(this);
        CameraPosition cameraPosition;
        Integer iconWidth = 66;
        Integer iconHeight = 62;

//        Intent receiveIntent;
//        double longitude;
//        double latitude;
//        receiveIntent = this.getIntent();
//        longitude = receiveIntent.getDoubleExtra("longitude",0);
//        latitude = receiveIntent.getDoubleExtra("latitude",0);

        LatLng userLatLng = new LatLng(user.getCurrentLocation().getLatitude(),user.getCurrentLocation().getLongitude());

        //user location init
        mMap.addMarker(new MarkerOptions().position(userLatLng).title("Your Location"));

        //hospital location init
        for(int i=0;i<hospitalLocation.size();i++)
            mMap.addMarker(new MarkerOptions().position(new LatLng(hospitalLocation.get(i).getLatitude(),
                    hospitalLocation.get(i).getLongitude())).title(hospitalLocation.get(i).getName())
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap("hospital_logo",iconWidth,iconHeight))).flat(true)
                    .snippet(hospitalLocation.get(i).getPhoneNumber()));

        //police station location init
        for(int i=0;i<policeLocation.size();i++)
            mMap.addMarker(new MarkerOptions().position(new LatLng(policeLocation.get(i).getLatitude(),
                    policeLocation.get(i).getLongitude())).title(policeLocation.get(i).getName())
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap("police_logo",iconWidth,iconHeight))).flat(true)
                    .snippet(policeLocation.get(i).getPhoneNumber()));

        //fire station location init
        for(int i=0;i<fireStationLocation.size();i++)
            mMap.addMarker(new MarkerOptions().position(new LatLng(fireStationLocation.get(i).getLatitude(),
                    fireStationLocation.get(i).getLongitude())).title(fireStationLocation.get(i).getName())
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap("fire_station_logo",iconWidth,iconHeight))).flat(true)
                    .snippet(fireStationLocation.get(i).getPhoneNumber()));

        cameraPosition = new CameraPosition.Builder()
                .target(userLatLng)      // Sets the center of the map
                .zoom(13)                   // Sets the zoom
                .build();                   // Creates a CameraPosition from the builder
        //                .bearing(90)                // Sets the orientation of the camera to east
        //                .tilt(30)                   // Sets the tilt of the camera to 30 degrees

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MapsActivity.this, AccountActivity.class));
            }
        });
        buttonSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ScheduleActivity.class));
            }
        });
        buttonCallMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ScheduleActivity.class));
            }
        });

        buttonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                call();
            }
        });
    }

    private void call(){
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        String phoneNumber="tel:";

        if(currentPhoneNumber==null){
            phoneNumber+="112";
        }else{
            phoneNumber+=currentPhoneNumber;
        }
        callIntent.setData(Uri.parse(phoneNumber));
        startActivity(callIntent);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    private Bitmap resizeBitmap(String drawableName,int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(drawableName, "drawable", getPackageName()));
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }

    @Override
    public void onLocationChanged(Location location) {}
    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.getTitle() == "Your Location"){
            currentPhoneNumber = "112";
            return false;
        }
        currentPhoneNumber = marker.getSnippet();
        Toast.makeText(MapsActivity.this, marker.getSnippet(), Toast.LENGTH_LONG).show();
        return false;
    }
}