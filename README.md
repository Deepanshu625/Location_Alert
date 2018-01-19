# Location_Alert

This app gets the location of the user and sent it to the boltiot device so, that iot device can predict your exact location. Also this contains a feature of loging the change in location and if you want to sent your exact loctiton, you can mail it through the app.

To use location we need android.Location package.

To get yours position you need to set some location's latitude and longitude values.

#This function is used to get value of location:

    public void getLocation() {
        Log.e(TAG, "Inside get location");
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isGPSEnabled) {
                this.isGPSTrackingEnabled = true;
                Log.e(TAG, "Application use GPS Service");
                provider_info = LocationManager.GPS_PROVIDER;
            } else if (isNetworkEnabled) { // Try to get location if you Network Service is enabled
                this.isGPSTrackingEnabled = true;
                Log.e(TAG, "Application use Network State to get GPS coordinates");
                provider_info = LocationManager.NETWORK_PROVIDER;
            }
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
        }
    }
    
So to get latitude and longitude we can use location.latitude, location.longitude
 
 Also to get the distance through longitude and latitude, we need to put some efforts like:
 
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
    
    We also implemented some small functions to get address of device, city or country from his longitude  and latitude locaiton like:
    
        public List<Address> getGeocoderAddress(Context context) {
          if (location != null) {
            Geocoder geocoder = new Geocoder(context, Locale.ENGLISH);
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, this.geocoderMaxResults);
                return addresses;
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Impossible to connect to Geocoder", e);
            }
        }
        return null;
      }
    
this get the location and save it in a list. So, we can easily get it by using:
      
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
 
 To make your service alive if you clear your RAM use,
         
         @Override
      public int onStartCommand(Intent intent, int flags, int startId) {
          getLocation();
          return Service.START_STICKY;
      }
    
    
 Atlast, to ping boltiot use  AsyncTask with https and get method and do not forget to edit your url configurations.
 
      String api = "https://cloud.boltiot.com/remote/<API KEY>/"serialWrite?data="+value+"&deviceName=<Boltid>";
 
 Some Instructions:
 
        *if your device is using Marshallow or upgraded version, you need to give permissions by going into permission  setting in your device.
        *Change the cloud url according to your id and api key.
        *This code is to sent 3 values, two for particular location and third value for rest of the locations.
        *You can also ping on this url through browser to check if it is working perfectly.
        *First you need to run your app to get your exact latitude and longitude location.
        
 . 
 
 ![alt tag](https://github.com/Deepanshu625/Location_Alert/blob/master/Screenshot_2018-01-18-20-28-16-036.jpeg "If everything works fine your app will look like this")




