package com.example.geotrend;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;


public class MainActivity extends AppCompatActivity {


    Button start, stop;
    String TOPIC = "/topics/geotrend";
    WebView map;
    String PROVIDER = "";
    private String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACTIVITY_RECOGNITION};

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    String android_id = "";

    String URL = "";
    String PC_URL     = "http://10.0.2.2:80/";
    String DEVICE_URL = "http://192.168.43.79/";
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(arePermissionsEnabled()){
                    //          permissions granted, continue flow normally
            }else{
                requestMultiplePermissions();
            }
        }

        prefs = getSharedPreferences("", Context.MODE_PRIVATE);
        editor = getApplicationContext().getSharedPreferences("", Context.MODE_PRIVATE).edit();
        Log.d("GEOJSON", "PRE");
        loadPreferences();
        editor.clear();
        editor.commit();

        Log.d("GEOJSON", "POST");
        loadPreferences();
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC);

        android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        URL = PC_URL+"myPaths.php?userid=" + android_id;

        map = findViewById(R.id.map);
        map.getSettings().setJavaScriptEnabled(true); // enable javascript
        map.getSettings().setAllowContentAccess(true);
        map.getSettings().setAllowFileAccess(true);
        map.getSettings().setDatabaseEnabled(true);
        map.getSettings().setDomStorageEnabled(true);
        map.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                // Redirect to deprecated method, so you can use it in all SDK versions
                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }

        });
        map.loadUrl(URL);

        start = findViewById(R.id.btn_start_tracking);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                    String provider = prefs.getString("bestp", PROVIDER);

                    Log.d("GEOJSON", provider);
                    if (provider.equals("")) {

                        editor.putString("bestp", LocationManager.NETWORK_PROVIDER);

                    }

                    String tmp = getRandomString(7);
                    editor.putString("token", tmp);
                    editor.apply();
                    Log.d("GEOJSON", "ABOUT TO PRESS START TOKEN: "+prefs.getString("token", ""));
                    startService(new Intent(MainActivity.this, LocationService.class));
             }
        });
        stop = findViewById(R.id.btn_stop_tracking);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Service stopped!", Toast.LENGTH_SHORT).show();
                stopService(new Intent(MainActivity.this, LocationService.class));
            }
        });


    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean arePermissionsEnabled(){
        for(String permission : permissions){
            if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    public void start()
    {
        startService(new Intent(MainActivity.this, LocationService.class));
    }
    @NonNull
    private static String getRandomString(final int sizeOfRandomString)
    {
        String ALLOWED_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm";
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder(sizeOfRandomString);
        for(int i = 0;i < sizeOfRandomString; i++)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestMultiplePermissions(){
        List<String> remainingPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                remainingPermissions.add(permission);
            }
        }
        requestPermissions(remainingPermissions.toArray(new String[remainingPermissions.size()]), MY_PERMISSIONS_REQUEST_LOCATION);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if(requestCode == MY_PERMISSIONS_REQUEST_LOCATION){
            for(int i=0;i < grantResults.length;i++){
                if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    if(shouldShowRequestPermissionRationale(permissions[i])){
                        new AlertDialog.Builder(this)
                                .setMessage("Permissions need to be granted in order for it to work")
                                .setPositiveButton("Allow", (dialog, which) -> requestMultiplePermissions())
                                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                .create()
                                .show();
                    }
                    return;
                }
            }
            //all is good, continue flow

        }
    }
    //startService(new Intent(this, LocationService.class));
    @Override
    public void onResume()
    {
        super.onResume();
        map = findViewById(R.id.map);
        map.getSettings().setJavaScriptEnabled(true); // enable javascript
        map.getSettings().setAllowContentAccess(true);
        map.getSettings().setAllowFileAccess(true);
        map.getSettings().setDatabaseEnabled(true);
        map.getSettings().setDomStorageEnabled(true);
        map.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                // Redirect to deprecated method, so you can use it in all SDK versions
                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }

        });
        map.loadUrl(URL);
       // startService(new Intent(MainActivity.this, LocationService.class));

    }

    @Override
    public void onPause() {

        super.onPause();
        if(!(isMyServiceRunning(LocationService.class)))
        {
            Log.d("GEOJSON", "ONLOCATION Restarted");
            startService(new Intent(MainActivity.this, LocationService.class));
        }
        else
        {
            Log.d("GEOJSON", "ONLOCATION Running");
        }
    }
    public void loadPreferences() {

       String test = "";
        Map<String, ?> prefs = getSharedPreferences("", MODE_PRIVATE).getAll();
        for (String key : prefs.keySet()) {
            Object pref = prefs.get(key);
            String printVal = "";
            if (pref instanceof Boolean) {
                printVal =  key + " : " + (Boolean) pref;
            }
            if (pref instanceof Float) {
                printVal =  key + " : " + (Float) pref;
            }
            if (pref instanceof Integer) {
                printVal =  key + " : " + (Integer) pref;
            }
            if (pref instanceof Long) {
                printVal =  key + " : " + (Long) pref;
            }
            if (pref instanceof String) {
                printVal =  key + " : " + (String) pref;
            }
            if (pref instanceof Set<?>) {
                printVal =  key + " : " + (Set<String>) pref;
            }
            // Every new preference goes to a new line
            test += (printVal + "\n");
        }
        Log.d("GEOJSON", test);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
