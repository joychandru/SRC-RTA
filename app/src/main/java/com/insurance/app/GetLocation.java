package com.insurance.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class GetLocation {
    private static final String TAG = "Debug";
    private Context context;
    private Boolean flag;
    private Activity myActivity;

    public GetLocation(Activity myActivity) {
        this.flag = Boolean.valueOf(false);
        this.context = myActivity;
        this.myActivity = myActivity;
    }

    public GetLocation(Context context) {
        this.flag = Boolean.valueOf(false);
        this.context = context;
    }

    public Location getLocationData() {
        this.flag = displayGpsStatus();
        if (this.flag.booleanValue()) {
            Log.v(TAG, "onClick");
            return getLastKnownLocation();
        }
        Log.d("Gps Status!!", "Your GPS is: OFF");
        return null;
    }

    public Boolean displayGpsStatus() {
        if (Secure.isLocationProviderEnabled(this.context.getContentResolver(), "gps")) {
            return Boolean.valueOf(true);
        }
        return Boolean.valueOf(false);
    }

    /* GPS Constant Permission */
    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 12;

    /* Position */
    private static final int MINIMUM_TIME = 10000;  // 10s
    private static final int MINIMUM_DISTANCE = 50; // 50m

    /* GPS */
    private String mProviderName;


    private Location getLastKnownLocation() {

        LocationManager locationManager = (LocationManager) this.context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        Location bestLocation = null;

        // Get the best provider between gps, network and passive
        Criteria criteria = new Criteria();
        mProviderName = locationManager.getBestProvider(criteria, true);

        // API 23: we have to check if ACCESS_FINE_LOCATION and/or ACCESS_COARSE_LOCATION permission are granted
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // No one provider activated: prompt GPS
            if (mProviderName == null || mProviderName.equals("")) {
                this.myActivity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }

        for (String provider : locationManager.getProviders(true)) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l != null && (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy())) {
                bestLocation = l;
            }
        }
        return bestLocation;
    }
}
