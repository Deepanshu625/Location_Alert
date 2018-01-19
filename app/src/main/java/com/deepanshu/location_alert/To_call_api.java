package com.deepanshu.location_alert;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by deepanshu on 14/1/18.
 */

public class To_call_api {

    Context context;
    String[] pos = {"at HOME","at OFFICE","LOST"};
    String value = "0";

    public To_call_api(Context context, String value)
    {
        this.context = context;
        this.value = value;
        Toast.makeText(context, "You are "+pos[Integer.parseInt(value)], Toast.LENGTH_SHORT).show();
        new on_start().execute();
    }
    private class on_start extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(Void... params) {
            String line, response = "no internet";
            String api = "https://cloud.boltiot.com/remote/<API KEY>/" +
                    "serialWrite?data="+value+"&deviceName=<Boltid>";

            if (haveNetworkConnection()) {
                try {
                    URL url = new URL(api);
                    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                    if (Build.VERSION.SDK != null && Build.VERSION.SDK_INT > 13) {
                    }
                    //conn.setRequestProperty("Accept-Encoding", "");
                    conn.setReadTimeout(30000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        // read the response
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        while ((line = br.readLine()) != null) {
                            response += line;
                        }
                    } else {
                        response = "server";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                response = "no internet";
            }

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            Log.e("RESPONSE", response);
            if (response.equals("no internet")) {
                Toast.makeText(context, "No internet Connection", Toast.LENGTH_SHORT).show();
            } else if (response.equals("server")) {
                Toast.makeText(context, "Can't connect to server", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("Location Updated", response);
                Toast.makeText(context, ""+response, Toast.LENGTH_SHORT).show();
            }
        }

        private boolean haveNetworkConnection() {
            boolean haveConnectedWifi = false;
            boolean haveConnectedMobile = false;
            try
            {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo[] netInfo = cm.getAllNetworkInfo();
                for (NetworkInfo ni : netInfo) {
                    if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                        if (ni.isConnected())
                            haveConnectedWifi = true;
                    if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                        if (ni.isConnected())
                            haveConnectedMobile = true;
                }
            }
            catch (Exception ex)
            {
                Log.e("Exception raised",""+ ex);
            }

            return haveConnectedWifi || haveConnectedMobile;
        }
    }
}
