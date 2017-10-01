package com.tanbaron_coffee.myphonecall;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private Button buttonCallPolice;
    private Button buttonCallHospital;
    private Button buttonCallFireStation;
    private Button buttonLogin;
    private Button buttonMap;
    private Button buttonSchedule;
    private FusedLocationProviderClient mFusedLocationClient;

    public static ScheduleActivity.User user=new ScheduleActivity.User();
    public static List<LocationData> hospitalLocation;
    public static List<LocationData> policeLocation;
    public static List<LocationData> fireStationLocation;

    SettingsClient client;
    LocationSettingsRequest.Builder builder;
    private GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Task<LocationSettingsResponse> task;

    private ConnectionHandler connectionHandler;
    private GpsHandler gpsHandler;
    private RequestHandler requestHandler;
    private RequestQueue requestQueue;
    private RequestCreator requestCreator;
    private Gson gson;
    private GsonBuilder gsonBuilder;

    public static Boolean firstOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        buttonCallPolice = (Button) findViewById(R.id.btnPolice);
        buttonCallFireStation = (Button) findViewById(R.id.btnFireStation);
        buttonCallHospital = (Button) findViewById(R.id.btnHospital);
        buttonMap = (Button) findViewById(R.id.btnMap);
        buttonLogin = (Button) findViewById(R.id.btnLogin);
        buttonSchedule = (Button) findViewById(R.id.btnSchedule);

        connectionHandler = new ConnectionHandler(1000);
        connectionHandler.startRepeatingTask();

        gpsHandler = new GpsHandler(mFusedLocationClient, 3000);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);

        client = LocationServices.getSettingsClient(this);
        task = client.checkLocationSettings(builder.build());

        hospitalLocation = new ArrayList<LocationData>();
        policeLocation = new ArrayList<LocationData>();
        fireStationLocation = new ArrayList<LocationData>();

        requestCreator = new RequestCreator();
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestHandler = new RequestHandler(5000,5000,10000);
        requestHandler.startRepeatingTask();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat("M/d/yy hh:mm a");
        gson = gsonBuilder.create();
        mGoogleApiClient.connect();

        firstOpen = false;

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Toast.makeText(MainActivity.this, "All requirements satisfied", Toast.LENGTH_LONG).show();
                // All location settings are satisfied. The client can initialize
                // location requests here.
            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(MainActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });

        buttonCallFireStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                String phoneNumber="tel:";
                if(fireStationLocation.isEmpty()){
                    phoneNumber+="112";
                }else{
                    Collections.sort(fireStationLocation, new DistanceComparator());
                    phoneNumber+=fireStationLocation.get(0).getPhoneNumber();
                    Toast.makeText(MainActivity.this, phoneNumber+" "+fireStationLocation.get(0).getName(), Toast.LENGTH_LONG).show();
                }

                callIntent.setData(Uri.parse(phoneNumber));
                startActivity(callIntent);
            }
        });
        buttonCallHospital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                String phoneNumber="tel:";
                if(hospitalLocation.isEmpty()){
                    phoneNumber+="112";
                }else{
                    Collections.sort(hospitalLocation, new DistanceComparator());
                    phoneNumber+=hospitalLocation.get(0).getPhoneNumber();
                    Toast.makeText(MainActivity.this, phoneNumber+" "+hospitalLocation.get(0).getName(), Toast.LENGTH_LONG).show();
                }
                callIntent.setData(Uri.parse(phoneNumber));
                startActivity(callIntent);
            }
        });
        buttonCallPolice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                String phoneNumber="tel:";
                if(policeLocation.isEmpty()){
                    phoneNumber+="112";
                }else{
                    Collections.sort(policeLocation, new DistanceComparator());
                    phoneNumber+=policeLocation.get(0).getPhoneNumber();
                    Toast.makeText(MainActivity.this, phoneNumber+" "+policeLocation.get(0).getName(), Toast.LENGTH_LONG).show();
                }
                callIntent.setData(Uri.parse(phoneNumber));
                startActivity(callIntent);
            }
        });

        //////////////////////////////////////////Menu Button///////////////////////////////////////
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            startActivity(new Intent(getApplicationContext(), AccountActivity.class));
            }
        });
        buttonSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent scheduleIntent = new Intent(getApplicationContext(), ScheduleActivity.class);
                startActivity(scheduleIntent);
            }
        });
        buttonMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(user.isCurrentLocationEmpty()){
                Toast.makeText(MainActivity.this, "Failed to get location", Toast.LENGTH_LONG).show();
                return;
            }
            Intent mapIntent = new Intent(getApplicationContext(), MapsActivity.class);
            startActivity(mapIntent);
            }
        });
        /////////////////////////////////////////End of Menu Button/////////////////////////////////
    }

    @Override
    public void onDestroy() {
        connectionHandler.stopRepeatingTask();
        requestHandler.stopRepeatingTask();
        gpsHandler.stopRepeatingTask();
        super.onStop();
        super.onDestroy();
    }

    protected void onStart() {
        super.onStart();
        /////////////////////////////////Initiate GPS Handler///////////////////////////////////////
        if(!firstOpen){
            firstOpen=true;
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
            public void run() {
                gpsHandler.startRepeatingTask();
                }
            }, 500);
        }
        /////////////////////////////End of Initiate GPS Handler////////////////////////////////////
    }
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK){
            Toast.makeText(MainActivity.this, "System settings do not meet program requirements", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {}
    @Override
    public void onConnectionSuspended(int i) {}
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}


    public abstract class TaskHandler extends Handler{
        protected int timeInterval;
        protected int getTimeInterval(){return timeInterval;}
        protected void setTimeInterval(int timeInterval){this.timeInterval=timeInterval;}
        protected abstract void startRepeatingTask();
        protected abstract void stopRepeatingTask();
    }

    public class ConnectionHandler extends TaskHandler {
        private Boolean connectionStatus=true;
        private ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        private NetworkInfo activeNetwork;

        public ConnectionHandler(int timeInterval){this.timeInterval=timeInterval;}

        @Override
        public void startRepeatingTask() {
            connectionStatusChecking.run();
        }

        @Override
        public void stopRepeatingTask() {removeCallbacks(connectionStatusChecking);}

        public Boolean isConnected() {
            return connectionStatus;
        }

        Runnable connectionStatusChecking = new Runnable() {
            public void run() {
                try {
                    activeNetwork = connectivityManager.getActiveNetworkInfo();
                    connectionStatus = activeNetwork != null && activeNetwork.isConnected();
                } finally {
                    postDelayed(connectionStatusChecking, timeInterval);
                }
            }
        };

    }

    public class GpsHandler extends TaskHandler{
        private LocationData location = new LocationData("location");
        private Boolean gpsStatus;
        private DialogFragment gpsDialog= new GpsDialog();
        private FusedLocationProviderClient mFusedLocationClient;
        GpsHandler(FusedLocationProviderClient mFusedLocationClient, int timeInterval){
            this.timeInterval=timeInterval;
            this.mFusedLocationClient = mFusedLocationClient;
        }

        public Double getLongitude(){return location.getLongitude();}
        public Double getLatitude(){return location.getLatitude();}
        public Boolean isEnable() {
            return gpsStatus;
        }
        public Boolean isLatLongEmpty(){
            Double lat=getLatitude();
            Double lon=getLongitude();
            if(lat.compareTo(0.0)==0 || lon.compareTo(0.0)==0) return true;
         //   Toast.makeText(MainActivity.this, "DAMN!", Toast.LENGTH_LONG).show();
            return false;
        }
        public void setLongitude(double longitude){location.setLongitude(longitude);}
        public void setLatitude(double latitude){location.setLatitude(latitude);}

        public void gpsCheck() throws Settings.SettingNotFoundException {
            int off = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
            if (off == 0) {
                gpsStatus = false;
                } else gpsStatus = true;
            }

        public void promptGpsSetting() {
            Bundle args = new Bundle();
            args.putString(GpsDialog.ARG_TITLE, "GPS Setting");
            args.putString(GpsDialog.ARG_MESSAGE, getResources().getString(R.string.prompt_gps_setting));
            gpsDialog.setArguments(args);
            gpsDialog.setTargetFragment(gpsDialog, 1);
            gpsDialog.show(getFragmentManager(), "tag");
        }

        public void calculateLocation(){
            try {
                Location mLastLocation;
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) {
                    setLongitude(mLastLocation.getLongitude());
                    setLatitude(mLastLocation.getLatitude());
                    user.setCurrentLocation(new LocationData(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
//                }else{
//                    setLongitude(0);
//                    setLatitude(0);
                }
            }catch(SecurityException e) {}
        }
        public void findNation(){location.findNation();}

        @Override
        public void startRepeatingTask() { gpsCheckTask.run(); }

        @Override
        public void stopRepeatingTask() { removeCallbacks(gpsCheckTask); }

        Runnable gpsCheckTask = new Runnable() {
            public void run() {
                try {
                    gpsCheck();
                    if (connectionHandler.isConnected() && isEnable()){
                        calculateLocation();
                        if(!isLatLongEmpty())
                        findNation();
                    }
                } catch (Settings.SettingNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    postDelayed(gpsCheckTask, timeInterval);
                }
            }
        };

    }

    public class GpsDialog extends DialogFragment{
        public static final String ARG_TITLE = "GpsDialog.Title";
        public static final String ARG_MESSAGE = "GpsDialog.Message";

        public GpsDialog(){}
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            Bundle args = getArguments();
            String title = args.getString(ARG_TITLE);
            String message = args.getString(ARG_MESSAGE);

            return new AlertDialog.Builder(getActivity())
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("Setting", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            Intent onGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(onGPS);
                            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
                        }
                    })
                    .create();
        }
    }

    public class LocationData extends Location{
        private String id;
        private String type;
        private String nation;
        private String name;
        private String phoneNumber;
        private double distance;
        private void setDistance(){
            distance=Math.floor(this.distanceTo(user.getCurrentLocation())*100)/100;
        }
        public LocationData(String provider) {
            super(provider);
            setLatitude(0);
            setLongitude(0);
        }
        public LocationData(Double latitude, Double longitude) {
            super("location");
            setLatitude(latitude);
            setLongitude(longitude);
        }
        public LocationData(String provider, String id, String name, String type, String phoneNumber, Double latitude, Double longitude){
            super(provider);
            setId(id);
            setName(name);
            setType(type);
            setPhoneNumber(phoneNumber);
            setLatitude(latitude);
            setLongitude(longitude);
            setDistance();
        }

        public String getName(){return name;}
        public String getPhoneNumber(){return phoneNumber;}
        public Double getDistance(){return distance;}
        public String getType(){return type;}
        public String getId(){return id;}
        public String getNation(){return nation;}
        public void setName(String name){this.name=name;}
        public void setPhoneNumber(String phoneNumber){this.phoneNumber=phoneNumber;}

        public void setType(String type){this.type=type;}
        public void setId(String id){this.id=id;}
        public void setNation(String nation){this.nation=nation;}

        public void findNation(){
            Geocoder gcd = new Geocoder(MainActivity.this, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = gcd.getFromLocation(getLatitude(), getLongitude(), 1);
                if (addresses.size() > 0)
                {
                    String nationName=addresses.get(0).getCountryName();
                    setNation(nationName);
                    Log.i("Country Name",nationName);
                }
            } catch (IOException e) {
                setNation("Undefined");
                e.printStackTrace();
            }
        }
    }

    public class RequestCreator{
//        public String getFirstRequest(String latitude, String longitude, Integer radius, String pageToken){
//            if(pageToken==null) pageToken="";
//            return new String("https://maps.googleapis.com/maps/api/place/search/json?location="+latitude+","+longitude+"&hasNextPage=true&nextPage()=true&radius="+radius+"&types=hospital|police|fire_station&key=AIzaSyBrLe3fjpOvYhBRn3U9ypqeVfag3pgNQDY&pagetoken="+pageToken);
//        }
        public String getFirstRequest(Double latitude, Double longitude, Integer radius, String type, String pageToken){
            if(pageToken==null) pageToken="";
            return new String("https://maps.googleapis.com/maps/api/place/search/json?location="+latitude+","+longitude+"&hasNextPage=true&nextPage()=true&radius="+radius+"&types="+type+"&key=AIzaSyBrLe3fjpOvYhBRn3U9ypqeVfag3pgNQDY&pagetoken="+pageToken);
        }
        public String getSecondRequest(String placeId){return new String("https://maps.googleapis.com/maps/api/place/details/json?placeid="+placeId+"&key=AIzaSyBrLe3fjpOvYhBRn3U9ypqeVfag3pgNQDY");}
    }
//https://maps.googleapis.com/maps/api/place/search/json?location=-6.4653324,%20106.8598435&hasNextPage=true&nextPage()=true&radius=5000&types=fire_station&key=AIzaSyBrLe3fjpOvYhBRn3U9ypqeVfag3pgNQDY&pagetoken=

    public class RequestHandler extends TaskHandler{
        private final Integer totalRequestPackets=3;
        private String mainRequestUrl;
        private int requestCount = new Integer(0);
        private String status="idle";
//        private Integer placeDetected=0;
//        private Integer placeScanned=0;
//        private Integer errMsgCounter=0;
        private RequestPacket[] requestPackets=new RequestPacket[totalRequestPackets];

        RequestHandler(Integer timeInterval, Integer radius, Integer secondRadius){
            requestPackets[0] = new RequestPacket("hospital",radius,secondRadius);
            requestPackets[1] = new RequestPacket("police",radius,secondRadius);
            requestPackets[2] = new RequestPacket("fire_station",radius,secondRadius);
            this.timeInterval=timeInterval;
        }
        RequestHandler(String mainRequestUrl){this.mainRequestUrl=mainRequestUrl;}

        public String getMainRequestUrl(){return mainRequestUrl;}
        public void setMainRequestUrl(String url){mainRequestUrl=url;}

        @Override
        public void startRepeatingTask() { sendRequest.run(); }

        @Override
        public void stopRepeatingTask() { removeCallbacks(sendRequest);}

        Runnable sendRequest = new Runnable() {
            @Override
            public void run() {
            if(status.equals("finish")){
                postDelayed(sendRequest,timeInterval);
                return;
            }
            Integer requestDone=0;
            Integer requestWork=0;
            for(int i=0;i<totalRequestPackets;i++){
                if(!requestPackets[i].getStatus().equals("idle")){
                    requestWork++;
                    if(requestPackets[i].getPlaceDetected()==requestPackets[i].getPlaceScanned()){
                        requestPackets[i].setStatus("done");
                        requestDone++;
                    }
                }
            }
            if(requestDone==totalRequestPackets) status="done";
                else status="work";
            if(!connectionHandler.isConnected() || gpsHandler.isLatLongEmpty() || requestWork==totalRequestPackets) {
                if(status.equals("done")){
                    Toast.makeText(MainActivity.this, "Program ready to use", Toast.LENGTH_LONG).show();
                    status="finish";
                }

                postDelayed(sendRequest,timeInterval);
                return;
            }
            Toast.makeText(MainActivity.this, "Scanning nearby locations", Toast.LENGTH_LONG).show();
            for(int i=0;i<totalRequestPackets;i++)
                if(requestPackets[i].getStatus().equals("idle"))
                    requestPackets[i].postRequest();

            postDelayed(sendRequest,timeInterval/5);
            }
        };
    }

    public class RequestPacket extends Handler{
        private String type;
        private String mainRequestUrl;
        private String status="idle";
        private Integer radius;
        private Integer secondRadius;
        private Integer placeDetected=0;
        private Integer placeScanned=0;

        RequestPacket(String type, Integer radius, Integer secondRadius){
            this.type=type;
            this.radius=radius;
            this.secondRadius=secondRadius;
        }

        public Integer getPlaceDetected() {return placeDetected;}
        public Integer getPlaceScanned() {return placeScanned;}
        public String getMainRequestUrl(){return mainRequestUrl;}
        public String getType() {return type;}
        public String getStatus() {return status;}
        public Integer getRadius() {return radius;}

        public void setPlaceDetected(Integer placeDetected) {this.placeDetected = placeDetected;}
        public void setPlaceScanned(Integer placeScanned) {this.placeScanned = placeScanned;}
        public void setMainRequestUrl(String mainRequestUrl) {this.mainRequestUrl = mainRequestUrl;}
        public void setType(String type) {this.type = type;}
        public void setStatus(String status) {this.status = status;}
        public void setRadius(Integer radius) {this.radius = radius;}

        public void postRequest(){
            mainRequestUrl=requestCreator.getFirstRequest(gpsHandler.getLatitude(),gpsHandler.getLongitude(),radius,type,null);
            sendFirstRequest();
        }

        public void sendFirstRequest(){
            if(status.equals("idle")) status="requesting";
            StringRequest request = new StringRequest(Request.Method.GET, mainRequestUrl, onPostsLoaded1, onPostsError);
            requestQueue.add(request);
        }
        private void sendSecondRequest(String url) {
            StringRequest request = new StringRequest(Request.Method.GET, url, onPostsLoaded2, onPostsError);
            requestQueue.add(request);
        }

        Runnable sendAdditionalRequest = new Runnable() {
            @Override
            public void run() {
                sendFirstRequest();
            }
        };

        private final Response.Listener<String> onPostsLoaded1 = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ResponseClass res = null;
                res = gson.fromJson(response, ResponseClass.class);
                if(res==null || !res.status.equals("OK")){
                    if(res.status.equals("ZERO_RESULTS")){
                        if(radius==secondRadius){
                            status="request done";
                            return;
                        }
                        radius=secondRadius;
//                        Toast.makeText(MainActivity.this, "Latlong:"+gpsHandler.getLatitude()+":"+gpsHandler.getLongitude()+" | rad:"+radius, Toast.LENGTH_LONG).show();
                        Toast.makeText(MainActivity.this,mainRequestUrl, Toast.LENGTH_LONG).show();
                    }
//                    else{
//                        errMsgCounter++;
//                    }
//                    if(errMsgCounter==6){
//                        errMsgCounter=0;
//                        Toast.makeText(MainActivity.this, "Failed to get location", Toast.LENGTH_LONG).show();
//                    }
                    status="idle";
                    return;
                }
                if(status.equals("requesting")){
//                    Toast.makeText(MainActivity.this, "Scanning nearby locations", Toast.LENGTH_LONG).show();
                    status="scanning";
                }

                placeDetected += res.results.size();
                for(int i=0; i<res.results.size(); i++){
                    sendSecondRequest(requestCreator.getSecondRequest(res.results.get(i).place_id));
                }
                if(res.pageToken!=null){
                    mainRequestUrl=requestCreator.getFirstRequest(gpsHandler.getLatitude(),gpsHandler.getLongitude(),radius,type,res.pageToken);
                    postDelayed(sendAdditionalRequest,3000);
                }else{
                    status="request done";
                }
            }
        };

        private final Response.Listener<String> onPostsLoaded2 = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ResponseClass2 res = null;
                res = gson.fromJson(response, ResponseClass2.class);
                if(res==null || !res.status.equals("OK")){
                    placeScanned++;
                    return;
                }
                if(res.result.phoneNumber==null || res.result.phoneNumber.isEmpty()){
                    placeScanned++;
                    return;
                }
                String id, name, type, phoneNumber, phoneNumberTemp;
                Double latitude, longitude;
                id = res.result.place_id;
                name = res.result.name;
                type = res.result.types.get(0);
                phoneNumberTemp = res.result.phoneNumber;
                phoneNumber="";
                for(int i=0; i< phoneNumberTemp.length(); i++){
                    if(phoneNumberTemp.charAt(i)=='-' || phoneNumberTemp.charAt(i)==')' || phoneNumberTemp.charAt(i)=='(' || phoneNumberTemp.charAt(i)==' ') continue;
                    phoneNumber += phoneNumberTemp.charAt(i);
                }
                latitude = res.result.geometry.location.lat;
                longitude = res.result.geometry.location.lng;

                switch(res.result.types.get(0)){
                    case "hospital":
                        hospitalLocation.add(new LocationData("hospital", id, name, type, phoneNumber, latitude, longitude));
                        break;
                    case "police":
                        policeLocation.add(new LocationData("police", id, name, type, phoneNumber, latitude, longitude));
                        break;
                    case "fire_station":
                        fireStationLocation.add(new LocationData("fire station", id, name, type, phoneNumber, latitude, longitude));
                        break;
                    default:
                        break;
                }
                placeScanned++;
            }
        };

        private final Response.ErrorListener onPostsError = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(MainActivity.class.getSimpleName(), error.toString());
            }
        };
    }

    ///////////////////////////////////Array Comparator Class///////////////////////////////////////
    public class DistanceComparator implements Comparator<LocationData>{
        @Override
        public int compare(LocationData first, LocationData second) {
            return first.getDistance().compareTo(second.distance);
        }
    }
    ////////////////////////////////End of Array Comparator Class///////////////////////////////////
    //////////////////////////////////////JSON Class - 1////////////////////////////////////////////
    public class ResponseClass{
        List<Results> results;
        String status;

        @SerializedName("next_page_token")
        String pageToken=null;
    }
    public class Results{
        @SerializedName("place_id")
        String place_id=null;
    }
    ///////////////////////////////////End of JSON Class - 1////////////////////////////////////////
    //////////////////////////////////////JSON Class - 2////////////////////////////////////////////
    public class ResponseClass2{
        Results2 result;
        String status;
    }
    public class Results2{
        @SerializedName("formatted_phone_number")
        String phoneNumber;

        List<String> types;
        String place_id;
        String name;

        Geometry geometry;
    }
    public class Geometry{
        LocationClass location;
    }
    public class LocationClass{
        double lat;
        double lng;
    }
    ///////////////////////////////////End of JSON Class - 2////////////////////////////////////////
}