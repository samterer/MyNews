package com.hzpd.utils;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.hzpd.ui.App;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Location 定位 api http://maps.google.com/maps/api/geocode/json?latlng=-4.8371544045,121.9235229492&sensor=true&language=zh-CN
 */
public class LocationUtils implements LocationListener {
    private LocationManager locationManager;
    private static LocationUtils locationUtils;

    public static void getLocation(Context context) {
        if (locationUtils != null) {
            locationUtils.release();
        }
        locationUtils = new LocationUtils(context);
        locationUtils.locate();
    }

    private LocationUtils(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    private void locate() {

        String provider = LocationManager.NETWORK_PROVIDER;
        if (!locationManager.isProviderEnabled(provider)) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(true);
            provider = locationManager.getBestProvider(criteria, true);
        }
        if (provider != null) {
            locationManager.requestLocationUpdates(provider, 0, 0, this);
        } else {
            Log.e("test", "News: 无法定位");
        }
    }

    private void release() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
            locationManager = null;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e("test", "News: " + location);
        try {
            Geocoder gcd = new Geocoder(App.getInstance(), Locale.getDefault());
            List<Address> addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            locationManager.removeUpdates(this);
            Log.e("test", "News: " + addresses);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                final String countryCode = address.getCountryCode();
                final String city = address.getLocality();
                final String addressLine = address.getAddressLine(0);
                Log.e("test", "News: " + countryCode + ":" + city + ":" + addressLine);
                SPUtil.setCountryCode(countryCode);
                SPUtil.setCity(city);
                SPUtil.setAddress(addressLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        locationUtils = null;
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
}
