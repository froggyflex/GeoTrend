package com.example.geotrend;


import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.location.DetectedActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Calendar;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class LocationService extends Service implements LocationListener {

    LocationManager m_locationManager;
    String label = "walking";
    int confidence = -1;
    BroadcastReceiver broadcastReceiver;
    String PROVIDER = "";


    @Override
    public void onCreate() {
        this.m_locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

//
//        broadcastReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//
//                if (intent.getAction().equals(Costants.Constants.BROADCAST_DETECTED_ACTIVITY)) {
//                    int type = intent.getIntExtra("type", -1);
//                    int confidence = intent.getIntExtra("confidence", 0);
//                    Log.d("GEOJSON", "ABOUT TO CALL HADLEUSERACTIVITY");
//                    handleUserActivity(type, confidence);
//                }
//
//            }
//
//        };
        //startTracking();
        //since I have problems with the emulator I am assuming the user is walking only
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(Costants.Constants.BROADCAST_DETECTED_ACTIVITY));
     }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(getApplicationContext(), "Service starts", Toast.LENGTH_SHORT).show();
        Log.d("GEOJSON", "onStartCommand");

        this.m_locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        this.m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER , 1000, 1, this);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTracking();
        //continueMonitoring(); // if we dont want to close it for good
    }

    public String getProvider()
    {
        SharedPreferences prefs = getSharedPreferences("", MODE_PRIVATE);
        String PROVIDERTMP = prefs.getString("bestp", "");

        if(PROVIDERTMP.equals(""))
        {
            PROVIDER = LocationManager.NETWORK_PROVIDER;
        }
        else
        {
            PROVIDER = LocationManager.GPS_PROVIDER;
        }
        return PROVIDER;
    }



    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        //stopTracking();
        continueMonitoring();
    }

    private void handleUserActivity(int type, int confidence) {
         label = getString(R.string.activity_unknown);
         this.confidence = confidence;

        switch (type) {
            case DetectedActivity.IN_VEHICLE: {
                label = getString(R.string.activity_in_vehicle);
                Log.d("CONFIDENCE", label +" ||| " +confidence);
                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                label = getString(R.string.activity_on_bicycle);
                Log.d("CONFIDENCE", label +" ||| " +confidence);
                break;
            }
            case DetectedActivity.ON_FOOT: {
                label = getString(R.string.activity_on_foot);
                Log.d("CONFIDENCE", label +" ||| " +confidence);
                break;
            }
            case DetectedActivity.RUNNING: {
                label = getString(R.string.activity_running);
                Log.d("CONFIDENCE", label +" ||| " +confidence);
                break;
            }
            case DetectedActivity.STILL: {
                label = getString(R.string.activity_still);
                Log.d("CONFIDENCE", label +" ||| " +confidence);
                break;
            }
            case DetectedActivity.TILTING: {
                label = getString(R.string.activity_tilting);
                Log.d("CONFIDENCE", label +" ||| " +confidence);

                break;
            }
            case DetectedActivity.WALKING: {
                label = getString(R.string.activity_walking);
                Log.d("CONFIDENCE", label +" ||| " +confidence);

                break;
            }
            case DetectedActivity.UNKNOWN: {
                label = getString(R.string.activity_unknown);
                Log.d("CONFIDENCE", label +" ||| " +confidence);
                break;
            }
        }

    }
    private void continueMonitoring()
    {

        Intent myIntent = new Intent(getApplicationContext(), LocationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, myIntent, 0);
        AlarmManager alarmManager1 = (AlarmManager) getSystemService(ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 10);
        alarmManager1.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        //Toast.makeText(getApplicationContext(), "Start Alarm", Toast.LENGTH_SHORT).show();

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onLocationChanged(Location loc) {
        Log.d("GEOJSON", "ONLOCATION CHANGED");
        if (loc == null)        //  Filtering out null values
            return ;

        String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),Settings.Secure.ANDROID_ID);

        Double lat = loc.getLatitude();
        Double lon = loc.getLongitude();

        JSONObject props = new JSONObject();

        JSONObject geometry = new JSONObject();
        JSONObject features = new JSONObject();
        JSONObject geojson = new JSONObject();
        JSONArray  ft;
        JSONArray  ca = new JSONArray().put(lat);
                   ca.put(lon);
         try {


            props.put("status",label);
            geometry.put("type","Point");
            geometry.put("coordinates", ca);

            features.put("type", "Feature");
            features.put("geometry", geometry);
            features.put("properties", props);

            ft = new JSONArray().put(features);
            geojson.put("type", "FeatureCollection");
            geojson.put("features", ft);




        } catch (JSONException e) {
            e.printStackTrace();
        }
        //if (confidence > Costants.Constants.CONFIDENCE) {

            BackgroundWorker backgroundWorker = new BackgroundWorker(getBaseContext());
            backgroundWorker.execute(geojson.toString(),android_id, lat.toString(), lon.toString());

        //}


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
    private void startTracking() {
        Log.d("GEOJSON", "CALLING DETECTIONACTIVITY");
        Intent intent1 = new Intent(getApplicationContext(), BackgroundDetectedActivitiesService.class);
        startService(intent1);
    }
    private void stopTracking() {

           //Intent intent = new Intent(getBaseContext(), BackgroundDetectedActivitiesService.class);
          //stopService(intent);
        m_locationManager.removeUpdates(this);
        Log.d("GEOJSON", "ONLOCATION removeUpdates");
    }

}