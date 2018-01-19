
package com.deepanshu.location_alert;

/**
 * Created by deepanshu on 12/1/18.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;



public class GPSTracker extends Service implements LocationListener {

    // Get Class Name
    private static String TAG = GPSTracker.class.getName();

    private final Context mContext;

    // flag for GPS Status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS Tracking is enabled
    boolean isGPSTrackingEnabled = false;

    Location location;
    double distance_from_home = 0.0;
    double distance_from_office = 0.0;
    double latitude,longitude;
    double LATITUDE_home = 12.974126666666667, LONGITUDE_home = 77.638460000000000;
    double LATITUDE_office = 12.979780000000002, LONGITUDE_office = 77.63865000000001;
    double range = 0.100;
    String[] data = {"0","1","2"};

    // How many Geocoder should return our GPSTracker
    int geocoderMaxResults = 1;

    // The minimum distance to change updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    // Store LocationManager.GPS_PROVIDER or LocationManager.NETWORK_PROVIDER information
    private String provider_info;

    public Intent intent;

    public  GPSTracker()
    {
        this.mContext = this;
    }
    public GPSTracker(Context context) {
        this.mContext = context;
        getLocation();
        String loc = "  lon: "+ longitude + " lat:" + latitude ;
        appendLog(loc);
        distance_from_home = distance(latitude,longitude,LATITUDE_home,LONGITUDE_home);
        distance_from_office = distance(latitude,longitude,LATITUDE_office,LONGITUDE_office);
        check_range(context);
    }

    /**
     * Try to get my current location by GPS or Network Provider
     */
    public void getLocation() {
        Log.e(TAG, "Inside get location");
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            //getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            //getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            // Try to get location if you GPS Service is enabled
            if (isGPSEnabled) {
                this.isGPSTrackingEnabled = true;
                Log.e(TAG, "Application use GPS Service");
				/*
				 * This provider determines location using
				 * satellites. Depending on conditions, this provider may take a while to return
				 * a location fix.
				 */
                provider_info = LocationManager.GPS_PROVIDER;
            } else if (isNetworkEnabled) { // Try to get location if you Network Service is enabled
                this.isGPSTrackingEnabled = true;
                Log.e(TAG, "Application use Network State to get GPS coordinates");
				/*
				 * This provider determines location based on
				 * availability of cell tower and WiFi access points. Results are retrieved
				 * by means of a network lookup.
				 */
                provider_info = LocationManager.NETWORK_PROVIDER;
            }

            // Application can use GPS or Network Provider
            if (!provider_info.isEmpty()) {
                locationManager.requestLocationUpdates(
                        provider_info,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES,
                        this
                );
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(provider_info);
                    updateGPSCoordinates();
                }
            }
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            Log.e(TAG, "Impossible to connect to LocationManager", e);
            Date currentTime = Calendar.getInstance().getTime();
            String loc = currentTime+"  exception:" + e;
            appendLog(loc);

        }
    }

    /**
     * Update GPSTracker latitude and longitude
     */
    public void updateGPSCoordinates() {
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
    }

    /**
     * GPSTracker latitude getter and setter
     * @return latitude
     */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        return latitude;
    }

    /**
     * GPSTracker longitude getter and setter
     * @return
     */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        return longitude;
    }

    /**
     * GPSTracker isGPSTrackingEnabled getter.
     * Check GPS/wifi is enabled
     */
    public boolean getIsGPSTrackingEnabled() {

        return this.isGPSTrackingEnabled;
    }

    /**
     * Stop using GPS listener
     * Calling this method will stop using GPS in your app
     */
    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    /**
     * Function to show settings alert dialog
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        //Setting Dialog Title
        alertDialog.setTitle(R.string.GPSAlertDialogTitle);
        //Setting Dialog Message
        alertDialog.setMessage(R.string.GPSAlertDialogMessage);
        //On Pressing Setting button
        alertDialog.setPositiveButton(R.string.action_settings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });
        //On pressing cancel button
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    /**
     * Get list of address by latitude and longitude
     * @return null or List<Address>
     */
    public List<Address> getGeocoderAddress(Context context) {
        if (location != null) {
            Geocoder geocoder = new Geocoder(context, Locale.ENGLISH);
            try {
                /**
                 * Geocoder.getFromLocation - Returns an array of Addresses
                 * that are known to describe the area immediately surrounding the given latitude and longitude.
                 */
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, this.geocoderMaxResults);
                return addresses;
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Impossible to connect to Geocoder", e);
            }
        }
        return null;
    }

    /**
     * Try to get AddressLine
     * @return null or addressLine
     */
    public String getAddressLine(Context context) {
        List<Address> addresses = getGeocoderAddress(context);
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String addressLine = address.getAddressLine(0);
            return addressLine;
        } else {
            return null;
        }
    }

    /**
     * Try to get Locality
     * @return null or locality
     */
    public String getLocality(Context context) {
        List<Address> addresses = getGeocoderAddress(context);
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String locality = address.getLocality();
            return locality;
        }
        else {
            return null;
        }
    }
    /**
     * Try to get Postal Code
     * @return null or postalCode
     */
    public String getPostalCode(Context context) {
        List<Address> addresses = getGeocoderAddress(context);
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String postalCode = address.getPostalCode();
            return postalCode;
        } else {
            return null;
        }
    }

    /**
     * Try to get CountryName
     * @return null or postalCode
     */
    public String getCountryName(Context context) {
        List<Address> addresses = getGeocoderAddress(context);
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String countryName = address.getCountryName();
            return countryName;
        } else {
            return null;
        }
    }
    /**
     * Try to get distance from home
     * @return null or distance
     */
    public double getDistance() {

        return distance_from_home;
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        updateGPSCoordinates();
        String loc = "  lon: "+ longitude + " lat:" + latitude ;
        appendLog(loc);
        distance_from_home = distance(latitude,longitude,LATITUDE_home,LONGITUDE_home);
        distance_from_office = distance(latitude,longitude,LATITUDE_office,LONGITUDE_office);
        Log.e(TAG,"distance1"+distance_from_home+" distance1"+distance_from_office);
        check_range(this);
        Toast.makeText(mContext,"location update location changed",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        String loc = "  provider: "+ provider + " status:" + status ;
        appendLog(loc);
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful

        getLocation();
        distance_from_home = distance(latitude,longitude,LATITUDE_home,LONGITUDE_home);
        distance_from_office = distance(latitude,longitude,LATITUDE_office,LONGITUDE_office);
        check_range(this);
        return Service.START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed ", Toast.LENGTH_LONG).show();
    }
    public void appendLog(String text) {
        File logFile = new File("sdcard/log.txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            Date currentTime = Calendar.getInstance().getTime();
            text = currentTime+text;
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    public  void call_api(Context c,String value)
    {
        To_call_api to_call_api = new To_call_api(c,value);
    }
    public void check_range(Context c)
    {

        if(distance_from_home<range)
            call_api(c,data[0]);
        else
            if(distance_from_office<range)
                call_api(c,data[1]);
            else
                call_api(c,data[2]);
    }
    private double distance(double lat1, double lng1, double lat2, double lng2) {

        DecimalFormat df = new DecimalFormat("#.###");
        double earthRadius = 3958.75; // in miles, change to 6371 for kilometer output for result in miles 3958.75
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2) * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist_miles = earthRadius * c;
        double dist_km = dist_miles *  1.6093;
        return Double.valueOf(df.format(dist_km));
    }
}
