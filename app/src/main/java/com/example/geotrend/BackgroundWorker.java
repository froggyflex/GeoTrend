package com.example.geotrend;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BackgroundWorker
        extends AsyncTask<String, String, String>
{
    Context context;
    ProgressDialog pro;
    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = "key=" + "my_key";
    final private String contentType = "application/json";
    String lat;
    String lon;
    String id;
    String PC_URL     = "http://10.0.2.2:80/";
    String DEVICE_URL = "http://192.168.43.79/";


    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    BackgroundWorker(Context paramContext)
    {
        this.context = paramContext;
        this.pro = new ProgressDialog(paramContext);
        prefs         = context.getSharedPreferences("", Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    protected String doInBackground(String... list)
    {


        String tokenUP = prefs.getString("token", "");
        Log.d("GEOJSON", "TOKEN "+ tokenUP);
        HttpURLConnection localHttpURLConnection;
        InputStream localInputStream;
        String response = "";
        try
        {
            lat = list[2];
            lon = list[3];
            id  = list[1];

            localHttpURLConnection = (HttpURLConnection)new URL(DEVICE_URL+"GeoTrendFilter.php").openConnection();
            localHttpURLConnection.setRequestMethod("POST");
            localHttpURLConnection.setDoOutput(true);
            localHttpURLConnection.setDoInput(true);
            OutputStream localOutputStream = localHttpURLConnection.getOutputStream();
            BufferedWriter localBufferedWriter = new BufferedWriter(new OutputStreamWriter(localOutputStream, "UTF-8"));
            localBufferedWriter.write(URLEncoder.encode("geojson",  "UTF-8")   + "=" + URLEncoder.encode(list[0], "UTF-8")
                                  + "&" + URLEncoder.encode("deviceID", "UTF-8")   + "=" + URLEncoder.encode(list[1], "UTF-8")
                                  + "&" + URLEncoder.encode("initial",  "UTF-8")   + "=" + URLEncoder.encode(tokenUP, "UTF-8"));

            localBufferedWriter.flush();
            localBufferedWriter.close();
            localOutputStream.close();


            localInputStream = localHttpURLConnection.getInputStream();
            BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(localInputStream, "iso-8859-1"));

            String res = "";
            while((res = localBufferedReader.readLine()) != null)
            {
                response = res;
            }
            localBufferedReader.close();

            return response;

        }
        catch (Exception localMalformedURLException)
        {
            Log.d("GEOJSON CATCH: ", "->"+localMalformedURLException.getMessage()+"\n"+localMalformedURLException.getStackTrace());
        }


        return response;
    }

    @Override

    protected void onPostExecute(String gottenResponse) {
        super.onPostExecute(gottenResponse);
        Log.d("GEOJSON", gottenResponse);
        String NOTIFICATION_TITLE;
        String NOTIFICATION_MESSAGE;
        String LINK_DATA;

        NOTIFICATION_TITLE   = "Hey there you just got through one Geofence!";
        NOTIFICATION_MESSAGE = "";
        LINK_DATA            = "";

        Log.d("GEOJSON", gottenResponse.toString());
        if(gottenResponse.startsWith("MX:"))
        {

            //user just came out from a geofence so we will update the path token
            if(prefs.getString("check", "").equals("IN"))
            {
                editor.putString("token", getRandomString(7));
                editor.putString("check", "OUT");
                Log.d("GEOJSON", "PATH TOKEN UPDATED AS USER COMES OUT FROM A FENCE");
                //if the area is not in the shared prefs, the user is still outside of the fences since the response is not a json obj
                editor.remove(gottenResponse);
                editor.commit();
                Log.d("GEOJSON", "USER IS OUTSIDE OF THE SPECIFIED AREAS - CLEARING DATA");
            }
            else
            {
                //if the area is not in the shared prefs, the user is still outside of the fences since the response is not a json obj
                editor.remove(gottenResponse);
                editor.commit();
                Log.d("GEOJSON", "USER IS OUTSIDE OF THE SPECIFIED AREAS - CLEARING DATA");
            }

        }
        else
        {
            Log.d("GEOJSON", "WE GOT SOMETHING");
            editor.putString("check", "IN");
            try {
                JSONArray result = new JSONArray(gottenResponse);

                //update the provider based on the best choice
                String updateProvider = result.getJSONObject(0).getString("bp");
                editor.putString("bestp", updateProvider);
                editor.apply();

                for(int i=0;i<result.length();i++){

                    String message = result.getJSONObject(i).getString("message");
                    NOTIFICATION_MESSAGE = message;
                    if( !(result.getJSONObject(i).getString("link_data").equals("")))
                    {
                        LINK_DATA = result.getJSONObject(i).getString("link_data");
                    }

                }
                 //NOTIFICATION
                if(!(NOTIFICATION_MESSAGE.equals("")))
                {
                    String TOPIC = "/topics/geotrend";
                    JSONObject notification = new JSONObject();
                    JSONObject notifcationBody = new JSONObject();

                    try {
                        notifcationBody.put("title", NOTIFICATION_TITLE);
                        notifcationBody.put("message", NOTIFICATION_MESSAGE);
                        notifcationBody.put("link_data", LINK_DATA);
                        notification.put("to", TOPIC);
                        notification.put("data", notifcationBody);
                    } catch (JSONException e) {
                        Log.d("GEOJSON", "onCreate: " + e.getMessage() );
                    }


                    if(prefs.getString("MX:"+NOTIFICATION_MESSAGE, null)!= null)
                    {
                        if(prefs.getString("MX:"+NOTIFICATION_MESSAGE, null).equals("SENT"))
                        {
                            //based on the shared prefs the user fence is still registered as active so we dont need to send a notification
                            updateEntry(DEVICE_URL+"updateEntry.php", prefs.getString("token", ""), false);
                            Log.d("GEOJSON", " USER IS STILL INSIDE THE AREA", null);
                        }
                        else
                        {
                            //based on the shared prefs the user notification has not been sent so we can proceed with the sending procedure
                            Log.d("GEOJSON", "SENDING NOTIFICATION", null);
                            sendNotification(notification);
                            editor.putString("MX:"+NOTIFICATION_MESSAGE, "SENT");
                           // editor.putString("token", getRandomString(7));
                            editor.putString("poiC", lon+":"+lat);
                            editor.apply();
                            updateEntry(DEVICE_URL+"updateEntry.php", prefs.getString("token", ""), true);
                        }
                    }

                    else
                    {
                            Log.d("GEOJSON", "SENDING NOTIFICATION AS USER ENTERS IT", null);
                            sendNotification(notification);
                            editor.putString("MX:"+NOTIFICATION_MESSAGE, "SENT");
                            //editor.putString("token", getRandomString(7));
                            editor.putString("poiC", lon+":"+lat);
                            editor.apply();
                            updateEntry(DEVICE_URL+"updateEntry.php", prefs.getString("token", ""), true);

                    }

                }



            } catch (JSONException e) {
                e.printStackTrace();
            }
        }






    }



    private static String getRandomString(final int sizeOfRandomString)
    {
        String ALLOWED_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm";
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder(sizeOfRandomString);
        for(int i = 0;i < sizeOfRandomString; i++)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }
    private void updateEntry(String post_url, String token, Boolean isPOI)
    {
        Log.d("GEOJSON", "updateEntry " + isPOI);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, post_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("GEOJSON", response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("GEOJSON", error.networkResponse.toString());
                    }
                }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("did",id);
                params.put("token",token);
                params.put("lat",lat);
                params.put("lon",lon);
                params.put("poi",isPOI.toString());
                params.put("poiC",prefs.getString("poiC", null));
                return params;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);

    }
    private void sendNotification(JSONObject notification) {
        Log.d("GEOJSON", "TRYING TO SEND NOTIFICATION: ");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                response -> {
                    Log.d("GEOJSON", "onResponse: " + response.toString());

                },
                error -> {
                    Toast.makeText(context, "Request error", Toast.LENGTH_LONG).show();
                    Log.d("GEOJSON", "onErrorResponse: Didn't work");
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };
        MySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    protected void onPreExecute()
    {
//        this.pro.setMessage("Uploading Order. Please wait...");
//        this.pro.setIndeterminate(false);
//        this.pro.setCancelable(false);
//        this.pro.show();
    }

}
