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
import android.widget.TextView;
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
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
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
import java.util.List;
import java.util.Locale;

//class HelloAccessoryConsumer extends SAAgent {
//
//    protected HelloAccessoryConsumer(String s, Class<? extends SASocket> aClass) {
//        super(s, aClass);
//    }
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//}

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private Button callPoliceButton;
    private Button callHospitalButton;
    private Button callFireStationButton;
    private Button loginButton;
    private Button mapButton;

    private FusedLocationProviderClient mFusedLocationClient;
//    TextView statusTextField;
    TextView resultTextField;

    SettingsClient client;
    LocationSettingsRequest.Builder builder;
    private GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Task<LocationSettingsResponse> task;
    PendingResult<LocationSettingsResult> result;

    List<LocationData> hospitalLocation;
    List<LocationData> policeLocation;
    List<LocationData> fireStationLocation;

    ConnectionHandler connectionHandler;
    GpsHandler gpsHandler;
    RequestHandler requestHandler;
    private RequestQueue requestQueue;
    private RequestCreator requestCreator;
    private Gson gson;
    GsonBuilder gsonBuilder;
    //Integer requestCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        callPoliceButton = (Button) findViewById(R.id.btnPolice);
        callFireStationButton = (Button) findViewById(R.id.btnFireStation);
        callHospitalButton = (Button) findViewById(R.id.btnHospital);
        mapButton = (Button) findViewById(R.id.btnMap);
        loginButton = (Button) findViewById(R.id.btnLogin);
//        statusTextField = (TextView) findViewById(R.id.status);
        resultTextField = (TextView) findViewById(R.id.resultField);

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
        requestHandler = new RequestHandler(10000);
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
//        String stringUrl1 = "https://maps.googleapis.com/maps/api/place/search/json?location=-6.456341,106.845621&radius=1500&types=hospital&sensor=true&key=AIzaSyBrLe3fjpOvYhBRn3U9ypqeVfag3pgNQDY";
//        String stringUrl2 = "https://maps.googleapis.com/maps/api/place/details/json?placeid=ChIJBxt4xn_raS4REaNHge4v1KA&key=AIzaSyBrLe3fjpOvYhBRn3U9ypqeVfag3pgNQDY";

//
//        HttpURLConnection httpconn = null;
//        try {
//            URL url = new URL(stringUrl);
//            httpconn = (HttpURLConnection)url.openConnection();
//            if (httpconn.getResponseCode() == HttpURLConnection.HTTP_OK)
//            {
//                BufferedReader input = new BufferedReader(new InputStreamReader(httpconn.getInputStream()),8192);
//                String strLine = null;
//                while ((strLine = input.readLine()) != null)
//                {
//                    statusTextField.append(strLine);
//                }
//                input.close();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        String jsonOutput = statusTextField.toString();
//        Log.i("RESULT IS",jsonOutput);
        //
//        gps = new GPSTracker(this);
//        double latitude=5;
//        double longitude=5;
//        if (gps.canGetLocation()) {
//
//            latitude = gps.getLatitude();
//            longitude = gps.getLongitude();
//
//            // Toast.makeText(
//            // getApplicationContext(),
//            // "Your Location is - \nLat: " + latitude + "\nLong: "
//            // + longitude, Toast.LENGTH_LONG).show();
//        } else {
//            gps.showSettingsAlert();
//        }

//        Log.e("latlong", "" + latitude + "" + longitude);

        //
//        task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
//            @Override
//            public void onComplete(Task<LocationSettingsResponse> task) {
//                try {
//                    LocationSettingsResponse response = task.getResult(ApiException.class);
//                    // All location settings are satisfied. The client can initialize location
//                    // requests here
//                } catch (ApiException exception) {
//                    switch (exception.getStatusCode()) {
//                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                            // Location settings are not satisfied. But could be fixed by showing the
//                            // user a dialog.
//                            try {
//                                // Cast to a resolvable exception.
//                                ResolvableApiException resolvable = (ResolvableApiException) exception;
//                                // Show the dialog by calling startResolutionForResult(),
//                                // and check the result in onActivityResult().
//                                resolvable.startResolutionForResult(
//                                        MainActivity.this,
//                                        REQUEST_CHECK_SETTINGS);
//                            } catch (IntentSender.SendIntentException e) {
//                                // Ignore the error.
//                            } catch (ClassCastException e) {
//                                // Ignore, should be an impossible error.
//                            }
//                            break;
//                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                            // Location settings are not satisfied. However, we have no way to fix the
//                            // settings so we won't show the dialog.
//                            break;
//                    }
//                }
//            }
//        });

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
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




//        googleApiClient = null;
//        if (googleApiClient == null) {
//            googleApiClient = new GoogleApiClient.Builder(MainActivity.this)
//                    .addApi(LocationServices.API)
//                    .addConnectionCallbacks(MainActivity.this)
//                    .addOnConnectionFailedListener(MainActivity.this).build();
//            googleApiClient.connect();
//
//            locationRequest = LocationRequest.create();
//            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//            locationRequest.setInterval(30 * 1000);
//            locationRequest.setFastestInterval(5 * 1000);
//            builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
//
//            //**************************
//            builder.setAlwaysShow(true); //this is the key ingredient
//            //**************************
//
//            result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
//            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
//                @Override
//                public void onResult(LocationSettingsResult result) {
//                    final Status status = result.getStatus();
//                    final LocationSettingsStates state = result.getLocationSettingsStates();
//                    switch (status.getStatusCode()) {
//                        case LocationSettingsStatusCodes.SUCCESS:
//                            // All location settings are satisfied. The client can initialize location
//                            // requests here.
//                            break;
//                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                            // Location settings are not satisfied. But could be fixed by showing the user
//                            // a dialog.
//                            try {
//                                // Show the dialog by calling startResolutionForResult(),
//                                // and check the result in onActivityResult().
//                                status.startResolutionForResult(MainActivity.this, 1000);
//                            } catch (IntentSender.SendIntentException e) {
//                                // Ignore the error.
//                            }
//                            break;
//                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                            // Location settings are not satisfied. However, we have no way to fix the
//                            // settings so we won't show the dialog.
//                            break;
//                    }
//                }
//            });             }

        //
//        if (connectionCheckerHandler.connectionChecker.isConnected())
//            try {
//                gpsHandler.gpsCheck();
//                if (!gpsHandler.isEnable()) gpsHandler.promptGpsSetting();
//            } catch (Settings.SettingNotFoundException e) {
//                e.printStackTrace();
//            }

//        Bundle args = new Bundle();
//        args.putString(GpsDialog.ARG_TITLE, "GPS Dialog");
//        args.putString(GpsDialog.ARG_MESSAGE, "GPS set");
//        dialog.setArguments(args);
//        dialog.setTargetFragment(dialog, 1);
//        dialog.show(getFragmentManager(), "tag");
        //      connectionChecking = new ConnectionChecking();
        //      connectionChecking.execute(true);

//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    gpsHandler.gpsCheck();
//                    if (connectionHandler.isConnected() && gpsHandler.isEnable()){
//                        gpsHandler.calculateLocation();
//                        gpsHandler.findNation();
//                        requestHandler.setMainRequestUrl(requestCreator.getFirstRequest(Double.toString(gpsHandler.getLatitude()), Double.toString(gpsHandler.getLongitude())));
//                        resultTextField.setText(requestCreator.getFirstRequest(Double.toString(gpsHandler.getLatitude()), Double.toString(gpsHandler.getLongitude()))+" "+gpsHandler.getLatitude());
//                        ///////////////////////////////////////////////////
////                        GsonBuilder gsonBuilder = new GsonBuilder();
////                        gsonBuilder.setDateFormat("M/d/yy hh:mm a");
////                        gson = gsonBuilder.create();
////                        requestQueue = Volley.newRequestQueue(getApplicationContext());
////                        fetchPosts1(requestCreator.getFirstRequest(String.valueOf(gpsHandler.getLatitude()), String.valueOf(gpsHandler.getLongitude())));
//                    }
//                } catch (Settings.SettingNotFoundException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, 500);
        callFireStationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                String phoneNumber="tel:";

                resultTextField.setText(" HOS:"+hospitalLocation.size()+"|POL:"+policeLocation.size()+"|FS:"+fireStationLocation.size());
                for(int i=0;i<fireStationLocation.size();i++)
                    resultTextField.setText(resultTextField.getText()+fireStationLocation.get(i).getName()+"|"+fireStationLocation.get(i).getPhoneNumber()+"|");

                if(fireStationLocation.isEmpty()){
                    phoneNumber+="112";
                }else{
                    phoneNumber+=fireStationLocation.get(0).getPhoneNumber();
                    Toast.makeText(MainActivity.this, phoneNumber+" "+fireStationLocation.get(0).getName(), Toast.LENGTH_LONG).show();
                }

                callIntent.setData(Uri.parse(phoneNumber));
                startActivity(callIntent);
            }
        });
        callHospitalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                String phoneNumber="tel:";

                resultTextField.setText(" HOS:"+hospitalLocation.size()+"|POL:"+policeLocation.size()+"|FS:"+fireStationLocation.size());
                for(int i=0;i<hospitalLocation.size();i++)
                    resultTextField.setText(resultTextField.getText()+hospitalLocation.get(i).getName()+"|"+hospitalLocation.get(i).getPhoneNumber()+"|");

                if(hospitalLocation.isEmpty()){
                    phoneNumber+="112";
                }else{
                    phoneNumber+=hospitalLocation.get(0).getPhoneNumber();
                    Toast.makeText(MainActivity.this, phoneNumber+" "+hospitalLocation.get(0).getName(), Toast.LENGTH_LONG).show();
                }
                callIntent.setData(Uri.parse(phoneNumber));
                startActivity(callIntent);
            }
        });
        callPoliceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                String phoneNumber="tel:";

                resultTextField.setText(" HOS:"+hospitalLocation.size()+"|POL:"+policeLocation.size()+"|FS:"+fireStationLocation.size());
                for(int i=0;i<policeLocation.size();i++)
                    resultTextField.setText(resultTextField.getText()+policeLocation.get(i).getName()+"|"+policeLocation.get(i).getPhoneNumber()+"|");

                if(policeLocation.isEmpty()){
                    phoneNumber+="112";
                }else{
                    phoneNumber+=policeLocation.get(0).getPhoneNumber();
                    Toast.makeText(MainActivity.this, phoneNumber+" "+policeLocation.get(0).getName(), Toast.LENGTH_LONG).show();
                }
                callIntent.setData(Uri.parse(phoneNumber));
                startActivity(callIntent);
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AccountActivity.class));
            }
        });
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!connectionHandler.isConnected()) return; //alert
                try {
                    gpsHandler.gpsCheck();
                    if (!gpsHandler.isEnable()) gpsHandler.promptGpsSetting();
                } catch (Settings.SettingNotFoundException e) {
                    e.printStackTrace();
                }
                if (!gpsHandler.isEnable()) return; //show alert
                if (connectionHandler.isConnected() && gpsHandler.isEnable()){
                    gpsHandler.calculateLocation();
                    gpsHandler.findNation();
                }

//                requestQueue = Volley.newRequestQueue(getApplicationContext());
//                fetchPosts1(requestCreator.getFirstRequest(String.valueOf(gpsHandler.getLatitude()), String.valueOf(gpsHandler.getLongitude())));
//        fetchPosts2(stringUrl2);
                ///////////////////////////////////////////////////////////////////


                Intent mapIntent = new Intent(getApplicationContext(), MapsActivity.class);
                mapIntent.putExtra("longitude",gpsHandler.getLongitude());
                mapIntent.putExtra("latitude",gpsHandler.getLatitude());
//                Log.i("longlat2",gpsHandler.getLongitude()+" "+gpsHandler.getLatitude());
                startActivity(mapIntent);
            }
        });
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
        final Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                gpsHandler.startRepeatingTask();
            }
        }, 500);

    }
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
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

        public double getLongitude(){return location.getLongitude();}
        public double getLatitude(){return location.getLatitude();}
        public Boolean isEnable() {
            return gpsStatus;
        }
        public Boolean isLatLongEmpty(){
            if(new Double(getLatitude()).equals(0) || new Double(getLongitude()).equals(0)) return true;
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
    //                Toast.makeText(MainActivity.this, Double.toString(getLatitude())+" , "+Double.toString(getLongitude()), Toast.LENGTH_LONG).show();///////////////////////////////////////////////////////////////
                } else Toast.makeText(MainActivity.this, "null", Toast.LENGTH_LONG).show();///////////////////////////////////////////////////////////////
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
        public LocationData(String provider) {
            super(provider);
        }
        public LocationData(String provider, String id){super(provider); setId(id);}
        public LocationData(String provider, String id, String name, String type, String phoneNumber){
            super(provider);
            setId(id);
            setName(name);
            setType(type);
            setPhoneNumber(phoneNumber);
        }

        public String getName(){return name;}
        public String getPhoneNumber(){return phoneNumber;}
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
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addresses.size() > 0)
            {
                String nationName=addresses.get(0).getCountryName();
                setNation(nationName);
                Log.i("Country Name",nationName);
            }
        }

    }

    public class RequestCreator{
        public String getFirstRequest(String latitude, String longitude, String pageToken){
            if(pageToken==null) pageToken="";
            return new String("https://maps.googleapis.com/maps/api/place/search/json?location="+latitude+","+longitude+"&hasNextPage=true&nextPage()=true&radius=5000&types=hospital|police|fire_station&sensor=true&key=AIzaSyBrLe3fjpOvYhBRn3U9ypqeVfag3pgNQDY&pagetoken="+pageToken);
        }
        public String getSecondRequest(String placeId){return new String("https://maps.googleapis.com/maps/api/place/details/json?placeid="+placeId+"&key=AIzaSyBrLe3fjpOvYhBRn3U9ypqeVfag3pgNQDY");}
    }

////////////////////////Under Construct//////////////////////////////
    public class RequestHandler extends TaskHandler{
    private String mainRequestUrl;
    private int requestCount = new Integer(0);
    private String status="idle";

    RequestHandler(int timeInterval){this.timeInterval=timeInterval;}
    RequestHandler(String mainRequestUrl){this.mainRequestUrl=mainRequestUrl;}

    public String getMainRequestUrl(){return mainRequestUrl;}
    public void setMainRequestUrl(String url){mainRequestUrl=url;}

    @Override
    public void startRepeatingTask() { sendRequest.run(); }

    @Override
    public void stopRepeatingTask() { removeCallbacks(sendRequest);
        Toast.makeText(MainActivity.this, "stop", Toast.LENGTH_LONG).show();
    }

    private void sendFirstRequest(){
        status="work";
        StringRequest request = new StringRequest(Request.Method.GET, mainRequestUrl, onPostsLoaded1, onPostsError);
        requestQueue.add(request);
    }
    private void sendSecondRequest(String url) {
        StringRequest request = new StringRequest(Request.Method.GET, url, onPostsLoaded2, onPostsError);
        requestQueue.add(request);
    }

    Runnable sendRequest = new Runnable() {
        @Override
        public void run() {
            if(!connectionHandler.isConnected() || gpsHandler.isLatLongEmpty() || !status.equals("idle")){
                //Toast.makeText(MainActivity.this, "empty", Toast.LENGTH_LONG).show();
                postDelayed(sendRequest, 5000);
                return;
            }
    //        timeInterval=30;
//            locationData.clear();
            setMainRequestUrl(requestCreator.getFirstRequest(Double.toString(gpsHandler.getLatitude()), Double.toString(gpsHandler.getLongitude()),null));
            resultTextField.setText(requestCreator.getFirstRequest(Double.toString(gpsHandler.getLatitude()), Double.toString(gpsHandler.getLongitude()),null)+" "+gpsHandler.getLatitude());
            sendFirstRequest();
 //           Toast.makeText(MainActivity.this, "first req passed! "+ Integer.toString(locationData.size()), Toast.LENGTH_LONG).show();
//
 //           Toast.makeText(MainActivity.this, "second req passed!", Toast.LENGTH_LONG).show();
                //stopRepeatingTask();
            postDelayed(sendRequest,1000);
        }
    };

    private final Response.Listener<String> onPostsLoaded1 = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
//            Toast.makeText(MainActivity.this, "get it!", Toast.LENGTH_LONG).show();
            ResponseClass res = gson.fromJson(response, ResponseClass.class);
            if(!res.status.equals("OK")){
                status="idle";
                return;
            }
            for(int i=0; i<res.results.size(); i++){
                sendSecondRequest(requestCreator.getSecondRequest(res.results.get(i).place_id));

   //             locationData.add(new LocationData("locationContact"+i,res.results.get(i).place_id));
   //             resultTextField.setText(resultTextField.getText()+" : "+locationData.get(i).getId());
            }
            if(res.pageToken!=null){
                Toast.makeText(MainActivity.this, "has next", Toast.LENGTH_LONG).show();
                mainRequestUrl=requestCreator.getFirstRequest(Double.toString(gpsHandler.getLatitude()),Double.toString(gpsHandler.getLongitude()),res.pageToken);
             //   sendFirstRequest();
            }
   //         resultTextField.setText("Number : ");
            status="done";
        }
    };

    //https://maps.googleapis.com/maps/api/place/search/json?location=-6.4653324,%20106.8598435&hasNextPage=true&nextPage()=true&radius=5000&types=hospital|police|fire_station&sensor=true&key=AIzaSyBrLe3fjpOvYhBRn3U9ypqeVfag3pgNQDY&pagetoken=

    private final Response.Listener<String> onPostsLoaded2 = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
//            resultTextField.setText(resultTextField.getText()+"Request("+requestCount+") |");
//            Toast.makeText(MainActivity.this, "Request2 fetched", Toast.LENGTH_LONG).show();
            ResponseClass2 res = gson.fromJson(response, ResponseClass2.class);
//            Toast.makeText(MainActivity.this, res.status, Toast.LENGTH_LONG).show();
            if(!res.status.equals("OK")) return;
            if(res.result.phoneNumber==null || res.result.phoneNumber.isEmpty()){
//                resultTextField.setText(resultTextField.getText()+"[Index("+requestCount+") Tercyduk] |");
//                locationData.remove(requestCount);
                return;
            }
            String id, name, type, phoneNumber, phoneNumberTemp;
            id = res.result.place_id;
            name = res.result.name;
            type = res.result.types.get(0);
            phoneNumberTemp = res.result.phoneNumber;
            phoneNumber="";
            for(int i=0; i< phoneNumberTemp.length(); i++){
                if(phoneNumberTemp.charAt(i)=='-' || phoneNumberTemp.charAt(i)==')' || phoneNumberTemp.charAt(i)=='(' || phoneNumberTemp.charAt(i)==' ') continue;
                phoneNumber += phoneNumberTemp.charAt(i);
            }
            //        Toast.makeText(MainActivity.this, phoneNumberTemp, Toast.LENGTH_LONG).show();
//            Toast.makeText(MainActivity.this, "locationdata total:"+locationData.size(), Toast.LENGTH_LONG).show();
//            locationData.get(requestCount).setName(res.result.name);
//            locationData.get(requestCount).setPhoneNumber(locationData.get(requestCount).parsePhoneNumber(res.result.phoneNumber));
//            locationData.get(requestCount).setType(res.result.types.get(0));
//            resultTextField.setText(resultTextField.getText()+Double.toString(res.result.geometry.location.lat)+" "+locationData.get(requestCount).getPhoneNumber() + " " + locationData.get(requestCount).getName()+" "+locationData.get(requestCount).getType() + " INDEX:("+requestCount+") |");

            switch(res.result.types.get(0)){
                case "hospital":
                    hospitalLocation.add(new LocationData("hospital", id, name, type, phoneNumber));
                    break;
                case "police":
                    policeLocation.add(new LocationData("police", id, name, type, phoneNumber));
                    break;
                case "fire_station":
                    fireStationLocation.add(new LocationData("fire station", id, name, type, phoneNumber));
                    break;
                default:
                    break;
            }
//            resultTextField.setText(resultTextField.getText()+name+"|");
        }
    };

    private final Response.ErrorListener onPostsError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(MainActivity.class.getSimpleName(), error.toString());
        }
    };
}
////////////////////////////////////////////////////////////////////////
//    private void fetchPosts1(String url) {
//        StringRequest request = new StringRequest(Request.Method.GET, url, onPostsLoaded1, onPostsError);
//        requestQueue.add(request);
//    }
//    private void fetchPosts2(String url) {
//        StringRequest request = new StringRequest(Request.Method.GET, url, onPostsLoaded2, onPostsError);
//        requestQueue.add(request);
//    }
//
//    private final Response.Listener<String> onPostsLoaded1 = new Response.Listener<String>() {
//        @Override
//        public void onResponse(String response) {
//            ResponseClass res = gson.fromJson(response, ResponseClass.class);
//            if(!res.status.equals("OK"))return;
//            for(int i=0; i<res.results.size(); i++){
//                locationData.add(new LocationData("locationContact"+i,res.results.get(i).place_id));
//                resultTextField.setText(resultTextField.getText()+" : "+locationData.get(i).getId());
//            }
//            //Toast.makeText(MainActivity.this, requestCreator.getSecondRequest(locationData.get(0).getId()), Toast.LENGTH_LONG).show();
//            resultTextField.setText("Number : ");
//            for(int i=0; i<locationData.size(); i++) {
//                fetchPosts2(requestCreator.getSecondRequest(locationData.get(i).getId()));
//            }
//        }
//    };
//    private final Response.Listener<String> onPostsLoaded2 = new Response.Listener<String>() {
//        @Override
//        public void onResponse(String response) {
//            resultTextField.setText(resultTextField.getText()+"Request("+requestCount+") |");
////            Toast.makeText(MainActivity.this, "Request2 fetched", Toast.LENGTH_LONG).show();
//            ResponseClass2 res = gson.fromJson(response, ResponseClass2.class);
////            Toast.makeText(MainActivity.this, res.status, Toast.LENGTH_LONG).show();
//            if(!res.status.equals("OK")) return;
//            if(res.result.phoneNumber==null || res.result.phoneNumber.isEmpty()){
//                resultTextField.setText(resultTextField.getText()+"[Index("+requestCount+") Tercyduk] |");
//                locationData.remove(requestCount);
//                return;
//            }
//            locationData.get(requestCount).setName(res.result.name);
//            locationData.get(requestCount).setPhoneNumber(locationData.get(requestCount).parsePhoneNumber(res.result.phoneNumber));
//            locationData.get(requestCount).setType(res.result.types.get(0));
//            resultTextField.setText(resultTextField.getText()+Double.toString(res.result.geometry.location.lat)+" "+locationData.get(requestCount).getPhoneNumber() + " " + locationData.get(requestCount).getName()+" "+locationData.get(requestCount).getType() + " INDEX:("+requestCount+") |");
//            requestCount++;
//        }
//    };
//
//    private final Response.ErrorListener onPostsError = new Response.ErrorListener() {
//        @Override
//        public void onErrorResponse(VolleyError error) {
//            Log.e(MainActivity.class.getSimpleName(), error.toString());
//        }
//    };
    //////////////////////JSON Class - 1//////////////
    public class ResponseClass{
        @SerializedName("results")
        List<Results> results;
        @SerializedName("status")
        String status;

        @SerializedName("next_page_token")
        String pageToken=null;
    }
    public class Results{
        @SerializedName("place_id")
        String place_id=null;
    }
    //////////End of JSON Class - 1//////////////1/////
    //////////////////////JSON Class - 2//////////////
    public class ResponseClass2{
        //@SerializedName("result")
        Results2 result;
        //@SerializedName("status")
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
    //////////End of JSON Class - 2///////////////////
}

    //    public class ConnectionChecker extends Handler {
//        private int timeInterval = 5000;
//        private ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        private NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//        private boolean connectionStatus;
//        private void checkConnection(){
//            connectionStatus = activeNetwork != null && activeNetwork.isConnected();
//        }
//        public Boolean isConnected(){return connectionStatus;}
//        public int getTimeInterval(){return timeInterval;}
//
//        Runnable connectionStatusChecking = new Runnable() {
//            @Override
//            public void run() {
//                String statusText;
//                connectionChecker.checkConnection();
//                if(connectionChecker.isConnected()) statusText = "Connected";
//                else statusText = "Disconnected";
//                statusTextField.setText(statusText);
//                postDelayed(connectionStatusChecking, connectionChecker.timeInterval);
//            }
//        };
//
//        public void startRepeatingTask() {connectionStatusChecking.run();}
//        public void stopRepeatingTask() {removeCallbacks(connectionStatusChecking);}
//    }




//    private class ConnectionChecking extends AsyncTask<Boolean, Boolean, Boolean> {
//        @Override
//        protected Boolean doInBackground(Boolean... params) {
//            try {
//                int timeoutMs = 10000;
//                Socket sock = new Socket();
//                SocketAddress sockaddr = new InetSocketAddress("8.8.8.8", 53);
//
//                sock.connect(sockaddr, timeoutMs);
//                sock.close();
//
//                return true;
//            } catch (IOException e) { return false; }
//        }
//
//        @Override
//        protected void onPreExecute() {
//            text.setText("Checking internet connection..");
//        }
//        @Override
//        public void onPostExecute(Boolean status){
//            String statusText;
//            if(status) statusText = "Connected";
//                    else statusText = "Disconnected";
//            text.setText(statusText);
//        }
//    }
