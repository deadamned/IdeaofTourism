package com.idea.xxx.ideaoftourism;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DirectionsView {
    private Activity m_activity;
    private TextInputEditText text;

    public DirectionsView(DirectionsActivity directionsActivity) {
        m_activity = directionsActivity;
        initDirections();
    }

    private void initDirections() {
        text = m_activity.findViewById(R.id.text);
        Intent intent = new Intent(m_activity, SearchActivity.class);
        m_activity.startActivity(intent);

        LocationManager locationManager = (LocationManager) m_activity.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(m_activity.getApplicationContext(), Locale.getDefault());
                try {
                    addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    String address = addresses.get(0).getAddressLine(0);
                    text.setHint(address);

                } catch (IOException e) {
                    e.printStackTrace();
                }

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
        };
        if (ActivityCompat.checkSelfPermission(m_activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(m_activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(m_activity.getApplicationContext(), "Please allow location", Toast.LENGTH_LONG).show();
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 500, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 500, locationListener);

        Spinner spinnerradius = m_activity.findViewById(R.id.radius);
        ArrayAdapter<String> spinnerradiusArrayAdapter = new ArrayAdapter<>(m_activity, android.R.layout.simple_spinner_dropdown_item, m_activity.getResources().getStringArray(R.array.radius));
        spinnerradius.setAdapter(spinnerradiusArrayAdapter);
    }
}
